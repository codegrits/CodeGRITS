package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import component.ConfigDialog;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ConfigAction extends AnAction {

    private static boolean isEnabled = true;

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isEnabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        ConfigDialog configDialog;
        try {
            configDialog = new ConfigDialog(project);
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        configDialog.show();
    }

    public static void setIsEnabled(boolean isEnabled) {
        ConfigAction.isEnabled = isEnabled;
    }
}
