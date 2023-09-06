package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScreenRecorderAction extends AnAction {

    private Thread recordingThread;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {


            //wrap in a thread

            recordingThread = new Thread(() -> {
                try {
                    System.out.println("Recording screen");
                    recordScreen();
                    System.out.println("Finished recording screen");
                } catch (AWTException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            recordingThread.start();

    }
    private void recordScreen() throws AWTException, IOException {
        Rectangle bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        Robot robot = new Robot();
        AWTSequenceEncoder awtEncoder = AWTSequenceEncoder.createSequenceEncoder(new File("video.mp4"), 24);

        long startTime = System.currentTimeMillis();
        long frameRate = 24;
        long lastFrameTime = System.nanoTime();
        long frameDuration = 1000000000 / frameRate;
        while (System.currentTimeMillis() - startTime < 50000) { // Record for 10 seconds
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
