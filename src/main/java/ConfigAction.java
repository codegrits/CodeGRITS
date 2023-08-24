import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import components.ConfigDialog;
import org.jetbrains.annotations.NotNull;

public class ConfigAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        ConfigDialog configDialog = new ConfigDialog(project);
        configDialog.show();
    }
}
