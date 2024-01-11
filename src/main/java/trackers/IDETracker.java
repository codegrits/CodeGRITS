package trackers;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import utils.RelativePathGetter;
import utils.XMLWriter;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This class is the IDE tracker.
 */
public final class IDETracker implements Disposable {
    boolean isTracking = false;
    /**
     * This variable is the XML document for storing the tracking data.
     */
    Document iDETracking = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = iDETracking.createElement("ide_tracking");
    Element environment = iDETracking.createElement("environment");
    Element actions = iDETracking.createElement("actions");
    Element archives = iDETracking.createElement("archives");
    Element typings = iDETracking.createElement("typings");
    Element files = iDETracking.createElement("files");
    Element mouses = iDETracking.createElement("mouses");
    Element carets = iDETracking.createElement("carets");
    Element selections = iDETracking.createElement("selections");
    Element visibleAreas = iDETracking.createElement("visible_areas");
    String projectPath = "";
    String dataOutputPath = "";
    String lastSelectionInfo = "";

    /**
     * This variable indicates whether the data is transmitted in real time.
     */
    private static boolean isRealTimeDataTransmitting = false;
    /**
     * This variable is the handler for the IDE tracker data.
     */
    private Consumer<Element> ideTrackerDataHandler;

    /**
     * This variable is the document listener for the IDE tracker. When the document is changed, if the {@code EditorKind} is {@code CONSOLE}, the console output is archived. Otherwise, the {@code changedFilepath} and {@code changedFileText} are updated.
     */
    DocumentListener documentListener = new DocumentListener() {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            if (!isTracking) return;
            if (event.getDocument().getText().length() == 0) return;
            if (EditorFactory.getInstance().getEditors(event.getDocument()).length == 0) return;
            Editor currentEditor = EditorFactory.getInstance().getEditors(event.getDocument())[0];
            if (currentEditor != null && currentEditor.getEditorKind() == EditorKind.CONSOLE) {
                archiveFile("unknown", String.valueOf(System.currentTimeMillis()),
                        "", event.getDocument().getText());
                return;
            }
            VirtualFile changedFile = FileDocumentManager.getInstance().getFile(event.getDocument());
            if (changedFile != null) {
                changedFilepath = changedFile.getPath();
                changedFileText = event.getDocument().getText();
            }
        }
    };

    /**
     * This variable is the mouse listener for the IDE tracker.
     * When the mouse is pressed, clicked, or released, the mouse event is tracked.
     */
    EditorMouseListener editorMouseListener = new EditorMouseListener() {
        @Override
        public void mousePressed(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mousePressed");
            mouses.appendChild(mouseElement);
        }

        @Override
        public void mouseClicked(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mouseClicked");
            mouses.appendChild(mouseElement);
            handleElement(mouseElement);

        }

        @Override
        public void mouseReleased(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mouseReleased");
            mouses.appendChild(mouseElement);
            handleElement(mouseElement);

        }
    };

    /**
     * This variable is the mouse motion listener for the IDE tracker.
     * When the mouse is moved or dragged, the mouse event is tracked.
     */
    EditorMouseMotionListener editorMouseMotionListener = new EditorMouseMotionListener() {
        @Override
        public void mouseMoved(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mouseMoved");
            mouses.appendChild(mouseElement);
            handleElement(mouseElement);
        }

        @Override
        public void mouseDragged(@NotNull EditorMouseEvent e) {
            if (!isTracking) return;
            Element mouseElement = getMouseElement(e, "mouseDragged");
            mouses.appendChild(mouseElement);
            handleElement(mouseElement);
        }
    };

    /**
     * This variable is the caret listener for the IDE tracker.
     * When the caret position is changed, the caret event is tracked.
     */
    CaretListener caretListener = new CaretListener() {
        @Override
        public void caretPositionChanged(@NotNull CaretEvent e) {
            if (!isTracking) return;
            Element caretElement = iDETracking.createElement("caret");
            carets.appendChild(caretElement);
            caretElement.setAttribute("id", "caretPositionChanged");
            caretElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(e.getEditor().getDocument());
            caretElement.setAttribute("path", virtualFile != null ?
                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
            caretElement.setAttribute("line", String.valueOf(e.getNewPosition().line));
            caretElement.setAttribute("column", String.valueOf(e.getNewPosition().column));
            handleElement(caretElement);
        }
    };

    /**
     * This variable is the selection listener for the IDE tracker.
     * When the selection is changed, the selection event is tracked.
     */
    SelectionListener selectionListener = new SelectionListener() {
        @Override
        public void selectionChanged(@NotNull SelectionEvent e) {
            if (!isTracking) return;

            Element selectionElement = iDETracking.createElement("selection");
            selectionElement.setAttribute("id", "selectionChanged");
            selectionElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(e.getEditor().getDocument());
            selectionElement.setAttribute("path", virtualFile != null ?
                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
            LogicalPosition startLogicalPos = e.getEditor().offsetToLogicalPosition(e.getNewRange().getStartOffset());
            LogicalPosition endLogicalPos = e.getEditor().offsetToLogicalPosition(e.getNewRange().getEndOffset());
            selectionElement.setAttribute("start_position", startLogicalPos.line + ":" +
                    startLogicalPos.column);
            selectionElement.setAttribute("end_position", endLogicalPos.line + ":" +
                    endLogicalPos.column);
            selectionElement.setAttribute("selected_text", e.getEditor().getSelectionModel().getSelectedText());

            String currentSelectionInfo = selectionElement.getAttribute("path") + "-" +
                    selectionElement.getAttribute("start_position") + "-" +
                    selectionElement.getAttribute("end_position") + "-" +
                    selectionElement.getAttribute("selected_text");
            if (currentSelectionInfo.equals(lastSelectionInfo)) return;
            selections.appendChild(selectionElement);
            lastSelectionInfo = currentSelectionInfo;
            handleElement(selectionElement);
        }
    };

    /**
     * This variable is the visible area listener for the IDE tracker.
     * When the visible area is changed, the visible area event is tracked.
     */
    VisibleAreaListener visibleAreaListener = e -> {
        if (!isTracking) return;
        if (e.getEditor().getEditorKind() == EditorKind.MAIN_EDITOR) {
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(e.getEditor().getDocument());
            Element visibleAreaElement = iDETracking.createElement("visible_area");
            visibleAreas.appendChild(visibleAreaElement);
            visibleAreaElement.setAttribute("id", "visibleAreaChanged");
            visibleAreaElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
            visibleAreaElement.setAttribute("path", virtualFile != null ?
                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
            visibleAreaElement.setAttribute("x", String.valueOf(e.getEditor().getScrollingModel().getHorizontalScrollOffset()));
            visibleAreaElement.setAttribute("y", String.valueOf(e.getEditor().getScrollingModel().getVerticalScrollOffset()));
            visibleAreaElement.setAttribute("width", String.valueOf(e.getEditor().getScrollingModel().getVisibleArea().width));
            visibleAreaElement.setAttribute("height", String.valueOf(e.getEditor().getScrollingModel().getVisibleArea().height));
            handleElement(visibleAreaElement);
        }

    };

    /**
     * This variable is the editor event multicaster for the IDE tracker.
     * It is used to add and remove all the listeners.
     */
    EditorEventMulticaster editorEventMulticaster = EditorFactory.getInstance().getEventMulticaster();
    /**
     * This variable is the timer for tracking the document changes.
     */
    Timer timer = new Timer();
    String changedFilepath = "";
    String changedFileText = "";
    /**
     * This variable is the timer task for tracking the document changes. If the {@code changedFilepath} is not empty, the file is archived.
     */
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (changedFilepath.length() > 0) {
                if (!isTracking) return;
                archiveFile(changedFilepath, String.valueOf(System.currentTimeMillis()),
                        "contentChanged", changedFileText);
                changedFilepath = "";
            }
        }
    };

    /**
     * This constructor initializes the IDE tracker.
     */
    IDETracker() throws ParserConfigurationException {
        iDETracking.appendChild(root);
        root.appendChild(environment);

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        environment.setAttribute("screen_size", "(" + size.width + "," + size.height + ")");
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
        environment.setAttribute("scale_x", String.valueOf(config.getDefaultTransform().getScaleX()));
        environment.setAttribute("scale_y", String.valueOf(config.getDefaultTransform().getScaleY()));
        environment.setAttribute("os_name", System.getProperty("os.name"));
        environment.setAttribute("java_version", System.getProperty("java.version"));
        environment.setAttribute("ide_version", ApplicationInfo.getInstance().getFullVersion());
        environment.setAttribute("ide_name", ApplicationInfo.getInstance().getVersionName());

        root.appendChild(archives);
        root.appendChild(actions);
        root.appendChild(typings);
        root.appendChild(files);
        root.appendChild(mouses);
        root.appendChild(carets);
        root.appendChild(selections);
        root.appendChild(visibleAreas);

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(
                AnActionListener.TOPIC, new AnActionListener() {

                    @Override
                    public void beforeActionPerformed(@NotNull AnAction action, @NotNull AnActionEvent event) {
                        if (isTracking) {
                            Element actionElement = iDETracking.createElement("action");
                            actionElement.setAttribute("id", ActionManager.getInstance().getId(action));
                            actionElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
                            VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
                            actionElement.setAttribute("path", virtualFile != null ?
                                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
                            actions.appendChild(actionElement);
                            handleElement(actionElement);
                        }
                    }

                    @Override
                    public void beforeEditorTyping(char c, @NotNull DataContext dataContext) {
                        if (isTracking) {
                            Element typingElement = iDETracking.createElement("typing");
                            typings.appendChild(typingElement);
                            typingElement.setAttribute("character", String.valueOf(c));
                            typingElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
                            VirtualFile virtualFile = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE);
                            typingElement.setAttribute("path", virtualFile != null ?
                                    RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);

                            Editor editor = dataContext.getData(CommonDataKeys.EDITOR);
                            if (editor != null) {
                                Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
                                LogicalPosition logicalPos = primaryCaret.getLogicalPosition();
                                typingElement.setAttribute("line", String.valueOf(logicalPos.line));
                                typingElement.setAttribute("column", String.valueOf(logicalPos.column));
                            }
                            handleElement(typingElement);
                        }
                    }
                });

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {

                    @Override
                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        if (isTracking) {
                            Element fileElement = iDETracking.createElement("file");
                            files.appendChild(fileElement);
                            fileElement.setAttribute("id", "fileOpened");
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            fileElement.setAttribute("timestamp", timestamp);
                            fileElement.setAttribute("path",
                                    RelativePathGetter.getRelativePath(file.getPath(), projectPath));
                            archiveFile(file.getPath(), timestamp, "fileOpened", null);
                            handleElement(fileElement);
                        }
                    }

                    @Override
                    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        if (isTracking) {
                            Element fileElement = iDETracking.createElement("file");
                            files.appendChild(fileElement);
                            fileElement.setAttribute("id", "fileClosed");
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            fileElement.setAttribute("timestamp", timestamp);
                            fileElement.setAttribute("path",
                                    RelativePathGetter.getRelativePath(file.getPath(), projectPath));
                            archiveFile(file.getPath(), timestamp, "fileClosed", null);
                            handleElement(fileElement);
                        }
                    }

                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        if (isTracking) {
                            Element fileElement = iDETracking.createElement("file");
                            files.appendChild(fileElement);

                            fileElement.setAttribute("id", "selectionChanged");
                            fileElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
                            if (event.getOldFile() != null) {
                                fileElement.setAttribute("old_path",
                                        RelativePathGetter.getRelativePath(event.getOldFile().getPath(), projectPath));
                                archiveFile(event.getOldFile().getPath(), String.valueOf(System.currentTimeMillis()),
                                        "selectionChanged | OldFile", null);
                            }
                            if (event.getNewFile() != null) {
                                fileElement.setAttribute("new_path",
                                        RelativePathGetter.getRelativePath(event.getNewFile().getPath(), projectPath));
                                archiveFile(event.getNewFile().getPath(), String.valueOf(System.currentTimeMillis()),
                                        "selectionChanged | NewFile", null);
                            }
                            handleElement(fileElement);
                        }
                    }
                });

        timer.schedule(timerTask, 0, 1);
    }

    /**
     * This method returns the IDE tracker instance.
     *
     * @return The IDE tracker instance.
     */
    public static IDETracker getInstance() throws ParserConfigurationException {
        return new IDETracker();
    }

    /**
     * This method starts tracking. All the listeners are added.
     *
     * @param project The project.
     */
    public void startTracking(Project project) {
        isTracking = true;
        environment.setAttribute("project_path", projectPath);
        environment.setAttribute("project_name", projectPath.substring(
                projectPath.lastIndexOf('/') + 1));
        editorEventMulticaster.addDocumentListener(documentListener, () -> {
        });
        editorEventMulticaster.addEditorMouseListener(editorMouseListener, () -> {
        });
        editorEventMulticaster.addEditorMouseMotionListener(editorMouseMotionListener, () -> {
        });
        editorEventMulticaster.addCaretListener(caretListener, () -> {
        });
        editorEventMulticaster.addSelectionListener(selectionListener, () -> {
        });
        editorEventMulticaster.addVisibleAreaListener(visibleAreaListener, () -> {
        });
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (VirtualFile file : fileEditorManager.getOpenFiles()) {
            archiveFile(file.getPath(), String.valueOf(System.currentTimeMillis()), "fileOpened", null);
        }
    }

    /**
     * This method sets the handler for the IDE tracker data for real-time data transmission.
     *
     * @param ideTrackerDataHandler The handler for the IDE tracker data.
     */
    public void setIdeTrackerDataHandler(Consumer<Element> ideTrackerDataHandler) {
        this.ideTrackerDataHandler = ideTrackerDataHandler;
    }

    /**
     * This method stops tracking. All the listeners are removed. The tracking data is written to the XML file.
     */
    public void stopTracking() throws TransformerException {
        isTracking = false;
        editorEventMulticaster.removeDocumentListener(documentListener);
        editorEventMulticaster.removeEditorMouseListener(editorMouseListener);
        editorEventMulticaster.removeEditorMouseMotionListener(editorMouseMotionListener);
        editorEventMulticaster.removeCaretListener(caretListener);
        editorEventMulticaster.removeSelectionListener(selectionListener);
        editorEventMulticaster.removeVisibleAreaListener(visibleAreaListener);
        String filePath = dataOutputPath + "/ide_tracking.xml";
        XMLWriter.writeToXML(iDETracking, filePath);
    }

    /**
     * This method pauses tracking. The {@code isTracking} is set to false.
     */
    public void pauseTracking() {
        isTracking = false;
    }

    /**
     * This method resumes tracking. The {@code isTracking} is set to true.
     */
    public void resumeTracking() {
        isTracking = true;
    }

    /**
     * This method sets the {@code isRealTimeDataTransmitting} variable.
     *
     * @param isRealTimeDataTransmitting Indicates whether the data is transmitted in real time.
     */
    public void setIsRealTimeDataTransmitting(boolean isRealTimeDataTransmitting) {
        IDETracker.isRealTimeDataTransmitting = isRealTimeDataTransmitting;
    }

    @Override
    public void dispose() {
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    /**
     * This method archives the file. If the file is a code file, the file is copied to the archive folder.
     *
     * @param path      The path of the file.
     * @param timestamp The timestamp of the file.
     * @param remark    The remark of the file.
     * @param text      The text of the file.
     */
    public void archiveFile(String path, String timestamp, String remark, String text) {
        File srcFile = new File(path);
        File destFile = new File(dataOutputPath + "/archives/" + timestamp + ".archive");
        String[] codeExtensions = {".java", ".cpp", ".c", ".py", ".rb", ".js", ".md", ".cs", ".html", ".htm", ".css", ".php", ".ts", ".swift", ".go", ".kt", ".kts", ".rs", ".pl", ".sh", ".bat", ".ps1", ".asp", ".aspx", ".jsp", ".lua"};
        try {
            if (path.equals("unknown")) {
                FileUtils.writeStringToFile(destFile, text, "UTF-8", true);
            } else {
                if (Arrays.stream(codeExtensions).anyMatch(path::endsWith)) {
                    if (text == null) {
                        FileUtils.copyFile(srcFile, destFile);
                    } else {
                        FileUtils.writeStringToFile(destFile, text, "UTF-8", true);
                    }
                } else {
                    remark += " | NotCodeFile | Fail";
                }
            }
        } catch (IOException e) {
            remark += " | IOException | Fail";
        }

        Element archive = iDETracking.createElement("archive");
        archives.appendChild(archive);
        if (!path.equals("unknown")) {
            archive.setAttribute("id", "fileArchive");
        } else {
            archive.setAttribute("id", "consoleArchive");
        }
        archive.setAttribute("timestamp", timestamp);
        if (!path.equals("unknown")) {
            archive.setAttribute("path", RelativePathGetter.getRelativePath(path, projectPath));
            archive.setAttribute("remark", remark);
        }
    }

    /**
     * This method returns the mouse XML element.
     *
     * @param e  The editor mouse event.
     * @param id The id of the mouse event.
     * @return The mouse element.
     */
    public Element getMouseElement(EditorMouseEvent e, String id) {
        Element mouseElement = iDETracking.createElement("mouse");
        mouseElement.setAttribute("id", id);
        mouseElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(e.getEditor().getDocument());
        mouseElement.setAttribute("path", virtualFile != null ?
                RelativePathGetter.getRelativePath(virtualFile.getPath(), projectPath) : null);
        MouseEvent mouseEvent = e.getMouseEvent();
        mouseElement.setAttribute("x", String.valueOf(mouseEvent.getXOnScreen()));
        mouseElement.setAttribute("y", String.valueOf(mouseEvent.getYOnScreen()));
        return mouseElement;
    }

    /**
     * This method sets the data output path.
     *
     * @param dataOutputPath The data output path.
     */
    public void setDataOutputPath(String dataOutputPath) {
        this.dataOutputPath = dataOutputPath;
    }

    /**
     * This method handles the XML element for real-time data transmission.
     *
     * @param element The XML element.
     */
    private void handleElement(Element element) {
        if (ideTrackerDataHandler == null) {
            return;
//            throw new RuntimeException("ideTrackerDataHandler is null");
        }
        if (isRealTimeDataTransmitting) {
            ideTrackerDataHandler.accept(element);
        }
    }

    /**
     * This method returns whether the IDE tracker is tracking.
     *
     * @return Whether the IDE tracker is tracking.
     */
    public boolean isTracking() {
        return isTracking;
    }

}
