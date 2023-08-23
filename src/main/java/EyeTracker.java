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
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


// this class is responsible for detecting the eye gaze points and writing them to an xml file
public class EyeTracker implements Disposable {

    PsiDocumentManager psiDocumentManager;
    Editor editor;
    Document eyeTrackerDetection = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element rootElement = eyeTrackerDetection.createElement("eye_tracker_detector");
    Element environment = eyeTrackerDetection.createElement("environment");
    Element gazes = eyeTrackerDetection.createElement("gazes");
    Element scrolls = eyeTrackerDetection.createElement("scrolls");
    boolean isDetecting = false;
    double screenWidth, screenHeight;
    String projectPath = "", filePath = "";
    PsiElement lastElement = null;
    int lastVerticalScrollOffset = 0;
    int lastHorizontalScrollOffset = 0;

    public EyeTracker() throws ParserConfigurationException {

        eyeTrackerDetection.appendChild(rootElement);
        // environment
        rootElement.appendChild(environment);
        Dimension size = new Dimension(1536, 864);
//        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = size.getWidth();
        screenHeight = size.getHeight();
        environment.setAttribute("screen_width", String.valueOf(size.getWidth()));
        environment.setAttribute("screen_height", String.valueOf(size.getHeight()));
        environment.setAttribute("plugin_type", "IntelliJ");

        // gazes & scrolls
        rootElement.appendChild(gazes);
        rootElement.appendChild(scrolls);

        // track editor changes (file opened or selection changed)
        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {

                    @Override
                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        psiDocumentManager = PsiDocumentManager.getInstance(source.getProject());
                        editor = source.getSelectedTextEditor();
                        filePath = file.getPath();
                    }

                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        editor = event.getManager().getSelectedTextEditor() != null
                                ? event.getManager().getSelectedTextEditor() : editor;
                        if (event.getNewFile() != null) {
                            filePath = event.getNewFile().getPath();
                        }
                    }
                });

    }

    public void startDetection() throws IOException {
        isDetecting = true;
        environment.setAttribute("project_path", projectPath);
        environment.setAttribute("project_name", projectPath.substring(
                projectPath.lastIndexOf('/') + 1));
        detect();
    }

    public void stopDetection() throws TransformerException {
        isDetecting = false;
        XMLWriter.writeToXML(eyeTrackerDetection, projectPath +
                "/eyeTracker_" + System.currentTimeMillis() + ".xml");
    }

    public void detect() {
        try {
            // connect to the eye tracker python server and collect the gaze points and other information
            Socket soc = new Socket("localhost", 12345);
            DataInputStream in = new DataInputStream(soc.getInputStream());
            String msg = in.readUTF();
            while (isDetecting && !msg.equals("End") && editor != null) {
                int eyeX = (int) (Double.parseDouble(msg.split(", ")[0]) * screenWidth);
                int eyeY = (int) (Double.parseDouble(msg.split(", ")[1]) * screenHeight);
                String timestamp = msg.split(", ")[2];
                String gazeValidity = msg.split(", ")[3];
                String pupilValidity = msg.split(", ")[4];
                String leftPupilDiameter = msg.split(", ")[5];
                String rightPupilDiameter = msg.split(", ")[6];

                // convert x, y coordinates to relevant logical positions
                int editorX, editorY, width, height;
                try {
                    editorX = editor.getContentComponent().getLocationOnScreen().x;
                    editorY = editor.getContentComponent().getLocationOnScreen().y;
                    width = editor.getContentComponent().getWidth();
                    height = editor.getContentComponent().getHeight();
                } catch (IllegalComponentStateException e) {
                    msg = in.readUTF();
                    continue;
                }
                int relativeX = eyeX - editorX;
                int relativeY = eyeY - editorY;
                if (relativeX < 0 || relativeY < 0 || relativeX > width || relativeY > height
                        || !filePath.endsWith(".java")) {
                    msg = in.readUTF();
                    continue;
                }
                Point relativePoint = new Point(relativeX, relativeY);

                // use one new thread to process the conversion and write the gaze point to the xml file
                EventQueue.invokeLater(new Thread(() -> {
                    PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
                    Element gaze = eyeTrackerDetection.createElement("gaze");
                    gazes.appendChild(gaze);
                    LogicalPosition logicalPosition = editor.xyToLogicalPosition(relativePoint);
                    if (psiFile != null) {
                        int offset = editor.logicalPositionToOffset(logicalPosition);
                        PsiElement psiElement = psiFile.findElementAt(offset);
                        if (filePath.endsWith(".java")) {
                            Element sourceCodeEntity = extractSourceCodeEntity(psiElement);
                            gaze.appendChild(sourceCodeEntity);
                        }
                        lastElement = psiElement;
                    }
                    gaze.setAttribute("timestamp", timestamp);
                    gaze.setAttribute("x", String.valueOf(eyeX));
                    gaze.setAttribute("y", String.valueOf(eyeY));
                    gaze.setAttribute("line", String.valueOf(logicalPosition.line));
                    gaze.setAttribute("column", String.valueOf(logicalPosition.column));
                    gaze.setAttribute("path", getPath(filePath));
                    gaze.setAttribute("gaze_validity", gazeValidity);
                    gaze.setAttribute("pupil_validity", pupilValidity);
                    gaze.setAttribute("left_pupil_diameter", leftPupilDiameter);
                    gaze.setAttribute("right_pupil_diameter", rightPupilDiameter);

                    // detect scrolling
                    int verticalScrollOffset = editor.getScrollingModel().getVerticalScrollOffset();
                    int horizontalScrollOffset = editor.getScrollingModel().getHorizontalScrollOffset();
                    if (verticalScrollOffset != lastVerticalScrollOffset) {
                        Element scroll = eyeTrackerDetection.createElement("scroll");
                        scrolls.appendChild(scroll);
                        scroll.setAttribute("timestamp", timestamp);
                        scroll.setAttribute("path", getPath(filePath));
                        scroll.setAttribute("type", "vertical");
                        scroll.setAttribute("offset", String.valueOf(verticalScrollOffset));
                        lastVerticalScrollOffset = verticalScrollOffset;
                    }
                    if (horizontalScrollOffset != lastHorizontalScrollOffset) {
                        Element scroll = eyeTrackerDetection.createElement("scroll");
                        scrolls.appendChild(scroll);
                        scroll.setAttribute("timestamp", timestamp);
                        scroll.setAttribute("path", getPath(filePath));
                        scroll.setAttribute("type", "horizontal");
                        scroll.setAttribute("offset", String.valueOf(horizontalScrollOffset));
                        lastHorizontalScrollOffset = horizontalScrollOffset;
                    }
                }));
                msg = in.readUTF();
            }
            soc.close();
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

    public String getPath(String originPath) {
        String path;
        if (originPath.substring(0, projectPath.length()).equals(projectPath)) {
            path = originPath.substring(projectPath.length());
        } else {
            path = originPath;
        }
        return path;
    }

    public Element extractSourceCodeEntity(PsiElement psiElement) {
        String token = "", astType = "";
        Element sourceCodeEntity = eyeTrackerDetection.createElement("source_code_entity");
        if (psiElement != null && psiElement.getTextLength() > 0) {
            token = psiElement.getText();
            astType = psiElement.getNode().getElementType().toString();
        }
        sourceCodeEntity.setAttribute("token", token);
        sourceCodeEntity.setAttribute("ast_type", astType);
        if (psiElement != null && psiElement.equals(lastElement)) {
            sourceCodeEntity.setAttribute("info", "same");
            return sourceCodeEntity;
        }
        PsiElement parent = psiElement;
        while (parent != null) {
            if (parent instanceof PsiFile) {
                break;
            }
            if (parent instanceof PsiMethod) {
                parent = parent.getParent();
                continue;
            }
            Element level = eyeTrackerDetection.createElement("level");
            sourceCodeEntity.appendChild(level);
            level.setAttribute("tag", String.valueOf(parent));
            LogicalPosition startLogicalPosition = editor.offsetToLogicalPosition(parent.getTextRange().getStartOffset());
            LogicalPosition endLogicalPosition = editor.offsetToLogicalPosition(parent.getTextRange().getEndOffset());
            level.setAttribute("start", startLogicalPosition.line + ":" + startLogicalPosition.column);
            level.setAttribute("end", endLogicalPosition.line + ":" + endLogicalPosition.column);
            parent = parent.getParent();
        }
        return sourceCodeEntity;
    }
}