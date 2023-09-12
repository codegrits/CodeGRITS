package tracker;

import action.ConfigAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
//import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.*;


public class EyeTracker implements Disposable {

    PsiDocumentManager psiDocumentManager;
    public Editor editor;
    Document eyeTracking = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = eyeTracking.createElement("eye_tracking");
    Element gazes = eyeTracking.createElement("gazes");

    boolean isTracking = false;
    double screenWidth, screenHeight;
    String projectPath = "", filePath = "";
    PsiElement lastElement = null;

    Process pythonProcess;
    Thread pythonOutputThread;
    String pythonInterpreter = "D:\\ProgramData\\Anaconda3\\python.exe";
    String pythonScriptTobii = """
            import tobii_research as tr
            import time
            import sys
            import math
                        
                        
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
                        
                        
            found_eyetrackers = tr.find_all_eyetrackers()
            my_eyetracker = found_eyetrackers[0]
            my_eyetracker.subscribe_to(tr.EYETRACKER_GAZE_DATA, gaze_data_callback, as_dictionary=True)
            time.sleep(math.inf)
            """;

    String pythonScriptMouse = """
            import pyautogui
            from screeninfo import get_monitors
            import time
            import sys
                        
            width, height = get_monitors()[0].width, get_monitors()[0].height
            start_time = time.time()
                        
            while time.time() - start_time <= 100000:
                message = f'{round(time.time() * 1000)}; ' \\
                          f'{pyautogui.position().x / width}, {pyautogui.position().y / height}, 0, 0, 0; ' \\
                          f'{pyautogui.position().x / width}, {pyautogui.position().y / height}, 0, 0, 0'
                print(message)
                sys.stdout.flush()
                time.sleep(0.001)
            """;

    public EyeTracker() throws ParserConfigurationException {

        eyeTracking.appendChild(root);
        root.appendChild(gazes);

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = size.getWidth();
        screenHeight = size.getHeight();

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                psiDocumentManager = PsiDocumentManager.getInstance(source.getProject());
                editor = source.getSelectedTextEditor();
                filePath = file.getPath();
            }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                editor = event.getManager().getSelectedTextEditor() != null ? event.getManager().getSelectedTextEditor() : editor;
                if (event.getNewFile() != null) {
                    filePath = event.getNewFile().getPath();
                }
            }
        });

    }

    public void startTracking() throws IOException {
        isTracking = true;
        track();
    }

    public void stopTracking() throws TransformerException {
        isTracking = false;
        pythonOutputThread.interrupt();
        pythonProcess.destroy();
        System.out.println("Python Process Stopped");
        XMLWriter.writeToXML(eyeTracking, projectPath + "/eyeTracker_" + System.currentTimeMillis() + ".xml");
    }

    public void processRawData(String message) {
        if (editor == null || !isTracking) {
            return;
        }
        Element gaze = getRawGazeElement(message);
        gazes.appendChild(gaze);

        String leftInfo = message.split("; ")[1];
        String leftGazePointX = leftInfo.split(", ")[0];
        String leftGazePointY = leftInfo.split(", ")[1];

        String rightInfo = message.split("; ")[2];
        String rightGazePointX = rightInfo.split(", ")[0];
        String rightGazePointY = rightInfo.split(", ")[1];

        if (leftGazePointX.equals("nan") || leftGazePointY.equals("nan") || rightGazePointX.equals("nan") || rightGazePointY.equals("nan")) {
            gaze.setAttribute("remark", "Fail");
            return;
        }
        int eyeX = (int) ((Double.parseDouble(leftGazePointX) + Double.parseDouble(rightGazePointX)) / 2 * screenWidth);
        int eyeY = (int) ((Double.parseDouble(leftGazePointY) + Double.parseDouble(rightGazePointY)) / 2 * screenHeight);

        // TODO: Simulate Mouse Positions
        // int mouseX = MouseInfo.getPointerInfo().getLocation().x;
        // int mouseY = MouseInfo.getPointerInfo().getLocation().y;

        int editorX, editorY, width, height;
        try {
            editorX = editor.getContentComponent().getLocationOnScreen().x;
            editorY = editor.getContentComponent().getLocationOnScreen().y;
            width = editor.getContentComponent().getWidth();
            height = editor.getContentComponent().getHeight();
        } catch (IllegalComponentStateException e) {
            gaze.setAttribute("remark", "Fail");
            return;
        }
        int relativeX = eyeX - editorX;
        int relativeY = eyeY - editorY;
        if (relativeX < 0 || relativeY < 0 || relativeX > width || relativeY > height || !filePath.endsWith(".java")) {
            gaze.setAttribute("remark", "Fail");
            return;
        }
        Point relativePoint = new Point(relativeX, relativeY);

        EventQueue.invokeLater(new Thread(() -> {
            PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
            LogicalPosition logicalPosition = editor.xyToLogicalPosition(relativePoint);
            if (psiFile != null) {
                int offset = editor.logicalPositionToOffset(logicalPosition);
                PsiElement psiElement = psiFile.findElementAt(offset);
                if (filePath.endsWith(".java")) {
                    Element aSTStructure = getASTStructureElement(psiElement);
                    aSTStructure.setAttribute("x", String.valueOf(eyeX));
                    aSTStructure.setAttribute("y", String.valueOf(eyeY));
                    aSTStructure.setAttribute("line", String.valueOf(logicalPosition.line));
                    aSTStructure.setAttribute("column", String.valueOf(logicalPosition.column));
                    aSTStructure.setAttribute("path", RelativePathGetter.getRelativePath(filePath, projectPath));
                    gaze.appendChild(aSTStructure);
                }
                lastElement = psiElement;
                System.out.println(gaze.getAttribute("timestamp") + " " + System.currentTimeMillis());
            }
        }));
    }

    public void track() {
        try {

            ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreter, "-c", pythonScriptMouse);
            processBuilder.redirectErrorStream(true);
            pythonProcess = processBuilder.start();

            System.out.println("Python Process Started");

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
}