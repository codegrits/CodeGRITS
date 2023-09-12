package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import component.NoteDialog;
import org.jetbrains.annotations.NotNull;

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
