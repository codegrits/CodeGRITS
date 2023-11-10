package component;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AlertDialog extends DialogWrapper {
    private String label;
    private Icon icon;
    public AlertDialog(String label,Icon icon) {

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

        //show a warning icon

        JLabel label = new JLabel(this.label);
        JLabel icon = new JLabel();
        label.setIcon(this.icon);

        System.out.println(UIManager.getIcon("OptionPane.warningIcon").getIconHeight());
        icon.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
//        dialogPanel.add(icon);
        dialogPanel.add(label);

        return dialogPanel;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}