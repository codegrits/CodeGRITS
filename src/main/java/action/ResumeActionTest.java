package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tracker.ScreenRecorder;

public class ResumeActionTest extends AnAction {
    ScreenRecorder screenRecorder = ScreenRecorder.getInstance();
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(screenRecorder.getState()== TrackerState.PAUSED.getValue());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        screenRecorder.resumeRecording();
    }


}
