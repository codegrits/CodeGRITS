package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import trackers.ScreenRecorder;

import java.io.IOException;

public class PauseResumeTrackingAction extends AnAction {
    private final ScreenRecorder screenRecorder = ScreenRecorder.getInstance();

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
            TakeNoteAction.setIsEnabled(true);
            screenRecorder.resumeRecording();
            ConfigAction.setIsEnabled(false);

        } else {
            StartStopTrackingAction.pauseTracking();
            ConfigAction.setIsEnabled(false);
            TakeNoteAction.setIsEnabled(false);
            try {
                screenRecorder.pauseRecording();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
