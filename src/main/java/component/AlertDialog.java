package component;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AlertDialog extends DialogWrapper {
    private String label;
    private Icon icon;

    public AlertDialog(String label, Icon icon) {

        super(true); // use current window as parent
        this.label = label;
        this.icon = icon;
        init();
        setTitle("Alert");

    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }


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

    public void setLabel(String label) {
        this.label = label;
    }
}
