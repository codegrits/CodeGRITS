package tracker;

import org.jcodec.api.awt.AWTSequenceEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScreenRecorder {

    /*
    * 0: initial state; only startAction enabled
    * 1: started, not paused; stopAction and pauseAction enabled
    * 2: started, paused; only resumeAction enabled
    * 3:
     */
    int state = 0;


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


    public void startRecording() {
        state = 1;

//        try {
//            recordScreen();
//        } catch (AWTException | IOException e) {
//            e.printStackTrace();
//        }
    }

    public void stopRecording() {
        state = 0;
    }

    public void pauseRecording() {
        state = 2;
    }

    public void resumeRecording() {
        state = 1;
    }

    private void recordScreen() throws AWTException, IOException {
        Rectangle bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        Robot robot = new Robot();
        AWTSequenceEncoder awtEncoder = AWTSequenceEncoder.createSequenceEncoder(new File("video.mp4"), 24);

        long startTime = System.currentTimeMillis();
        long frameRate = 24;
        long lastFrameTime = System.nanoTime();
        long frameDuration = 1000000000 / frameRate;
        while (System.currentTimeMillis() - startTime < 50000) {
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - lastFrameTime;
            if (elapsedTime >= frameDuration) {
                lastFrameTime = currentTime;
                BufferedImage screenCapture = robot.createScreenCapture(bounds);
//                for (int i = 0; i < slowDownFactor; i++) {
                awtEncoder.encodeImage(screenCapture);
//                }
            }
        }
        awtEncoder.finish();
    }

    public int getState() {
        return state;
    }
}
