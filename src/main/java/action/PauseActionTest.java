package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tracker.ScreenRecorder;

import java.io.IOException;

public class PauseActionTest extends AnAction {
    ScreenRecorder screenRecorder = ScreenRecorder.getInstance();
    private boolean isPaused = false;
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(screenRecorder.getState()== TrackerState.STARTED.getValue() || screenRecorder.getState()== TrackerState.PAUSED.getValue());
        if(isPaused)
            e.getPresentation().setText("Resume");
        else
            e.getPresentation().setText("Pause");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if(!isPaused) {
            try {
                screenRecorder.pauseRecording();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        else
            screenRecorder.resumeRecording();
        isPaused = !isPaused;
    }

}
