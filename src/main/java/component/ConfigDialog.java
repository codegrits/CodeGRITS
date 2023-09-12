package component;

import action.OutputHandler;
import action.ScreenRecorderAction;
import action.TakeNoteAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.JBUI;
import entity.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ConfigDialog extends DialogWrapper {

    private List<JCheckBox> checkBoxes;

    private final JPanel panel = new JPanel();
    private static List<JTextField> noteAreas = new ArrayList<>();

    private static TextFieldWithBrowseButton pythonInterpreterTextField;
    private static TextFieldWithBrowseButton dataOutputTextField;

    private final JComboBox<Integer> freqCombo = new ComboBox<>(new Integer[]{30,40,60});
    private final JComboBox<String> deviceCombo = new ComboBox<>(new String[]{"webcam", "eye tracker"});


    public ConfigDialog(Project project) {
        super(true); // use current window as parent
        init();
        setTitle("Config");
        setSize(500, 500);
        setAutoAdjustable(true);
//        setSize(500, 500);
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
        //TODO: set freq
        freqCombo.setSelectedIndex(freq / 15 - 2);
        pythonInterpreterTextField.setText(config.getPythonInterpreter());
    }

    private void saveConfig() {
        //save config to file
        Config config = new Config(getSelectedCheckboxes(), getCurrentNotes(), (Integer) freqCombo.getSelectedItem(), getPythonInterpreter());
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

        Font headingFont = new Font("Arial", Font.PLAIN, 16);
        Insets headingMargin = JBUI.insets(5); // Adjust the values as needed
        Insets contentMargin = JBUI.insets(5, 20); // Adjust the values as needed

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        panel.setAlignmentX(Component.);

        //add checkbox component
        JPanel checkBoxPanel = new JPanel();
        JLabel functionalities = new JLabel("Functionalities");

        functionalities.setBorder(new EmptyBorder(headingMargin));


        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        checkBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkBoxPanel.add(functionalities);
        functionalities.setFont(headingFont);
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

//        checkBoxPanel.setBorder(new EmptyBorder(contentMargin));
        eyeTracking.setBorder(new EmptyBorder(contentMargin));
        mouseTracking.setBorder(new EmptyBorder(contentMargin));
        screenRecording.setBorder(new EmptyBorder(contentMargin));
        panel.add(checkBoxPanel);

        //add settings component

        JLabel settings = new JLabel("Settings");
        settings.setBorder(new EmptyBorder(headingMargin));
        settings.setFont(headingFont);
        settings.setHorizontalTextPosition(JLabel.LEFT);
        panel.add(settings);

        JPanel checkPythonPanel = new JPanel();
        JLabel pythonInterpreterLabel = new JLabel("Python Interpreter Path");
        pythonInterpreterLabel.setBorder(new EmptyBorder(JBUI.insets(5,20,0,5)));
        pythonInterpreterLabel.setHorizontalTextPosition(JLabel.LEFT);
//        panel.add(pythonInterpreterLabel);
        JButton checkPython = new JButton("Check Availability");

        checkPython.addActionListener(e -> {
            try {
                OutputHandler outputHandler = new OutputHandler();
                outputHandler.checkTracker();
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        checkPythonPanel.setLayout(new BoxLayout(checkPythonPanel, BoxLayout.X_AXIS));
        checkPythonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkPythonPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        checkPythonPanel.add(pythonInterpreterLabel);
        checkPythonPanel.add(checkPython);
        panel.add(checkPythonPanel);

        pythonInterpreterTextField = new TextFieldWithBrowseButton();
        pythonInterpreterTextField.setEditable(false);
        pythonInterpreterTextField.setMaximumSize(new Dimension(500, 40));
        pythonInterpreterTextField.setBorder(new EmptyBorder(contentMargin));
        pythonInterpreterTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        pythonInterpreterTextField.setText("Select Python Interpreter");

        pythonInterpreterTextField.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(true, false, false, false, false, false)));

        panel.add(pythonInterpreterTextField);

        JLabel dataOutputLabel = new JLabel("Data Output Path");
        dataOutputLabel.setHorizontalTextPosition(JLabel.LEFT);
        dataOutputLabel.setBorder(new EmptyBorder(JBUI.insets(0,20,0,0)));
//        dataOutputLabel.setBorder(new EmptyBorder(headingMargin));
        panel.add(dataOutputLabel);

        dataOutputTextField = new TextFieldWithBrowseButton();
        dataOutputTextField.setEditable(false);
        dataOutputTextField.setBorder(new EmptyBorder(contentMargin));
        dataOutputTextField.setMaximumSize(new Dimension(500, 40));
        dataOutputTextField.setText("Select Data Output Folder");
        dataOutputTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dataOutputTextField.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(false, true, false, false, false, false)));

        panel.add(dataOutputTextField);


        JPanel freqPanel = new JPanel();
        freqPanel.setLayout(new BoxLayout(freqPanel, BoxLayout.Y_AXIS));
        freqPanel.setAlignmentX(Component.LEFT_ALIGNMENT);


        JLabel freqLabel = new JLabel("Frequency");
        JLabel deviceLabel = new JLabel("Device");

        deviceLabel.setHorizontalTextPosition(JLabel.LEFT);
        freqLabel.setHorizontalTextPosition(JLabel.LEFT);
        freqCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        deviceCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        freqCombo.setPreferredSize(new Dimension(221, 30));
        deviceCombo.setPreferredSize(new Dimension(221, 30));

        freqPanel.add(freqLabel);
        freqPanel.add(freqCombo);

        JPanel devicePanel = new JPanel();
        devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.Y_AXIS));
        devicePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        devicePanel.add(deviceLabel);
        devicePanel.add(deviceCombo);
        JPanel comboPanel = new JPanel();
        comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboPanel.add(freqPanel);
        comboPanel.add(devicePanel);
        comboPanel.setMaximumSize(new Dimension(500, 65));
        comboPanel.setMinimumSize(new Dimension(500, 65));
        panel.add(comboPanel);
        eyeTracking.addChangeListener(e -> {
            freqCombo.setEnabled(eyeTracking.isSelected());
        });

        //add note component
        JPanel noteAreaPanel = new JPanel();

        JLabel notes = new JLabel("Notes");
        notes.setFont(headingFont);
        notes.setBorder(new EmptyBorder(headingMargin));
        notes.setHorizontalTextPosition(JLabel.LEFT);
        JButton addNote = new JButton("Add Preset");
        addNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteAreaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteAreaPanel.setLayout(new BoxLayout(noteAreaPanel, BoxLayout.X_AXIS));
        noteAreaPanel.add(notes);
        noteAreaPanel.add(addNote);
//        noteAreaPanel.setBorder(new EmptyBorder(contentMargin));

        addNote.addActionListener(e -> {
            addNoteArea();
        });
        panel.add(noteAreaPanel);


        return panel;
    }

    private void addNoteArea() {
        JPanel notePanel = new JPanel();
        JTextField textField = new JTextField();
        JButton minusButton = new JButton();
        textField.setColumns(20);
        minusButton.setMaximumSize(new Dimension(1, 1));
        minusButton.setIcon(AllIcons.General.Remove);
        notePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        minusButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        return pythonInterpreterTextField.getText().equals("Select Python Interpreter") ? "python" : pythonInterpreterTextField.getText();
    }


}
