package trackers;

import com.opencsv.CSVWriter;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Mat;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import org.bytedeco.opencv.opencv_calib3d.*;
import org.bytedeco.opencv.opencv_objdetect.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_calib3d.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;
import javax.imageio.ImageIO;
import javax.tools.Tool;
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

    private FrameRecorder recorder;

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
        recorder = FrameRecorder.createDefault(dataOutputPath + "/screen_recording/clip_" + clipNumber + ".avi", 1920, 1080);
        recorder.setFormat("avi");
        recorder.setFrameRate(30);
//        recorder.setPixelFormat(2);
        System.out.println(recorder.getPixelFormat());


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
        createEncoder();
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
        Thread recordThread = new Thread(() -> {
            try {
                recorder.start();
            } catch (FrameRecorder.Exception e) {
                throw new RuntimeException(e);
            }
            while (isRecording) {
                try {
                    Mat grabbedImage = Java2DFrameUtils.toMat(robot.createScreenCapture(screenRect));

                    int height = grabbedImage.rows();
                    int width = grabbedImage.cols();
                    grabbedImage.convertTo(grabbedImage, CvType.CV_8UC3);
                    Mat rgbImage = new Mat(height, width, CvType.CV_8UC3);
//                    cvtColor(grabbedImage, rgbImage, COLOR_RGB2BGR);
                    recorder.record(converterToMat.convert(grabbedImage));
                    Thread.sleep(1000/30);
                } catch (InterruptedException | FrameRecorder.Exception e) {
                    throw new RuntimeException(e);
                }

            }
            try {
                recorder.stop();
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
