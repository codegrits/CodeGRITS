import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
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
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.xml.parsers.DocumentBuilderFactory;


public final class IDETracker implements @NotNull Disposable {
    boolean isTracking = false;
    Document iDETracking = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = iDETracking.createElement("ide_tracking");
    Element environment = iDETracking.createElement("environment");
    Element behaviors = iDETracking.createElement("behaviors");
    Element logs = iDETracking.createElement("logs");
    String projectPath = "";

    DocumentListener documentListener = new DocumentListener() {
        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            VirtualFile changedFile = FileDocumentManager.getInstance().getFile(event.getDocument());
            if (changedFile != null) {
                changedFilepath = changedFile.getPath();
                changedFileText = event.getDocument().getText();
            } else {
                logFile("unknown", String.valueOf(System.currentTimeMillis()),
                        "contentChanged | Console (Possible)", event.getNewFragment().toString());
            }
        }
    };

    EditorEventMulticaster editorEventMulticaster = EditorFactory.getInstance().getEventMulticaster();

    Timer timer = new Timer();
    String changedFilepath = "";
    String changedFileText = "";
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (changedFilepath.length() > 0) {
                logFile(changedFilepath, String.valueOf(System.currentTimeMillis()), "contentChanged", changedFileText);
                changedFilepath = "";
            }
        }
    };

    IDETracker() throws ParserConfigurationException {
        iDETracking.appendChild(root);
        root.appendChild(environment);
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

        environment.setAttribute("screen_width", String.valueOf(size.getWidth()));
        environment.setAttribute("screen_height", String.valueOf(size.getHeight()));
        environment.setAttribute("ide_version", ApplicationInfo.getInstance().getFullVersion());
        environment.setAttribute("ide_name", ApplicationInfo.getInstance().getVersionName());

        root.appendChild(logs);
        root.appendChild(behaviors);

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
                            behaviors.appendChild(actionElement);
                        }
                    }

                    @Override
                    public void beforeEditorTyping(char c, @NotNull DataContext dataContext) {
                        if (isTracking) {
                            Element typingElement = iDETracking.createElement("typing");
                            behaviors.appendChild(typingElement);
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
                        }
                    }
                });

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {

                    @Override
                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        if (isTracking) {
                            Element fileElement = iDETracking.createElement("file");
                            behaviors.appendChild(fileElement);
                            fileElement.setAttribute("id", "fileOpened");
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            fileElement.setAttribute("timestamp", timestamp);
                            fileElement.setAttribute("path",
                                    RelativePathGetter.getRelativePath(file.getPath(), projectPath));
                            logFile(file.getPath(), timestamp, "fileOpened", null);
                        }
                    }

                    @Override
                    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        if (isTracking) {
                            Element fileElement = iDETracking.createElement("file");
                            behaviors.appendChild(fileElement);
                            fileElement.setAttribute("id", "fileClosed");
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            fileElement.setAttribute("timestamp", timestamp);
                            fileElement.setAttribute("path",
                                    RelativePathGetter.getRelativePath(file.getPath(), projectPath));
                            logFile(file.getPath(), timestamp, "fileClosed", null);
                        }
                    }

                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        if (isTracking) {
                            Element fileElement = iDETracking.createElement("file");
                            behaviors.appendChild(fileElement);
                            fileElement.setAttribute("id", "selectionChanged");
                            fileElement.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
                            if (event.getOldFile() != null) {
                                fileElement.setAttribute("old_path",
                                        RelativePathGetter.getRelativePath(event.getOldFile().getPath(), projectPath));
                                logFile(event.getOldFile().getPath(), String.valueOf(System.currentTimeMillis()),
                                        "selectionChanged | OldFile", null);
                            }
                            if (event.getNewFile() != null) {
                                fileElement.setAttribute("new_path",
                                        RelativePathGetter.getRelativePath(event.getNewFile().getPath(), projectPath));
                                logFile(event.getNewFile().getPath(), String.valueOf(System.currentTimeMillis()),
                                        "selectionChanged | NewFile", null);
                            }
                        }
                    }
                });

        timer.schedule(timerTask, 0, 1);
    }

    public static IDETracker getInstance() throws ParserConfigurationException {
        return new IDETracker();
    }

    public void startTracking(Project project) {
        isTracking = true;
        environment.setAttribute("project_path", projectPath);
        environment.setAttribute("project_name", projectPath.substring(
                projectPath.lastIndexOf('/') + 1));
        editorEventMulticaster.addDocumentListener(documentListener, ApplicationManager.getApplication());

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (VirtualFile file : fileEditorManager.getOpenFiles()) {
            logFile(file.getPath(), String.valueOf(System.currentTimeMillis()), "fileOpened", null);
        }
    }

    public void stopTracking() throws TransformerException {
        isTracking = false;
        TypingProcessor typingProcessor = new TypingProcessor(iDETracking);
        typingProcessor.process();
        String filePath = projectPath + "/ide_tracking_" + System.currentTimeMillis() + ".xml";
        XMLWriter.writeToXML(iDETracking, filePath);
        editorEventMulticaster.removeDocumentListener(documentListener);
    }

    @Override
    public void dispose() {
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public void logFile(String path, String timestamp, String remark, String text) {
        File srcFile = new File(path);
        File destFile = new File(projectPath + "/logs/" + timestamp + ".log");
        String[] codeExtensions = {".java", ".cpp", ".c", ".py", ".rb", ".js", ".md"};
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

        Element log = iDETracking.createElement("log");
        logs.appendChild(log);
        log.setAttribute("id", "fileLog");
        log.setAttribute("timestamp", timestamp);
        log.setAttribute("path", RelativePathGetter.getRelativePath(path, projectPath));
        log.setAttribute("remark", remark);
    }

}
