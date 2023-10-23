package trackers;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import utils.RelativePathGetter;
import utils.XMLWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;


public class EyeTracker implements Disposable {
    String dataOutputPath = "";
    double sampleFrequency;
    PsiDocumentManager psiDocumentManager;
    public Editor editor;
    Document eyeTracking = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = eyeTracking.createElement("eye_tracking");
    Element setting = eyeTracking.createElement("setting");
    Element gazes = eyeTracking.createElement("gazes");

    boolean isTracking = false;
    double screenWidth, screenHeight;
    String projectPath = "", filePath = "";
    PsiElement lastElement = null;
    Rectangle visibleArea = null;
    Process pythonProcess;
    Thread pythonOutputThread;
    String pythonInterpreter = "";
    String pythonScriptTobii;
    String pythonScriptMouse;
    int deviceIndex = 0;

    private static boolean isRealTimeDataTransmitting = false;
    private Consumer<Element> eyeTrackerDataHandler;

    public EyeTracker() throws ParserConfigurationException {

        eyeTracking.appendChild(root);
        root.appendChild(setting);
        root.appendChild(gazes);

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = size.getWidth();
        screenHeight = size.getHeight();

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                editor = source.getSelectedTextEditor();
                if (editor != null) {
                    editor.getScrollingModel().addVisibleAreaListener(visibleAreaListener);
                }
                filePath = file.getPath();
                visibleArea = editor.getScrollingModel().getVisibleArea();
            }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                editor = event.getManager().getSelectedTextEditor() != null ? event.getManager().getSelectedTextEditor() : editor;
                if (event.getNewFile() != null) {
                    if (editor != null) {
                        editor.getScrollingModel().addVisibleAreaListener(visibleAreaListener);
                    }
                    filePath = event.getNewFile().getPath();
                    visibleArea = editor.getScrollingModel().getVisibleArea();
                }
            }
        });

    }

    public EyeTracker(String pythonInterpreter, double sampleFrequency, boolean isUsingMouse) throws ParserConfigurationException {

//        if(isUsingMouse) {
//            deviceIndex = 0;
//        } else {
//            deviceIndex = 1;
//        }

        eyeTracking.appendChild(root);
        root.appendChild(setting);
        root.appendChild(gazes);

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = size.getWidth();
        screenHeight = size.getHeight();

        this.pythonInterpreter = pythonInterpreter;
        this.sampleFrequency = sampleFrequency;
        setPythonScriptMouse();
        setPythonScriptTobii();

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                editor = source.getSelectedTextEditor();
                if (editor != null) {
                    editor.getScrollingModel().addVisibleAreaListener(visibleAreaListener);
                }
                filePath = file.getPath();
                visibleArea = editor.getScrollingModel().getVisibleArea();
            }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                editor = event.getManager().getSelectedTextEditor() != null ? event.getManager().getSelectedTextEditor() : editor;
                if (event.getNewFile() != null) {
                    if (editor != null) {
                        editor.getScrollingModel().addVisibleAreaListener(visibleAreaListener);
                    }
                    filePath = event.getNewFile().getPath();
                    visibleArea = editor.getScrollingModel().getVisibleArea();
                }
            }
        });

    }

    VisibleAreaListener visibleAreaListener = e -> visibleArea = e.getNewRectangle();


    public void startTracking(Project project) throws IOException {
        isTracking = true;
        psiDocumentManager = PsiDocumentManager.getInstance(project);
        editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            editor.getScrollingModel().addVisibleAreaListener(visibleAreaListener);
            visibleArea = editor.getScrollingModel().getVisibleArea();
        }
        VirtualFile[] virtualFiles = FileEditorManager.getInstance(project).getSelectedFiles();
        if (virtualFiles.length > 0) {
            filePath = virtualFiles[0].getPath();
        }
        if (deviceIndex == 0 ) {
            setting.setAttribute("eye_tracker", "Mouse");
        } else {
            setting.setAttribute("eye_tracker", "Tobii Pro Fusion");
        }
        setting.setAttribute("sample_frequency", String.valueOf(sampleFrequency));
        track();
    }

    public void stopTracking() throws TransformerException {
        isTracking = false;
        pythonOutputThread.interrupt();
        pythonProcess.destroy();
        XMLWriter.writeToXML(eyeTracking, dataOutputPath + "/eye_tracking.xml");
    }

    public void pauseTracking() {
        isTracking = false;
    }

    public void resumeTracking() {
        isTracking = true;
    }

    public void processRawData(String message) {
        if (!isTracking) return;
        Element gaze = getRawGazeElement(message);
        gazes.appendChild(gaze);

        String leftInfo = message.split("; ")[1];
        String leftGazePointX = leftInfo.split(", ")[0];
        String leftGazePointY = leftInfo.split(", ")[1];

        String rightInfo = message.split("; ")[2];
        String rightGazePointX = rightInfo.split(", ")[0];
        String rightGazePointY = rightInfo.split(", ")[1];

        if (leftGazePointX.equals("nan") || leftGazePointY.equals("nan") || rightGazePointX.equals("nan") || rightGazePointY.equals("nan")) {
            gaze.setAttribute("remark", "Fail | Invalid Gaze Point");
            return;
        }

        if (editor == null) {
            gaze.setAttribute("remark", "Fail | No Editor");
            return;
        }

        int eyeX = (int) ((Double.parseDouble(leftGazePointX) + Double.parseDouble(rightGazePointX)) / 2 * screenWidth);
        int eyeY = (int) ((Double.parseDouble(leftGazePointY) + Double.parseDouble(rightGazePointY)) / 2 * screenHeight);

        int editorX, editorY;
        try {
            editorX = editor.getContentComponent().getLocationOnScreen().x;
            editorY = editor.getContentComponent().getLocationOnScreen().y;
        } catch (IllegalComponentStateException e) {
            gaze.setAttribute("remark", "Fail | No Editor");
            return;
        }
        int relativeX = eyeX - editorX;
        int relativeY = eyeY - editorY;
        if ((relativeX - visibleArea.x) < 0 || (relativeY - visibleArea.y) < 0
                || (relativeX - visibleArea.x) > visibleArea.width || (relativeY - visibleArea.y) > visibleArea.height) {
            gaze.setAttribute("remark", "Fail | Out of Text Editor");
            return;
        }

        Point relativePoint = new Point(relativeX, relativeY);

        EventQueue.invokeLater(new Thread(() -> {
            PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
            LogicalPosition logicalPosition = editor.xyToLogicalPosition(relativePoint);
            if (psiFile != null) {
                int offset = editor.logicalPositionToOffset(logicalPosition);
                PsiElement psiElement = psiFile.findElementAt(offset);
                Element location = eyeTracking.createElement("location");
                location.setAttribute("x", String.valueOf(eyeX));
                location.setAttribute("y", String.valueOf(eyeY));
                location.setAttribute("line", String.valueOf(logicalPosition.line));
                location.setAttribute("column", String.valueOf(logicalPosition.column));
                location.setAttribute("path", RelativePathGetter.getRelativePath(filePath, projectPath));
                gaze.appendChild(location);
                Element aSTStructure = getASTStructureElement(psiElement);
                gaze.appendChild(aSTStructure);
                lastElement = psiElement;
                System.out.println(gaze.getAttribute("timestamp") + " " + System.currentTimeMillis());
                handleElement(gaze);
            }
        }));
    }

    public void track() {
        try {
            ProcessBuilder processBuilder;
            if (deviceIndex == 0) {
                processBuilder = new ProcessBuilder(pythonInterpreter, "-c", pythonScriptMouse);
            } else {
                processBuilder = new ProcessBuilder(pythonInterpreter, "-c", pythonScriptTobii);
            }
            processBuilder.redirectErrorStream(true);
            pythonProcess = processBuilder.start();

            pythonOutputThread = new Thread(() -> {
                try (InputStream inputStream = pythonProcess.getInputStream();
                     InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                     BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        processRawData(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            pythonOutputThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public void dispose() {
    }

    public Element getRawGazeElement(String message) {
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

        Element rawGaze = eyeTracking.createElement("gaze");
        Element leftEye = eyeTracking.createElement("left_eye");
        Element rightEye = eyeTracking.createElement("right_eye");

        rawGaze.appendChild(leftEye);
        rawGaze.appendChild(rightEye);

        rawGaze.setAttribute("timestamp", timestamp);

        leftEye.setAttribute("gaze_point_x", leftGazePointX);
        leftEye.setAttribute("gaze_point_y", leftGazePointY);
        leftEye.setAttribute("gaze_validity", leftGazeValidity);
        leftEye.setAttribute("pupil_diameter", leftPupilDiameter);
        leftEye.setAttribute("pupil_validity", leftPupilValidity);

        rightEye.setAttribute("gaze_point_x", rightGazePointX);
        rightEye.setAttribute("gaze_point_y", rightGazePointY);
        rightEye.setAttribute("gaze_validity", rightGazeValidity);
        rightEye.setAttribute("pupil_diameter", rightPupilDiameter);
        rightEye.setAttribute("pupil_validity", rightPupilValidity);

        return rawGaze;
    }

    public Element getASTStructureElement(PsiElement psiElement) {
        String token = "", type = "";
        Element aSTStructure = eyeTracking.createElement("ast_structure");
        if (psiElement != null && psiElement.getTextLength() > 0) {
            token = psiElement.getText();
            type = psiElement.getNode().getElementType().toString();
        }
        aSTStructure.setAttribute("token", token);
        aSTStructure.setAttribute("type", type);
        if (psiElement != null && psiElement.equals(lastElement)) {
            aSTStructure.setAttribute("remark", "Same (Last Successful AST)");
            return aSTStructure;
        }
        PsiElement parent = psiElement;
        while (parent != null) {
            if (parent instanceof PsiFile) {
                break;
            }
            Element level = eyeTracking.createElement("level");
            aSTStructure.appendChild(level);
            level.setAttribute("tag", String.valueOf(parent));
            LogicalPosition startLogicalPosition = editor.offsetToLogicalPosition(parent.getTextRange().getStartOffset());
            LogicalPosition endLogicalPosition = editor.offsetToLogicalPosition(parent.getTextRange().getEndOffset());
            level.setAttribute("start", startLogicalPosition.line + ":" + startLogicalPosition.column);
            level.setAttribute("end", endLogicalPosition.line + ":" + endLogicalPosition.column);
            parent = parent.getParent();
        }
        return aSTStructure;
    }

    private void handleElement(Element element) {
        if (eyeTrackerDataHandler != null && isRealTimeDataTransmitting) {
            eyeTrackerDataHandler.accept(element);
        } else if (eyeTrackerDataHandler == null) {
//            throw new RuntimeException("eyeTrackerDataHandler is null");
        }
    }

    public static void setIsRealTimeDataTransmitting(boolean isRealTimeDataTransmitting) {
        EyeTracker.isRealTimeDataTransmitting = isRealTimeDataTransmitting;
    }

    public void setEyeTrackerDataHandler(Consumer<Element> eyeTrackerDataHandler) {
        this.eyeTrackerDataHandler = eyeTrackerDataHandler;
    }

    public void setPythonInterpreter(String pythonInterpreter) {
        this.pythonInterpreter = pythonInterpreter;
    }

    public void setDataOutputPath(String dataOutputPath) {
        this.dataOutputPath = dataOutputPath;
    }

    public void setSampleFrequency(double sampleFrequency) {
        this.sampleFrequency = sampleFrequency;
    }

    public void setPythonScriptTobii() {
        pythonScriptTobii = "freq = " + sampleFrequency + "\n" + """
                import tobii_research as tr
                import time
                import sys
                import math
                            
                            
                def gaze_data_callback(gaze_data):
                    message = '{}; {}, {}, {}, {}, {}; {}, {}, {}, {}, {}'.format(
                        round(time.time() * 1000),
                        gaze_data['left_gaze_point_on_display_area'][0],
                        gaze_data['left_gaze_point_on_display_area'][1],
                        gaze_data['left_gaze_point_validity'],
                        gaze_data['left_pupil_diameter'],
                        gaze_data['left_pupil_validity'],
                        gaze_data['right_gaze_point_on_display_area'][0],
                        gaze_data['right_gaze_point_on_display_area'][1],
                        gaze_data['right_gaze_point_validity'],
                        gaze_data['right_pupil_diameter'],
                        gaze_data['right_pupil_validity']
                    )
                    print(message)
                    sys.stdout.flush()
                            
                found_eyetrackers = tr.find_all_eyetrackers()
                my_eyetracker = found_eyetrackers[0]
                my_eyetracker.set_gaze_output_frequency(freq)
                my_eyetracker.subscribe_to(tr.EYETRACKER_GAZE_DATA, gaze_data_callback, as_dictionary=True)
                start_time = time.time()
                while time.time() - start_time <= math.inf:
                    continue
                """;
    }

    public void setPythonScriptMouse() {
        pythonScriptMouse = "freq = " + sampleFrequency + "\n" + """
                import pyautogui
                from screeninfo import get_monitors
                import time
                import sys
                import math
                            
                width, height = get_monitors()[0].width, get_monitors()[0].height
                start_time = time.time()
                last_time = start_time
                            
                while time.time() - start_time <= math.inf:
                    current_time = time.time()
                    if current_time - last_time > 1 / freq:
                        message = f'{round(current_time * 1000)}; ' \\
                                  f'{pyautogui.position().x / width}, {pyautogui.position().y / height}, 1.0, 0, 0.0; ' \\
                                  f'{pyautogui.position().x / width}, {pyautogui.position().y / height}, 1.0, 0, 0.0'
                        print(message)
                        last_time = current_time
                        sys.stdout.flush()
                """;
    }

    public void setDeviceIndex(int deviceIndex) {
        this.deviceIndex = deviceIndex;
    }
}