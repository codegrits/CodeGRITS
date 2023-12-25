package trackers;

import com.opencsv.CSVWriter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenRecorder {

    /*
     * 0: initial state; only startAction enabled
     * 1: started, not paused; stopAction and pauseAction enabled
     * 2: started, paused; only resumeAction enabled
     */
    int state = 0;
    int frameRate = 4;
    private AWTSequenceEncoder awtEncoder;
//    private FFmpegFrameRecorder recorder;
    private final ArrayList<String[]> timeList = new ArrayList<>();
    private CSVWriter csvWriter;
    boolean isRecording = false;
    private int clipNumber = 1;
    private int frameNumber = 0;
    private String dataOutputPath = "";
    private static ScreenRecorder instance = null;

    public static ScreenRecorder getInstance() {
        if (instance == null) {
            instance = new ScreenRecorder();
        }
        return instance;
    }


    private void createEncoder() throws IOException {
        awtEncoder = AWTSequenceEncoder.createSequenceEncoder(
                new File(dataOutputPath + "/screen_recording/video_clip_" + clipNumber + ".mp4"), frameRate);
    }


    public void startRecording() throws IOException {
        state = 1;
        clipNumber = 1;
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
        createEncoder();
        frameNumber = 0;
        Rectangle bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Robot robot = new Robot();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isRecording) {
                    try {
                        awtEncoder.finish();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    timer.cancel();
                } else {
                    BufferedImage screenCapture = robot.createScreenCapture(bounds);
                    frameNumber++;
                    timeList.add(new String[]{String.valueOf(System.currentTimeMillis()), String.valueOf(frameNumber), String.valueOf(clipNumber)});
                    try {
                        if (frameNumber == 1) {
                            ImageIO.write(screenCapture, "png", new File(dataOutputPath + "/screen_recording/frame_" + frameNumber + ".png"));
                        }
                        awtEncoder.encodeImage(screenCapture);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 0, 1000 / frameRate);
    }

    public void setDataOutputPath(String dataOutputPath) {
        this.dataOutputPath = dataOutputPath;
    }
}