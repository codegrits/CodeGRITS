package actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class AddLabelAction extends AnAction {

    private String description;
    private static boolean isEnabled = false;

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(description);
        e.getPresentation().setEnabled(isEnabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Notification notification = new Notification("CodeGRITS Notification Group", "Add label",
                "Successfully add label \"" + description + "\"!", NotificationType.INFORMATION);
        notification.notify(e.getProject());
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static void setIsEnabled(boolean isEnabled) {
        AddLabelAction.isEnabled = isEnabled;
    }

    @Override
    public @NotNull String getTemplateText() {
        return description;
    }
}
