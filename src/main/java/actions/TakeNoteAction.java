package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBTextArea;
import components.NoteDialog;
import org.jdesktop.swingx.JXTextArea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TakeNoteAction extends AnAction {

    private String description = "Take Note";


    @Override
    public void update (@NotNull AnActionEvent e) {
        e.getPresentation().setText(description);
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        NoteDialog noteDialog = new NoteDialog(project);
        noteDialog.show();
    }

    public void setDescription(String description) {
        this.description = description;
    }
    @Override
    public @NotNull String getTemplateText() {
        return description;
    }
}
