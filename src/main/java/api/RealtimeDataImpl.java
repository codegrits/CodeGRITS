package api;

import com.intellij.openapi.project.Project;
import trackers.EyeTracker;
import trackers.IDETracker;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class RealtimeDataImpl{

    //make it singleton
    private static RealtimeDataImpl realtimeData = new RealtimeDataImpl();

    Socket socket;
    InputStream dataInputStream;


    private Consumer<String> ideTrackerDataHandler;
    private Consumer<String> eyeTrackerDataHandler;

    private static IDETracker ideTracker;

    private static EyeTracker eyeTracker;

    private RealtimeDataImpl(){
    }

    public static RealtimeDataImpl getInstance(){
        return realtimeData;
    }


    public void checkEnvironment() {
        System.out.println("Hello World!");
    }


    public void getRawIDETrackerData(Project project) throws ParserConfigurationException {
        ideTracker = IDETracker.getInstance();
        ideTracker.startTracking(project);
    }


    public void getRawEyeTrackerData() {

    }


    public void stopIDETrackerData() throws TransformerException {
        ideTracker.stopTracking();
    }


    public void stopEyeTrackerData() {

    }


    public void getHandledIDETrackerData(Project project) throws ParserConfigurationException, IOException {
        if(ideTrackerDataHandler == null){
            return;
        }
        Thread ideTrackerThread = new Thread(() -> {
            try {
                ideTracker = IDETracker.getInstance();
                ideTracker.setIsRealTimeDataTransmitting(true);
                ideTracker.startTracking(project);
                Thread.sleep(1000);
                Socket socket = new Socket("localhost", 12346);
                System.out.println("IDE Tracker connected!");
                dataInputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new java.io.InputStreamReader(dataInputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    ideTrackerDataHandler.accept(line);
                }
            } catch (IOException | ParserConfigurationException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        ideTrackerThread.start();
    }

    public void getHandledEyeTrackerData() {
        if(eyeTrackerDataHandler == null){
            throw new RuntimeException("Eye Tracker Data Handler not set!");
        }
    }



    public void setIDETrackerDataHandler(Consumer<String> ideTrackerDataHandler) {
        this.ideTrackerDataHandler = ideTrackerDataHandler;
    }


    public void setEyeTrackerDataHandler(Consumer<String> eyeTrackerDataHandler) {
        this.eyeTrackerDataHandler = eyeTrackerDataHandler;
    }

}
