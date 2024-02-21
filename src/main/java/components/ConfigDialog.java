package components;

import actions.AddLabelAction;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.*;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;
import utils.AvailabilityChecker;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import entity.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to create the configuration dialog.
 */
public class ConfigDialog extends DialogWrapper {

    private List<JCheckBox> checkBoxes;

    private final JPanel panel = new JPanel();
    private static List<JTextField> labelAreas = new ArrayList<>();

    private static final TextFieldWithBrowseButton pythonInterpreterTextField = new TextFieldWithBrowseButton();
    private static final TextFieldWithBrowseButton dataOutputTextField = new TextFieldWithBrowseButton();

    private final JComboBox<Double> freqCombo = new ComboBox<>();
    private final JComboBox<String> deviceCombo = new ComboBox<>(new String[]{"Mouse"});

    private boolean pythonEnvironment = false;
    private boolean eyeTracker = false;

    public static String selectDataOutputPlaceHolder = "Select Data Output Folder (Default: Project Root)";
    public static String selectPythonInterpreterPlaceHolder = "Select Python Interpreter (Default: \"python\")";

    /**
     * The constructor of the configuration dialog.
     *
     * @param project The project.
     */
    public ConfigDialog(Project project) throws IOException, InterruptedException {
        super(true);
        init();
        setTitle("CodeGRITS Configuration");
        setSize(500, 500);
        setAutoAdjustable(true);
        setResizable(false);
        Config config = new Config();
        if (config.configExists()) {
            config.loadFromJson();
            List<Boolean> selected = config.getCheckBoxes();
            for (int i = 0; i < selected.size(); i++) {
                checkBoxes.get(i).setSelected(selected.get(i));
            }
            pythonInterpreterTextField.setText(config.getPythonInterpreter());
        }
        if (getPythonInterpreter().equals(selectPythonInterpreterPlaceHolder) || getPythonInterpreter().equals("python") || getPythonInterpreter().equals("python3") || getPythonInterpreter().equals("") || getPythonInterpreter().endsWith("python") || getPythonInterpreter().endsWith("python3") || getPythonInterpreter().endsWith("python.exe") || getPythonInterpreter().endsWith("python3.exe")) {
            pythonEnvironment = AvailabilityChecker.checkPythonEnvironment(getPythonInterpreter().equals(selectPythonInterpreterPlaceHolder)?"python":getPythonInterpreter());
            if (pythonEnvironment && checkBoxes.get(1).isSelected()) {
                eyeTracker = AvailabilityChecker.checkEyeTracker(getPythonInterpreter());
                if (eyeTracker) { //eye tracker found, add mouse and eye tracker name, add eye tracker freq
                    String trackerName = AvailabilityChecker.getEyeTrackerName(getPythonInterpreter());
                    if (trackerName != null && !trackerName.equals("Not Found")) {
                        deviceCombo.removeAllItems();
                        deviceCombo.addItem("Mouse");
                        deviceCombo.addItem(trackerName);
                        deviceCombo.setSelectedIndex(1);
                    }
                    List<String> freqList = AvailabilityChecker.getFrequencies(getPythonInterpreter());
                    freqCombo.removeAllItems();
                    for (String freq : freqList) {
                        freqCombo.addItem(Double.parseDouble(freq));
                    }
                } else { //use mouse and default freq
                    deviceCombo.removeAllItems();
                    deviceCombo.addItem("Mouse");
                    freqCombo.removeAllItems();
                    freqCombo.addItem(30.0);
                    freqCombo.addItem(60.0);
                    freqCombo.addItem(120.0);
                }
            } else {
                freqCombo.setEnabled(false);
                deviceCombo.setEnabled(false);
                freqCombo.removeAllItems();
                deviceCombo.removeAllItems();
                deviceCombo.addItem("Mouse");
                freqCombo.addItem(30.0);
                freqCombo.addItem(60.0);
                freqCombo.addItem(120.0);
            }
        }
        loadConfig();
    }

    /**
     * Load the configuration from the config.json file to the configuration dialog using the {@link Config} class.
     */
    private void loadConfig() {
        Config config = new Config();
        if (!config.configExists()) {
            addLabelArea(true);
            return;
        }
        config.loadFromJson();

        List<Boolean> selected = config.getCheckBoxes();
        List<String> labels = config.getLabels();
        Double freq = config.getSampleFreq();
        for (int i = 0; i < selected.size(); i++) {
            checkBoxes.get(i).setSelected(selected.get(i));
        }
        labelAreas = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            addLabelArea(false);
            labelAreas.get(i).setText(labels.get(i));
        }
        addLabelArea(true);
        freqCombo.setSelectedItem(freq);
        pythonInterpreterTextField.setText(config.getPythonInterpreter());
        dataOutputTextField.setText(config.getDataOutputPath());

        if (deviceCombo.getItemCount() > 1) {
            deviceCombo.setSelectedIndex(config.getEyeTrackerDevice());
        } else {
            deviceCombo.setSelectedIndex(0);
        }
        if (!checkBoxes.get(1).isSelected()) {
            freqCombo.setEnabled(false);
            deviceCombo.setEnabled(false);
        }
    }

    /**
     * Save the configuration from the configuration dialog to the config.json file using the {@link Config} class.
     */
    private void saveConfig() {
        Config config = new Config(getSelectedCheckboxes(), getCurrentLabels(), (Double) freqCombo.getSelectedItem(),
                getPythonInterpreter(), getDataOutputPath(), deviceCombo.getSelectedIndex());
        config.saveAsJson();
    }

    /**
     * Save the configuration when the OK button is clicked.
     */
    @Override
    protected void doOKAction() {
        saveConfig();
        updateActionGroup();
        super.doOKAction();
    }

    /**
     * Update the {@link AddLabelAction} group when the configuration is changed.
     */
    public void updateActionGroup() {
        //update action group
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction("CodeGRITS.AddLabelActionGroup");
        List<String> labels = getCurrentLabels();
        //reset action group
        AnAction[] actions = actionGroup.getChildActionsOrStubs();
        for (AnAction child : actions) {
            actionGroup.remove(child);
            String childId = ActionManager.getInstance().getId(child);
            if (childId != null) {
                ActionManager.getInstance().unregisterAction(childId);
            }
        }
        //add new actions
        for (String label : labels) {
            AddLabelAction newLabel = new AddLabelAction();
            newLabel.setDescription(label);
            String id = "CodeGRITS.AddLabelAction.[" + label + "]";
            if (actionManager.getAction(id) != null) actionManager.unregisterAction(id);
            actionManager.registerAction(id, newLabel);
            actionGroup.add(newLabel);
        }
    }

    /**
     * Create the center panel of the configuration dialog.
     *
     * @return The center panel of the configuration dialog.
     */
    @Override
    protected JComponent createCenterPanel() {

        Font headingFont = new Font("Arial", Font.PLAIN, 16);
        Insets headingMargin = JBUI.insets(5);
        Insets contentMargin = JBUI.insets(5, 20);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel functionalities = new JLabel("Functionalities");
        functionalities.setBorder(new EmptyBorder(headingMargin));
        functionalities.setFont(headingFont);
        panel.add(functionalities);

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.X_AXIS));
        checkBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkBoxPanel.setMaximumSize(new Dimension(500, 40));

        checkBoxes = new ArrayList<>();
        JCheckBox iDETracking = new JCheckBox("IDE Tracking");
        checkBoxes.add(iDETracking);
        checkBoxPanel.add(iDETracking);
        iDETracking.setSelected(true);
        iDETracking.setEnabled(false);

        JCheckBox eyeTracking = new JCheckBox("Eye Tracking");
        checkBoxes.add(eyeTracking);
        checkBoxPanel.add(eyeTracking);

        JCheckBox screenRecording = new JCheckBox("Screen Recording");
        checkBoxes.add(screenRecording);
        checkBoxPanel.add(screenRecording);

        iDETracking.setBorder(new EmptyBorder(contentMargin));
        eyeTracking.setBorder(new EmptyBorder(contentMargin));
        screenRecording.setBorder(new EmptyBorder(contentMargin));

        panel.add(checkBoxPanel);

        JLabel settings = new JLabel("Settings");
        settings.setBorder(new EmptyBorder(headingMargin));
        settings.setFont(headingFont);
        panel.add(settings);

        JLabel pythonInterpreterLabel = new JLabel("Python Interpreter Path");
        pythonInterpreterLabel.setBorder(new EmptyBorder(JBUI.insetsLeft(20)));
        pythonInterpreterLabel.setHorizontalTextPosition(JLabel.LEFT);
        panel.add(pythonInterpreterLabel);

        pythonInterpreterTextField.setEditable(false);
        pythonInterpreterTextField.setMaximumSize(new Dimension(500, 40));
        pythonInterpreterTextField.setBorder(new EmptyBorder(contentMargin));
        pythonInterpreterTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        pythonInterpreterTextField.setText(selectPythonInterpreterPlaceHolder);
        pythonInterpreterTextField.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(true, false, false, false, false, false)));
        pythonInterpreterTextField.getTextField().getDocument().addDocumentListener(
                new DocumentAdapter() {
                    @Override
                    protected void textChanged(@NotNull DocumentEvent e) {
                        try {
                            //TODO: what if using mac/unix/anaconda
                            if (getPythonInterpreter().equals("python") || getPythonInterpreter().equals("python3") || getPythonInterpreter().equals("") || getPythonInterpreter().endsWith("python") || getPythonInterpreter().endsWith("python3") || getPythonInterpreter().endsWith("python.exe") || getPythonInterpreter().endsWith("python3.exe")) {
                                pythonEnvironment = AvailabilityChecker.checkPythonEnvironment(getPythonInterpreter());
                            } else {
                                pythonEnvironment = false;
                            }
                            ComponentValidator.getInstance(pythonInterpreterTextField.getTextField()).ifPresent(ComponentValidator::revalidate);
                        } catch (IOException | InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
        );
        //add validator for python interpreter
        new ComponentValidator(getDisposable()).withValidator(() -> {
            String text = pythonInterpreterTextField.getText();
            if (!pythonEnvironment) {
                return new ValidationInfo("Python environment not configured.", pythonInterpreterTextField.getTextField());
            } else {
                File file = new File(text);
                if (!file.exists()) {
                    return new ValidationInfo("Python interpreter not found", pythonInterpreterTextField.getTextField());
                } else {
                    return null;
                }
            }
        }).installOn(pythonInterpreterTextField.getTextField());

        panel.add(pythonInterpreterTextField);

        JLabel dataOutputLabel = new JLabel("Data Output Path");
        dataOutputLabel.setHorizontalTextPosition(JLabel.LEFT);
        dataOutputLabel.setBorder(new EmptyBorder(JBUI.insetsLeft(20)));
        panel.add(dataOutputLabel);
        dataOutputTextField.setEditable(false);
        dataOutputTextField.setButtonEnabled(false);
        dataOutputTextField.setBorder(new EmptyBorder(contentMargin));
        dataOutputTextField.setMaximumSize(new Dimension(500, 40));
        dataOutputTextField.setText(selectDataOutputPlaceHolder);
        dataOutputTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dataOutputTextField.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(false, true, false, false, false, false)));

        panel.add(dataOutputTextField);

        JPanel freqPanel = new JPanel();
        freqPanel.setLayout(new BoxLayout(freqPanel, BoxLayout.Y_AXIS));
        freqPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel freqLabel = new JLabel("Sample Frequency");
        freqLabel.setHorizontalTextPosition(JLabel.LEFT);
        freqLabel.setBorder(new EmptyBorder(JBUI.insetsBottom(5)));
        freqCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        freqCombo.setMaximumSize(new Dimension(230, 40));
        freqPanel.add(freqLabel);
        freqPanel.add(freqCombo);

        JPanel devicePanel = new JPanel();
        devicePanel.setLayout(new BoxLayout(devicePanel, BoxLayout.Y_AXIS));
        devicePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel deviceLabel = new JLabel("Eye Tracker Device");
        deviceLabel.setHorizontalTextPosition(JLabel.LEFT);
        deviceLabel.setBorder(new EmptyBorder(JBUI.insetsBottom(5)));
        deviceCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        deviceCombo.setMaximumSize(new Dimension(230, 40));
        devicePanel.add(deviceLabel);
        devicePanel.add(deviceCombo);

        JPanel comboPanel = new JPanel();
        comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
        comboPanel.add(freqPanel);
        comboPanel.add(devicePanel);
        comboPanel.setBorder(new EmptyBorder(JBUI.insets(0, 20, 5, 20)));
        panel.add(comboPanel);

        eyeTracking.addChangeListener(e -> {
            freqCombo.setEnabled(eyeTracking.isSelected());
            deviceCombo.setEnabled(eyeTracking.isSelected());
        });

        JPanel labelAreaPanel = new JPanel();
        JLabel labels = new JLabel("Preset Labels");
        labels.setFont(headingFont);
        labels.setBorder(new EmptyBorder(headingMargin));
        labels.setHorizontalTextPosition(JLabel.LEFT);
        labelAreaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelAreaPanel.setLayout(new BoxLayout(labelAreaPanel, BoxLayout.X_AXIS));
        labelAreaPanel.add(labels);
        panel.add(labelAreaPanel);

        eyeTracking.addActionListener(actionEvent -> {
            if (!pythonEnvironment) {
                eyeTracking.setSelected(false);
                new AlertDialog("Python environment not configured.", AllIcons.General.BalloonWarning).show();
                return;
            }
            if (!eyeTracking.isSelected()) {
                freqCombo.setEnabled(false);
                deviceCombo.setEnabled(false);
            }
            if (eyeTracking.isSelected() && pythonEnvironment) {
                deviceCombo.setEnabled(true);
                try {
                    eyeTracker = AvailabilityChecker.checkEyeTracker(getPythonInterpreter());
                    if (!eyeTracker) {
                        new AlertDialog("Eye tracker not found. Using mouse tracker.", AllIcons.General.BalloonWarning).show();
                    } else {
                        freqCombo.setEnabled(true);
                        String trackerName = AvailabilityChecker.getEyeTrackerName(getPythonInterpreter());
                        if (trackerName != null && !trackerName.equals("Not Found")) {
                            deviceCombo.removeAllItems();
                            deviceCombo.addItem("Mouse");
                            deviceCombo.addItem(trackerName);
                            deviceCombo.setSelectedIndex(1);
                        }
                        List<String> freqList = AvailabilityChecker.getFrequencies(getPythonInterpreter());
                        freqCombo.removeAllItems();
                        for (String freq : freqList) {
                            freqCombo.addItem(Double.parseDouble(freq));
                        }
                        new AlertDialog("Eye tracker found.", AllIcons.General.InspectionsOK).show();
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return panel;
    }

    /**
     * Add the label area to the configuration dialog. A label area contains a text field and a button to add or remove the label area.
     *
     * @param isEmpty Whether the label panel is empty. If {@code true}, the button will be an add button. If {@code false}, the button will be a remove button.
     */
    private void addLabelArea(boolean isEmpty) {
        JPanel labelPanel = new JPanel();
        JTextField textField = new JTextField();
        JButton button = new JButton();
        String spaceRegex = "^\\s*$";
        String digitsRegex = "^\\d+$";
        String lettersRegex = "^\\w+$";
        String punctuationRegex = "^[\\p{Punct}&&[^,]]+$";
        Pattern spacePattern = Pattern.compile(spaceRegex);
        Pattern digitsPattern = Pattern.compile(digitsRegex);
        Pattern lettersPattern = Pattern.compile(lettersRegex);
        Pattern punctuationPattern = Pattern.compile(punctuationRegex);
        new ComponentValidator(getDisposable()).withValidator(() -> {
            String text = textField.getText();
            Matcher spaceMatcher = spacePattern.matcher(textField.getText());
            Matcher digitsMatcher = digitsPattern.matcher(textField.getText());
            Matcher lettersMatcher = lettersPattern.matcher(textField.getText());
            Matcher punctuationMatcher = punctuationPattern.matcher(textField.getText());
            Set<String> invalidChars = new HashSet<>();
            if (spaceMatcher.matches() || text.equals("")) {
                button.setEnabled(false);
                return new ValidationInfo("Label cannot be empty", textField);
            } else {
                for (int i = 0; i < text.length(); i++) {
                    String c = String.valueOf(text.charAt(i));
                    if (!digitsMatcher.reset(c).matches() && !lettersMatcher.reset(c).matches() && !punctuationMatcher.reset(c).matches() && !c.equals(" ")) {
                        invalidChars.add(c);
                        button.setEnabled(false);
                    }
                }
                if (invalidChars.size() > 0) {
                    return new ValidationInfo("Label cannot contain " + invalidChars.toString(), textField);
                } else {
                    button.setEnabled(true);
                    return null;
                }
            }
        }).installOn(textField);

        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                ComponentValidator.getInstance(textField).ifPresent(ComponentValidator::revalidate);
            }
        });

        textField.setColumns(20);
        labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setMaximumSize(new Dimension(500, 40));

        button.setSize(40, 40);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelPanel.add(textField);
        labelPanel.add(button);
        if (isEmpty) {
            button.setIcon(AllIcons.General.Add);
            button.addActionListener(e -> {
                if (Objects.equals(textField.getText(), "")) {
                    button.setEnabled(false);
                    return;
                }
                if (button.getIcon() == AllIcons.General.Remove) {
                    panel.remove(labelPanel);
                    labelAreas.remove(textField);
                    panel.revalidate();
                    panel.repaint();
                } else {
                    labelAreas.add(textField);
                    addLabelArea(true);
                    button.setIcon(AllIcons.General.Remove);
                }
            });
        } else {
            labelAreas.add(textField);
            button.setIcon(AllIcons.General.Remove);
            button.addActionListener(e -> {
                panel.remove(labelPanel);
                labelAreas.remove(textField);
                panel.revalidate();
                panel.repaint();
            });
        }
        panel.add(labelPanel);
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.setBorder(new EmptyBorder(JBUI.insets(5, 20, 0, 20)));
        labelPanel.setMaximumSize(new Dimension(500, 40));
    }

    /**
     * Get the current labels in the configuration dialog.
     *
     * @return The current labels in the configuration dialog.
     */
    public static List<String> getCurrentLabels() {
        List<String> selected = new ArrayList<>();
        for (JTextField textField : labelAreas) {
            selected.add(textField.getText());
        }
        return selected;
    }

    /**
     * Get the selected checkboxes in the configuration dialog.
     *
     * @return The selected checkboxes in the configuration dialog.
     */
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

    /**
     * Get the python interpreter path in the configuration dialog. If the path is empty, the default "python" will be returned.
     *
     * @return The python interpreter path in the configuration dialog.
     */
    public static String getPythonInterpreter() {
        if (ProjectManager.getInstance().getOpenProjects().length == 0) {
            return "python";
        }
        if (pythonInterpreterTextField.getText().equals("")
                || pythonInterpreterTextField.getText().equals(selectPythonInterpreterPlaceHolder)) {
            return "python";
        }
        return pythonInterpreterTextField.getText().equals(selectPythonInterpreterPlaceHolder)
                ? selectPythonInterpreterPlaceHolder : pythonInterpreterTextField.getText();
    }

    /**
     * Get the data output path in the configuration dialog. If the path is equal to the placeholder, the default project root will be returned.
     *
     * @return The data output path in the configuration dialog.
     */
    public static String getDataOutputPath() {
        return dataOutputTextField.getText().equals(selectDataOutputPlaceHolder)
                ? selectDataOutputPlaceHolder : dataOutputTextField.getText();
    }

}
