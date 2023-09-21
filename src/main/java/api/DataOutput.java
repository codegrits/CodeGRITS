package api;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import trackers.IDETracker;
import trackers.ScreenRecorder;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class DataOutput {
    private Consumer<String> eyeDataHandler;
    private Consumer<String> mouseDataHandler;
    private Consumer<String> ideDataHandler;
    private Editor editor;
    private Rectangle visibleArea;

    private IDETracker ideTracker;
    private ScreenRecorder screenRecorder = ScreenRecorder.getInstance();
    private Thread outputThread;
    private Process eyeTrackProcess;
    private Process mouseTrackProcess;
    private Process ideTrackProcess;

    public void addDataOutputConsumer(Consumer<String> dataOutputConsumer) {
        this.eyeDataHandler = dataOutputConsumer;
    }

    private void handleDataOutput(String data) {
        eyeDataHandler.accept(data);
    }

    public void checkEyeTracker() throws IOException, InterruptedException, IOException {
        String pythonInterpreter = "python";
        String pythonScript = """
                import tobii_research as tr
                found_eyetrackers = tr.find_all_eyetrackers()
                if found_eyetrackers == ():
                    print('No eye tracker found')
                else:
                    print('Found eye tracker')
                """
                ;

        ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, "-c", pythonScript);
        pb.redirectErrorStream(true); // Redirect error stream to output stream
        eyeTrackProcess = pb.start();
        // Get the process's standard output stream
        InputStream stdout = eyeTrackProcess.getInputStream();
        // Create a reader to read the output stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));

        // Read the output and print it to the console
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        // Get the process output (optional)
        int exitCode = eyeTrackProcess.waitFor();
    }

    public void startEyeTracking() throws IOException {
        System.out.println("Starting tracking");
        String pythonInterpreter = "python";
        String pythonScript;
        pythonScript = """
                import tobii_research as tr
                import time
                found_eyetrackers = tr.find_all_eyetrackers()
                my_eyetracker = found_eyetrackers[0]
                def gaze_data_callback(gaze_data):
                    message = f'{round(time.time() * 1000)}; ' \\
                              f'{gaze_data["left_gaze_point_on_display_area"][0]}, ' \\
                              f'{gaze_data["left_gaze_point_on_display_area"][1]}, ' \\
                              f'{gaze_data["left_gaze_point_validity"]}, ' \\
                              f'{gaze_data["left_pupil_diameter"]}, ' \\
                              f'{gaze_data["left_pupil_validity"]}; ' \\
                              f'{gaze_data["right_gaze_point_on_display_area"][0]}, ' \\
                              f'{gaze_data["right_gaze_point_on_display_area"][1]}, ' \\
                              f'{gaze_data["right_gaze_point_validity"]}, ' \\
                              f'{gaze_data["right_pupil_diameter"]}, ' \\
                              f'{gaze_data["right_pupil_validity"]}'
                    print(message)
                    sys.stdout.flush()
                my_eyetracker.subscribe_to(tr.EYETRACKER_GAZE_DATA, gaze_data_callback, as_dictionary=True)
                print('Subscribed to gaze data')
                time.sleep(100)
                """
        ;

        ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, "-c", pythonScript);
        pb.redirectErrorStream(true); // Redirect error stream to output stream
        eyeTrackProcess = pb.start();
        System.out.println("Started tracking");

        outputThread = new Thread(() -> {
            try (InputStream inputStream = eyeTrackProcess.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                System.out.println("Reading output");
                while ((line = bufferedReader.readLine()) != null) {
                    if (eyeDataHandler != null) {
                        System.out.println("Handling output");
                        handleDataOutput(processRawEyeData(line));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outputThread.start();
    }

    public void stopEyeTracking() {
        eyeTrackProcess.destroy();
    }


    private String processRawEyeData(String message) {
        String timestamp = message.split("; ")[0];

        String leftInfo = message.split("; ")[1];
        String leftGazePointX = leftInfo.split(", ")[0];
        String leftGazePointY = leftInfo.split(", ")[1];
        String leftGazeValidity = leftInfo.split(", ")[2];
        String leftPupilDiameter = leftInfo.split(", ")[3];
        String leftPupilValidity = leftInfo.split(", ")[4];

        String rightInfo = message.split("; ")[2];
        String rightGazePointX = rightInfo.split(", ")[0];
        String rightGazePointY = rightInfo.split(", ")[1];
        String rightGazeValidity = rightInfo.split(", ")[2];
        String rightPupilDiameter = rightInfo.split(", ")[3];
        String rightPupilValidity = rightInfo.split(", ")[4];

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = size.getWidth();
        double screenHeight = size.getHeight();
        int eyeX = (int) ((Double.parseDouble(leftGazePointX) + Double.parseDouble(rightGazePointX)) / 2 * screenWidth);
        int eyeY = (int) ((Double.parseDouble(leftGazePointY) + Double.parseDouble(rightGazePointY)) / 2 * screenHeight);
        int editorX = editor.getComponent().getLocationOnScreen().x;
        int editorY = editor.getComponent().getLocationOnScreen().y;
        int relativeX = eyeX - editorX;
        int relativeY = eyeY - editorY;
        return timestamp + ", " + relativeX + ", " + relativeY;
    }


    public void startRecordingScreen() throws IOException {
        screenRecorder.startRecording();
    }

    public void stopRecordingScreen() throws IOException {
        screenRecorder.stopRecording();
    }

    public void startIDETracking() throws ParserConfigurationException {
        ideTracker = IDETracker.getInstance();
        ideTracker.startTracking();
//        ideTracker.
    }

    public void stopIDETracking() {
//        ideTracker.stopTracking();
    }

    public void startMouseTracking(){

    }

    public void testAPI(){
//        Project project = null; // Set your project here

        // Get the main frame of the IDE
//        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
//        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
//        VirtualFile virtualFile = fileEditorManager.getSelectedFiles()[0];
//        Editor editor = fileEditorManager.getSelectedTextEditor();
//        System.out.println(editor);
//        System.out.println(virtualFile);
//        System.out.println(virtualFile.getPath());
//        System.out.println(virtualFile.getCanonicalPath());
//        System.out.println(virtualFile.getPresentableName());
//        System.out.println(virtualFile.getName());
    }

    public void stopMouseTracking(){

    }

}
