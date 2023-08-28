import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import org.jetbrains.annotations.NotNull;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;


public class StartStopTrackingAction extends AnAction {

    boolean isTracking = false;
    IDETracker iDETracker;
    EyeTracker eyeTracker;
    MouseTracker mouseTracker;
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

                mouseTracker = new MouseTracker();
                mouseTracker.startTracking();
            } else {
                isTracking = false;
                mouseTracker.stopTracking();
                eyeTracker.stopTracking();
                eyeTrackerThread.join();
                iDETracker.stopTracking();
            }
        } catch (ParserConfigurationException | TransformerException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}