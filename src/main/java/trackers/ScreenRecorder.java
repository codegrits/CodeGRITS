package trackers;

import org.jcodec.api.awt.AWTSequenceEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ScreenRecorder {

    /*
     * 0: initial state; only startAction enabled
     * 1: started, not paused; stopAction and pauseAction enabled
     * 2: started, paused; only resumeAction enabled
     */
    int state = 0;
    private Thread recordThread;
    private AWTSequenceEncoder awtEncoder;
    private ArrayList<String[]> timeList;
    boolean isRecording = false;


    //refactor into singleton
    private static ScreenRecorder instance = null;

    private ScreenRecorder() {
    }

    public static ScreenRecorder getInstance() {
        if (instance == null) {
            instance = new ScreenRecorder();
        }
        return instance;
    }

    private void createEncoder() throws IOException {
        awtEncoder = AWTSequenceEncoder.createSequenceEncoder(new File("video" + System.currentTimeMillis() + ".mp4"), 24);
    }


    public void startRecording() {
        state = 1;
        isRecording = true;
        try {
            recordScreen();
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() throws IOException {
        state = 0;
        isRecording = false;
    }

    public void pauseRecording() throws IOException {
        state = 2;
        isRecording = false;
    }

    public void resumeRecording() {
        state = 1;
        isRecording = true;
        try {
            recordScreen();
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    private void recordScreen() throws AWTException, IOException {
        Rectangle bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        Robot robot = new Robot();
        createEncoder();
        long frameRate = 24;
        recordThread = new Thread(() -> {
            long lastFrameTime = System.nanoTime();
            long frameDuration = 1000000000 / frameRate;
            while (isRecording) {
                long currentTime = System.nanoTime();
                if (currentTime - lastFrameTime < frameDuration) {
                    continue;
                }
                lastFrameTime = currentTime;

                BufferedImage screenCapture = robot.createScreenCapture(bounds);
                try {
                    awtEncoder.encodeImage(screenCapture);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                awtEncoder.finish();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(recordThread.getId());
        recordThread.start();
    }

    public int getState() {
        return state;
    }
}
