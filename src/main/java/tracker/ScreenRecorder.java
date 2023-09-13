package tracker;

import org.jcodec.api.awt.AWTSequenceEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScreenRecorder {

    boolean isRecording = false;

    public void startRecording() {
        isRecording = true;
        try {
            recordScreen();
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        isRecording = false;

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


}
