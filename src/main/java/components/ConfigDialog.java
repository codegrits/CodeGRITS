package components;

import actions.TakeNoteAction;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import entity.Config;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ConfigDialog extends DialogWrapper {

    private List<JCheckBox> checkBoxes;
    private JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);

    private JPanel panel = new JPanel();
    private static List<JTextField> noteAreas = new ArrayList<>();

    private static JTextArea selectedFilePathTextArea;


    public ConfigDialog(Project project) {
        super(true); // use current window as parent
        init();
        setTitle("Config");
        //load config from file
        if (new File("config.json").exists()) {
            loadConfig();
            System.out.println("Config loaded");
        }

    }

    private void loadConfig() {
        Config config = new Config();
        config.loadFromJson();
        //parse checkbox
        List<Boolean> selected = config.getCheckBoxes();
        List<String> notes = config.getNotes();
        Integer freq = config.getFreq();
        for (int i = 0; i < selected.size(); i++) {
            checkBoxes.get(i).setSelected(selected.get(i));
        }
        //reset note area
        noteAreas = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++) {
            addNoteArea();
            noteAreas.get(i).setText(notes.get(i));
        }
        slider.setValue(freq);
        selectedFilePathTextArea.setText(config.getPythonInterpreter());
    }

    private void saveConfig() {
        //save config to file
        Config config = new Config(getSelectedCheckboxes(), getCurrentNotes(), slider.getValue(), getPythonInterpreter());
        config.saveAsJson();
//        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("config.dat"))) {
//            //parse checkbox
//            List<Boolean> selected = getSelectedCheckboxes();
//            List<String> notes = getCurrentNotes();
//            Config config = new Config(selected, notes, slider.getValue());
//            objectOutputStream.writeObject(config);
//            objectOutputStream.flush();
//            System.out.println("Config saved");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    protected void doOKAction() {
        //save config to file
        saveConfig();
        //TODO: reflect changes to action group
        updateActionGroup();
        super.doOKAction();
    }

    private void updateActionGroup() {
        //update action group
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction("TakeNoteActionGroup");
        List<String> notes = getCurrentNotes();
        System.out.println(notes);
        //reset action group
        AnAction[] actions = actionGroup.getChildActionsOrStubs();
        for (AnAction child : actions) {
            actionGroup.remove(child);
            String childId = ActionManager.getInstance().getId(child);
            ActionManager.getInstance().unregisterAction(childId);
        }
        //add new actions
        for (String note : notes) {
            TakeNoteAction newNote = new TakeNoteAction();
            newNote.setDescription(note);
            actionManager.registerAction("actions.TakeNoteAction" + note, newNote);
            actionGroup.add(newNote);
        }
    }


    @Override
    protected JComponent createCenterPanel() {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        checkBoxes = new ArrayList<>();
        JCheckBox eyeTracking = new JCheckBox("Eye tracking");
        checkBoxes.add(eyeTracking);
        panel.add(eyeTracking);

        JCheckBox mouseTracking = new JCheckBox("Mouse tracking");
        checkBoxes.add(mouseTracking);
        panel.add(mouseTracking);

        JCheckBox screenRecording = new JCheckBox("Screen recording");
        checkBoxes.add(screenRecording);
        panel.add(screenRecording);

        panel.add(new JLabel("Eye tracking sensitivity:"));
        slider.setEnabled(false); //initially disabled
        panel.add(slider);

        eyeTracking.addChangeListener(e -> {
            slider.setEnabled(eyeTracking.isSelected());
        });

        //add note component
        JButton addNote = new JButton("+");
        addNote.addActionListener(e -> {
            addNoteArea();
        });
        panel.add(addNote);

        //file selection
        JButton selectFile = new JButton("Select file");
        selectedFilePathTextArea = new JTextArea(1, 30);
        selectedFilePathTextArea.setEditable(false); // Prevent editing of the text area
        selectedFilePathTextArea.setText("");

//        selectedFilePathTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding
//        selectedFilePathTextArea.setLayout(new BorderLayout());
//        selectedFilePathTextArea.add(selectFile, BorderLayout.WEST); // Place button on the left

        selectFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String path = selectedFile.getAbsolutePath();
                selectedFilePathTextArea.setText(path);
            }
        });
        panel.add(selectFile);
        panel.add(selectedFilePathTextArea);
        return panel;
    }

    private void addNoteArea() {
        JPanel notePanel = new JPanel();
        JTextField textField = new JTextField();
        JButton minusButton = new JButton("-");
        minusButton.addActionListener(e -> {
            panel.remove(notePanel);
            noteAreas.remove(textField);
            panel.revalidate();
            panel.repaint();
        });
        noteAreas.add(textField);
        notePanel.add(textField);
        notePanel.add(minusButton);
        panel.add(notePanel);
        panel.revalidate();
        panel.repaint();
    }

    //some util methods
    public static List<String> getCurrentNotes() {
        List<String> selected = new ArrayList<>();
        for (JTextField textField : noteAreas) {
            selected.add(textField.getText());
        }
        return selected;
    }

    public List<Boolean> getSelectedCheckboxes() {
        List<Boolean> selected = new ArrayList<>();
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                selected.add(true);
            } else {
                selected.add(false);
            }
        }
        return selected;
    }

    public static String getPythonInterpreter() {
        return selectedFilePathTextArea.getText() == null ? "python" : selectedFilePathTextArea.getText();
    }


}
