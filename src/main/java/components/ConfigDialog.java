package components;

import actions.ScreenRecorderAction;
import actions.TakeNoteAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
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
    }

    @Override
    protected void doOKAction() {
        //save config to file
        saveConfig();
        updateActionGroup();
        updateActions();
        super.doOKAction();
    }

    private void updateActions(){
        List<Boolean> checkBoxes = getSelectedCheckboxes();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if(checkBoxes.get(i)){
                //enable action
                if(i == 0){
                    //eye tracking
                }else if(i == 1){
                    //mouse tracking
                }else if(i == 2){
                    //screen recording
                    ScreenRecorderAction.setEnabled(true);

                }
            }else{

                //disable action
                if (i == 0) {
                    //eye tracking
                } else if (i == 1) {
                    //mouse tracking
                } else if (i == 2) {
                    //screen recording
                    ScreenRecorderAction.setEnabled(false);


                }
            }
        }
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
//        FlowLayout flowLayout = new FlowLayout();
        //top to bottom
//        flowLayout.setAlignment(FlowLayout.LEFT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        //add checkbox component
        JPanel checkBoxPanel = new JPanel();
        JLabel functionalities = new JLabel("Functionalities");


        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        checkBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkBoxPanel.add(functionalities);
        functionalities.setPreferredSize(new Dimension(100, 20));
//        panel.add(functionalities);

        checkBoxes = new ArrayList<>();
        JCheckBox eyeTracking = new JCheckBox("Eye tracking");
        checkBoxes.add(eyeTracking);
        checkBoxPanel.add(eyeTracking);

        JCheckBox mouseTracking = new JCheckBox("Mouse tracking");
        checkBoxes.add(mouseTracking);
        checkBoxPanel.add(mouseTracking);

        JCheckBox screenRecording = new JCheckBox("Screen recording");
        checkBoxes.add(screenRecording);
        checkBoxPanel.add(screenRecording);

        panel.add(checkBoxPanel);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.add(new JLabel("Settings"));


        selectedFilePathTextArea = new JTextArea(1, 30);
        selectedFilePathTextArea.setEditable(false); // Prevent editing of the text area
        selectedFilePathTextArea.setText("");

        // Add folder icon to the selectedFilePathTextArea



        TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton();
        textFieldWithBrowseButton.setText("Select file");
        textFieldWithBrowseButton.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(true, false, false, false, false, false)));
        settingsPanel.add(textFieldWithBrowseButton);


        panel.add(settingsPanel);



        eyeTracking.addChangeListener(e -> {
            slider.setEnabled(eyeTracking.isSelected());
        });

        //add note component
        JButton addNote = new JButton("+");
        addNote.addActionListener(e -> {
            addNoteArea();
        });
//        panel.add(addNote);


//        panel.add(selectFile);
//        panel.add(selectedFilePathTextArea);
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
