package trackers;

import com.opencsv.CSVWriter;
import org.jcodec.api.awt.AWTSequenceEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ScreenRecorder {

    /*
     * 0: initial state; only startAction enabled
     * 1: started, not paused; stopAction and pauseAction enabled
     * 2: started, paused; only resumeAction enabled
     */
    int state = 0;
    private AWTSequenceEncoder awtEncoder;
    private final ArrayList<String[]> timeList = new ArrayList<>();
    private CSVWriter csvWriter;
    boolean isRecording = false;
    private int clipNumber = 1;
    private String dataOutputPath = "";

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
        awtEncoder = AWTSequenceEncoder.createSequenceEncoder(
                new File(dataOutputPath + "/screen_recording/video_clip_" + clipNumber + ".mp4"), 24);
    }


    public void startRecording() throws IOException {
        state = 1;
        isRecording = true;
        File file = new File(dataOutputPath + "/screen_recording/frames.csv");
        file.getParentFile().mkdirs();
        csvWriter = new CSVWriter(new FileWriter(file));
        csvWriter.writeNext(new String[]{"timestamp", "frame_number", "clip_number"});
        try {
            recordScreen();
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() throws IOException {
        state = 0;
        isRecording = false;
        csvWriter.writeAll(timeList);
        csvWriter.close();
    }

    public void pauseRecording() throws IOException {
        state = 2;
        isRecording = false;
        clipNumber++;
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
        Thread recordThread = new Thread(() -> {
            long frameNumber = 0;
            long lastFrameTime = System.nanoTime();
            long frameDuration = 1000000000 / frameRate;
            while (isRecording) {
                long currentTime = System.nanoTime();
                if (currentTime - lastFrameTime < frameDuration) {
                    continue;
                }
                lastFrameTime = currentTime;

                BufferedImage screenCapture = robot.createScreenCapture(bounds);
                frameNumber++;
                timeList.add(new String[]{String.valueOf(System.currentTimeMillis()), String.valueOf(frameNumber), String.valueOf(clipNumber)});
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
        recordThread.start();
    }

    public void setDataOutputPath(String dataOutputPath) {
        this.dataOutputPath = dataOutputPath;
    }
}
