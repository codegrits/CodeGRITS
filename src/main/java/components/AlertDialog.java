package components;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * This class is used to create a dialog to show alert message.
 */
public class AlertDialog extends DialogWrapper {
    /**
     * The label of the alert message.
     */
    private final String label;
    /**
     * The icon of the alert message.
     */
    private final Icon icon;

    /**
     * The constructor of the class.
     *
     * @param label The alert message.
     * @param icon  The icon of the alert message.
     */
    public AlertDialog(String label, Icon icon) {

        super(true); // use current window as parent
        this.label = label;
        this.icon = icon;
        init();
        setTitle("Alert");

    }

    /**
     * Create the OK button.
     * @return The OK button.
     */
    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }

    /**
     * Create the center panel of the dialog.
     * @return The center panel of the dialog.
     */
    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(this.label);
        JLabel icon = new JLabel();
        label.setIcon(this.icon);

        icon.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        dialogPanel.add(label);

        return dialogPanel;
    }

}
