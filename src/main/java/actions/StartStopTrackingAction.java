package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import entity.Config;
import org.jetbrains.annotations.NotNull;
import trackers.EyeTracker;
import trackers.IDETracker;
import trackers.ScreenRecorder;
import utils.AvailabilityChecker;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Objects;


public class StartStopTrackingAction extends AnAction {

    private static boolean isTracking = false;
    private static IDETracker iDETracker;
    private static EyeTracker eyeTracker;

    private final ScreenRecorder screenRecorder = ScreenRecorder.getInstance();

    Config config = new Config();

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(isTracking ? "Stop Tracking" : "Start Tracking");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if(config.configExists()){
            config.loadFromJson();
        }else{
            JOptionPane.showMessageDialog(null, "Please configure the plugin first.");
        }
        try {
            if (!isTracking) {
                if (config.getCheckBoxes().get(1)) {
                    if (!AvailabilityChecker.checkPythonEnvironment(config.getPythonInterpreter())) {
                        JOptionPane.showMessageDialog(null, "Python interpreter not found. Please check your configuration.");
                        return;
                    }
                    if (config.getEyeTrackerDevice() != 0 && !AvailabilityChecker.checkEyeTracker(config.getPythonInterpreter())) {
                        JOptionPane.showMessageDialog(null, "Eye tracker not found. Please use mouse tracker instead.");
                        return;
                    }
                }

                isTracking = true;
                ConfigAction.setIsEnabled(false);
                TakeNoteActionGroup.setIsEnabled(true);
                String projectPath = e.getProject() != null ? e.getProject().getBasePath() : "";
                String realDataOutputPath = Objects.equals(config.getDataOutputPath(), "Select Data Output Folder")
                        ? projectPath : config.getDataOutputPath();
                realDataOutputPath += "/" + System.currentTimeMillis() + "/";

                iDETracker = IDETracker.getInstance();
                iDETracker.setProjectPath(projectPath);
                iDETracker.setDataOutputPath(realDataOutputPath);
                iDETracker.startTracking(e.getProject());

                if (config.getCheckBoxes().get(1)) {
                    eyeTracker = new EyeTracker();
                    eyeTracker.setProjectPath(projectPath);
                    eyeTracker.setDataOutputPath(realDataOutputPath);
                    eyeTracker.setPythonInterpreter(config.getPythonInterpreter());
                    eyeTracker.setSampleFrequency(config.getSampleFreq());
                    eyeTracker.setDeviceIndex(config.getEyeTrackerDevice());
                    eyeTracker.setPythonScriptTobii();
                    eyeTracker.setPythonScriptMouse();
                    eyeTracker.startTracking(e.getProject());
                }
                if (config.getCheckBoxes().get(2)) {
                    screenRecorder.setDataOutputPath(realDataOutputPath);
                    screenRecorder.startRecording();
                }
//                if (config.getCheckBoxes().get(3)){ //socket
//                    iDETracker.transmitRealTimeData();
//                }
                TakeNoteAction.setIsEnabled(true);

            } else {
                isTracking = false;
                iDETracker.stopTracking();
                TakeNoteAction.setIsEnabled(false);
                ConfigAction.setIsEnabled(true);
                if (config.getCheckBoxes().get(1) && eyeTracker != null) {
                    eyeTracker.stopTracking();
                }
                if (config.getCheckBoxes().get(2)) {
                    screenRecorder.stopRecording();
                }
                eyeTracker = null;
            }
        } catch (ParserConfigurationException | TransformerException | IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean isTracking() {
        return isTracking;
    }

    public static boolean isPaused() {
        return !iDETracker.isTracking();
    }

    public static void pauseTracking() {
        iDETracker.pauseTracking();
        if (eyeTracker != null) {
            eyeTracker.pauseTracking();
        }
    }

    public static void resumeTracking() {
        iDETracker.resumeTracking();
        if (eyeTracker != null) {
            eyeTracker.resumeTracking();
        }
    }

}