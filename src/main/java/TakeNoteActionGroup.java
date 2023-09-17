import action.TakeNoteAction;
import com.intellij.openapi.actionSystem.*;
import component.ConfigDialog;

import java.util.List;

public class TakeNoteActionGroup extends DefaultActionGroup {

    private static boolean isEnabled = true;

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(isEnabled);
    }

    public static void setIsEnabled(boolean isEnabled) {
        TakeNoteActionGroup.isEnabled = isEnabled;
    }
}