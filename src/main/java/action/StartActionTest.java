package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tracker.ScreenRecorder;

public class StartActionTest extends AnAction {
    ScreenRecorder screenRecorder = ScreenRecorder.getInstance();
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(screenRecorder.getState()== TrackerState.STOPPED.getValue());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        screenRecorder.startRecording();
    }

}
