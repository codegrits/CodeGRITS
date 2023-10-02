package actions;

import com.intellij.openapi.actionSystem.*;
import entity.Config;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TakeNoteActionGroup extends DefaultActionGroup {

    private static boolean isEnabled = true;
    private boolean defaultNotesLoaded = false;

    @Override
    public void update(@NotNull AnActionEvent e) {
        Config config = new Config();
        if(!defaultNotesLoaded && config.configExists()){
            config.loadFromJson();
            ActionManager actionManager = ActionManager.getInstance();
            DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction("CodeVision.TakeNoteActionGroup");
            actionGroup.removeAll();
            List<String> notes= config.getNotes();
            for (String note : notes) {
                TakeNoteAction takeNoteAction = new TakeNoteAction();
                takeNoteAction.setDescription(note);
                actionManager.registerAction("CodeVision.AddLabelAction.[" + note + "]", takeNoteAction);
                actionGroup.add(takeNoteAction);
            }
            defaultNotesLoaded = true;
        }
    }

    public static void setIsEnabled(boolean isEnabled) {
        TakeNoteActionGroup.isEnabled = isEnabled;
    }
}