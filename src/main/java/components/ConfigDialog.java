package components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigDialog extends DialogWrapper {

    private List<JCheckBox> checkBoxes;
    private JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);

    public ConfigDialog(Project project) {
        super(true); // use current window as parent
        init();
        setTitle("Config");
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();
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



        return panel;
    }

    public List<JCheckBox> getSelectedCheckboxes() {
        List<JCheckBox> selected = new ArrayList<>();
        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                selected.add(checkBox);
            }
        }
        return selected;
    }


}
