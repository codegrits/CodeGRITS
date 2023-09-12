import action.TakeNoteAction;
import com.intellij.openapi.actionSystem.*;
import component.ConfigDialog;

import java.util.List;

public class TakeNoteActionGroup extends DefaultActionGroup {

    public void refreshNote() {
        //get note from config dialog
        List<String> notes = ConfigDialog.getCurrentNotes();
        for (int i = 0; i < notes.size(); i++) {
            AnAction newNote = new TakeNoteAction();
            this.add(newNote);
        }
    }
}
