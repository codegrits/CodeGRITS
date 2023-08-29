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
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ConfigDialog extends DialogWrapper {

    private List<JCheckBox> checkBoxes;
    private JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);

    private JPanel panel = new JPanel();
    private static List<JTextField> noteAreas = new ArrayList<>();



    public ConfigDialog(Project project) {
        super(true); // use current window as parent
        init();
        setTitle("Config");
        //load config from file
        if(new File("config.dat").exists()){
            loadConfig();
            System.out.println("Config loaded");
        }

    }

    private void loadConfig(){
        try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("config.dat"))) {
            Config config = (Config) objectInputStream.readObject();
            //parse checkbox
            List<Boolean> selected = config.getCheckBoxes();
            List<String> notes = config.getNotes();
            Integer freq = config.getFreq();
            for (int i = 0; i < selected.size(); i++) {
                checkBoxes.get(i).setSelected(selected.get(i));
            }
//            for (int i = 0; i < notes.size(); i++) {
//                addNoteArea();
//                noteAreas.get(i).setText(notes.get(i));
//            }
            slider.setValue(freq);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveConfig(){
        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("config.dat"))) {
            //parse checkbox
            List<Boolean> selected = getSelectedCheckboxes();
            List<String> notes = getCurrentNotes();
            Config config = new Config(selected, notes, slider.getValue());
            objectOutputStream.writeObject(config);
            objectOutputStream.flush();
            System.out.println("Config saved");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doOKAction() {
        //save config to file
        saveConfig();
        super.doOKAction();
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

        JButton confirmNote = new JButton("Confirm");
        confirmNote.addActionListener(e -> {
            //update action group
            ActionManager actionManager = ActionManager.getInstance();
            DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction("TakeNoteActionGroup");
            List<String> notes = getCurrentNotes();
            //clear everything in group
            actionGroup.removeAll();
            for (int i = 0; i < notes.size(); i++) {
                TakeNoteAction newNote = new TakeNoteAction();
                newNote.setDescription(notes.get(i));
                actionManager.registerAction("actions.TakeNoteAction" + i, newNote);
                actionGroup.add(newNote);
            }

        });
        panel.add(confirmNote);
        return panel;
    }
    private void addNoteArea(){
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
    public static List<String> getCurrentNotes(){
        List<String> selected = new ArrayList<>();
        for(JTextField textField : noteAreas){
            selected.add(textField.getText());
        }
        return selected;
    }
    public List<Boolean> getSelectedCheckboxes() {
        List<Boolean> selected = new ArrayList<>();
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                selected.add(true);
            }else{
                selected.add(false);
            }
        }
        return selected;
    }



}
