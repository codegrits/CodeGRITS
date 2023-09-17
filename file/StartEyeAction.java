package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StartEyeAction extends AnAction {

    private boolean isTracking = false;
    private Process p;
    private Thread outputThread;
//    private static Consumer<String> outputHandler;

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (isTracking) {
            e.getPresentation().setText("Stop Eye Tracking");
        } else {
            e.getPresentation().setText("Start Eye Tracking");
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            callPythonScript();
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void callPythonScript() throws IOException, InterruptedException {
        String pythonInterpreter = "python";
        String pythonScript;
        if (!isTracking) {
            isTracking = true;
            pythonScript = """
                    import tobii_research as tr
                    import time
                    found_eyetrackers = tr.find_all_eyetrackers()
                    my_eyetracker = found_eyetrackers[0]
                    def gaze_data_callback(gaze_data):
                        print(gaze_data)
                    my_eyetracker.subscribe_to(tr.EYETRACKER_GAZE_DATA, gaze_data_callback, as_dictionary=True)
                    print('Subscribed to gaze data')
                    time.sleep(100)
                    """
            ;

        } else {
            isTracking = false;
            p.destroy();
            ConfigAction.setIsEnabled(true);
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, "-c", pythonScript);
        pb.redirectErrorStream(true); // Redirect error stream to output stream
        p = pb.start();
        ConfigAction.setIsEnabled(false);

        outputThread = new Thread(() -> {
            try (InputStream inputStream = p.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {


                String line;
                while ((line = bufferedReader.readLine()) != null) {
//                    if(OutputHandler.getOutputHandler() != null){
//                        OutputHandler.handleOutput(line);
//                    }
//                    if(outputHandler != null){
//                        outputHandler.accept(line);
//                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outputThread.start();


    }

//    public static void addOutputHandler(Consumer<String> handler) {
//        outputHandler = handler;
//    }

//    public void outputHandler(String line){
//        System.out.println(line);
//    }


}
