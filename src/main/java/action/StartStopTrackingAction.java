package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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
    Thread eyeTrackerThread;

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(isTracking ? "Stop Tracking" : "Start Tracking");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            if (!isTracking) {
                isTracking = true;

                iDETracker = IDETracker.getInstance();
                iDETracker.setProjectPath(e.getProject() != null ? e.getProject().getBasePath() : "");
                iDETracker.startTracking(e.getProject());

                eyeTracker = (eyeTracker == null) ? new EyeTracker() : eyeTracker;
                eyeTracker.setProjectPath(e.getProject() != null ? e.getProject().getBasePath() : "");
                eyeTracker.editor = e.getData(CommonDataKeys.EDITOR);
                eyeTrackerThread = new Thread(() -> {
                    try {
                        eyeTracker.startTracking();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                eyeTrackerThread.start();

            } else {
                isTracking = false;
                eyeTracker.stopTracking();
                eyeTrackerThread.join();
                iDETracker.stopTracking();
            }
        } catch (ParserConfigurationException | TransformerException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}