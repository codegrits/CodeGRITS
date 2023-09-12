package actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import components.ConfigDialog;
import org.jetbrains.annotations.NotNull;

public class ConfigAction extends AnAction {
    private ConfigDialog configDialog;

    private static boolean isEnabled = true;

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(isEnabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        configDialog = new ConfigDialog(project);
        configDialog.show();
    }

    public static void setIsEnabled(boolean isEnabled) {
        ConfigAction.isEnabled = isEnabled;
    }
}
