package component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class NoteDialog extends DialogWrapper {
    private JTextArea textArea;

    public NoteDialog(@Nullable Project project) {
        super(project, true);
        init();
        setTitle("Take Note");
    }

    @Override
    public @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        textArea = new JTextArea(10, 30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void doOKAction() {
        String note = textArea.getText();
        System.out.println(note);
        //save the note here

        super.doOKAction();
    }
}
