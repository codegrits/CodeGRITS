package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TakeNoteAction extends AnAction {

    private String description;
    private static boolean isEnabled = false;

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(description);
        e.getPresentation().setEnabled(isEnabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JOptionPane.showMessageDialog(null, "Successfully Take Note \"" + description + "\"!");
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static void setIsEnabled(boolean isEnabled) {
        TakeNoteAction.isEnabled = isEnabled;
    }

    @Override
    public @NotNull String getTemplateText() {
        return description;
    }
}
