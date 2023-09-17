//package action;
//
//import com.intellij.openapi.actionSystem.AnAction;
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import org.jetbrains.annotations.NotNull;
//import tracker.ScreenRecorder;
//
//
//public class RecordScreenAction extends AnAction {
//
//    static boolean isEnabled = true;
//    boolean isRecording = false;
//    ScreenRecorder screenRecorder = ScreenRecorder.getInstance();
//
//    @Override
//    public void update(@NotNull AnActionEvent e) {
//        e.getPresentation().setText(isRecording ? "Stop Recording" : "Start Recording");
//    }
//
//    @Override
//    public void actionPerformed(@NotNull AnActionEvent e) {
//        if (!isRecording) {
//            isRecording = true;
//            Thread recordThread = new Thread(() -> screenRecorder.startRecording());
//            recordThread.start();
//            setEnabled(false);
//        } else {
//            isRecording = false;
//            screenRecorder.stopRecording();
//            setEnabled(true);
//        }
//    }
//
//    public static void setEnabled(boolean isEnabled) {
//        RecordScreenAction.isEnabled = isEnabled;
//    }
//
//}
