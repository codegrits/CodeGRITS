package api;

import com.intellij.openapi.project.Project;
import trackers.EyeTracker;
import trackers.IDETracker;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.function.Consumer;

public class RealtimeDataImpl implements RealtimeDataInterface{

    //make it singleton
    private static RealtimeDataImpl realtimeData = new RealtimeDataImpl();


    private Consumer<String> ideTrackerDataHandler;
    private Consumer<String> eyeTrackerDataHandler;

    private static IDETracker ideTracker;

    private static EyeTracker eyeTracker;

    private RealtimeDataImpl(){
    }

    public static RealtimeDataImpl getInstance(){
        return realtimeData;
    }

    @Override
    public void checkEnvironment() {
        System.out.println("Hello World!");
    }

    @Override
    public void getRawIDETrackerData(Project project) throws ParserConfigurationException {
        ideTracker = IDETracker.getInstance();
        ideTracker.startTracking(project);
    }

    @Override
    public void getRawEyeTrackerData() {

    }

    @Override
    public void stopIDETrackerData() throws TransformerException {
        ideTracker.stopTracking();
    }

    @Override
    public void stopEyeTrackerData() {

    }

    @Override
    public void getHandledIDETrackerData() throws ParserConfigurationException {
        if(ideTrackerDataHandler == null){
            throw new RuntimeException("IDE Tracker Data Handler not set!");
        }
        ideTracker = IDETracker.getInstance();
//        ideTracker.startTracking();

    }

    @Override
    public void getHandledEyeTrackerData() {
        if(eyeTrackerDataHandler == null){
            throw new RuntimeException("Eye Tracker Data Handler not set!");
        }
    }


    @Override
    public void setIDETrackerDataHandler(Consumer<String> ideTrackerDataHandler) {
        this.ideTrackerDataHandler = ideTrackerDataHandler;
    }

    @Override
    public void setEyeTrackerDataHandler(Consumer<String> eyeTrackerDataHandler) {
        this.eyeTrackerDataHandler = eyeTrackerDataHandler;
    }

}
