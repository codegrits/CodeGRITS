package actions;

import com.intellij.openapi.actionSystem.*;
import entity.Config;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddLabelActionGroup extends DefaultActionGroup {

    private static boolean isEnabled = true;
    private boolean defaultLabelsLoaded = false;

    @Override
    public void update(@NotNull AnActionEvent e) {
        Config config = new Config();
        if (!defaultLabelsLoaded && config.configExists()) {
            config.loadFromJson();
            ActionManager actionManager = ActionManager.getInstance();
            DefaultActionGroup actionGroup = (DefaultActionGroup) actionManager.getAction("CodeGRITS.AddLabelActionGroup");
            actionGroup.removeAll();
            List<String> labels = config.getLabels();
            for (String label : labels) {
                AddLabelAction addLabelAction = new AddLabelAction();
                addLabelAction.setDescription(label);
                actionManager.registerAction("CodeGRITS.AddLabel.[" + label + "]", addLabelAction);
                actionGroup.add(addLabelAction);
            }
            defaultLabelsLoaded = true;
        }
    }

    public static void setIsEnabled(boolean isEnabled) {
        AddLabelActionGroup.isEnabled = isEnabled;
    }
}