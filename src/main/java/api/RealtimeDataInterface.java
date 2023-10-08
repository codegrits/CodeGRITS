package api;

import com.intellij.openapi.project.Project;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.function.Consumer;

public interface RealtimeDataInterface {
    void checkEnvironment();

    void getRawIDETrackerData(Project project) throws ParserConfigurationException;
    void getRawEyeTrackerData();

    void stopIDETrackerData() throws TransformerException;
    void stopEyeTrackerData();

    void getHandledIDETrackerData() throws ParserConfigurationException;
    void getHandledEyeTrackerData();

    void setIDETrackerDataHandler(Consumer<String> ideTrackerDataHandler);

    void setEyeTrackerDataHandler(Consumer<String> eyeTrackerDataHandler);
}
