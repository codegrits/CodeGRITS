package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import entity.Config;
import org.jetbrains.annotations.NotNull;
import tracker.EyeTracker;
import tracker.IDETracker;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;


public class StartStopTrackingAction extends AnAction {

    boolean isTracking = false;
    IDETracker iDETracker;
    EyeTracker eyeTracker;

    Config config = new Config();

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(isTracking ? "Stop Tracking" : "Start Tracking");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        config.loadFromJson();
        // set trackers' configs
        try {
            if (!isTracking) {
                isTracking = true;

                iDETracker = IDETracker.getInstance();
                iDETracker.setProjectPath(e.getProject() != null ? e.getProject().getBasePath() : "");
                iDETracker.startTracking(e.getProject());

                eyeTracker = (eyeTracker == null) ? new EyeTracker() : eyeTracker;
                eyeTracker.setProjectPath(e.getProject() != null ? e.getProject().getBasePath() : "");
                eyeTracker.startTracking(e.getProject());
            } else {
                isTracking = false;
                eyeTracker.stopTracking();
                iDETracker.stopTracking();
            }
        } catch (ParserConfigurationException | TransformerException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}