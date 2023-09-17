package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import trackers.ScreenRecorder;

import java.io.IOException;

public class PauseResumeAction extends AnAction {
    private ScreenRecorder screenRecorder = ScreenRecorder.getInstance();

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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (StartStopTrackingAction.isPaused()) {
            StartStopTrackingAction.resumeTracking();
            screenRecorder.resumeRecording();

        } else {
            StartStopTrackingAction.pauseTracking();
            try {
                screenRecorder.pauseRecording();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
