package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import trackers.ScreenRecorder;

import java.io.IOException;

/**
 * This class is the action for pausing/resuming tracking.
 */
public class PauseResumeTrackingAction extends AnAction {
    private final ScreenRecorder screenRecorder = ScreenRecorder.getInstance();

    /**
     * Update the text of the action button. If the tracking is not started, the button is disabled.
     *
     * @param e The action event.
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        if (StartStopTrackingAction.isTracking()) {
            e.getPresentation().setEnabled(true);
            if (StartStopTrackingAction.isPaused()) {
                e.getPresentation().setText("Resume Tracking");
            } else {
                e.getPresentation().setText("Pause Tracking");
            }
        } else {
            e.getPresentation().setText("Pause Tracking");
            e.getPresentation().setEnabled(false);
        }
    }

    /**
     * This method is called when the action is performed. It will pause/resume tracking.
     *
     * @param e The action event.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (StartStopTrackingAction.isPaused()) {
            screenRecorder.resumeRecording();
            StartStopTrackingAction.resumeTracking();
            AddLabelAction.setIsEnabled(true);
            ConfigAction.setIsEnabled(false);

        } else {
            StartStopTrackingAction.pauseTracking();
            ConfigAction.setIsEnabled(false);
            AddLabelAction.setIsEnabled(false);
            try {
                screenRecorder.pauseRecording();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
