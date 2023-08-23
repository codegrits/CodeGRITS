import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


public class StartStopTrackingAction extends AnAction {

    boolean isTracking = false;
    IDETracker iDETracker;
//    EyeTracker eyeTracker;
//    Thread eyeTrackerThread;
//    Socket screenRecordingSocket;
//    DataOutputStream screenRecordingOutputStream;

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(isTracking ? "Stop Tracking" : "Start Tracking");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            if (!isTracking) {
                isTracking = true;

//                screenRecordingSocket = new Socket("localhost", 12346);
//                screenRecordingOutputStream = new DataOutputStream(screenRecordingSocket.getOutputStream());
//                screenRecordingOutputStream.writeUTF("start");

                iDETracker = IDETracker.getInstance();
                iDETracker.setProjectPath(e.getProject() != null ? e.getProject().getBasePath() : "");
                iDETracker.startTracking(e.getProject());

//                eyeTracker = (eyeTracker == null) ? new EyeTracker() : eyeTracker;
//                eyeTracker.setProjectPath(e.getProject() != null ? e.getProject().getBasePath() : "");
//                eyeTrackerThread = new Thread(() -> {
//                    try {
//                        eyeTracker.startDetection();
//                    } catch (IOException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                });
//                eyeTrackerThread.start();
            } else {
                isTracking = false;

//                screenRecordingOutputStream.writeUTF("end");
//                screenRecordingOutputStream.close();
//                screenRecordingSocket.close();
//
//                eyeTracker.stopDetection();
//                eyeTracker = null;
//                eyeTracker = new EyeTracker();

                iDETracker.stopTracking();
            }
        } catch (ParserConfigurationException | TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }
}