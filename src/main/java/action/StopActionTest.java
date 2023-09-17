package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tracker.ScreenRecorder;

import java.io.IOException;

public class StopActionTest extends AnAction {
    ScreenRecorder screenRecorder = ScreenRecorder.getInstance();
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(screenRecorder.getState()== TrackerState.STARTED.getValue());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            screenRecorder.stopRecording();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
