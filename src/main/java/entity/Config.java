package entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.bytedeco.javacv.FrameFilter;
import utils.OSDetector;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.List;

/**
 * This class is used to store the configuration of the application.
 */
public class Config implements Serializable {
    private List<Boolean> checkBoxes;
    private List<String> labels;
    private Double sampleFreq;
    private String pythonInterpreter;
    private String dataOutputPath;
    private Integer eyeTrackerDevice;
    //TODO: change mac path
    private static final String macConfigPath = System.getProperty("user.home")+"/Library/CodeGRITS/codegrits_config.json";
    private static final String otherConfigPath = "codegrits_config.json";

    /**
     * The constructor of the Config class.
     *
     * @param checkBoxes        The list of the checkboxes.
     * @param labels            The list of the labels.
     * @param sampleFreq        The sample frequency.
     * @param pythonInterpreter The path of the python interpreter.
     * @param dataOutputPath    The path of the data output folder.
     * @param eyeTrackerDevice  The index of the eye tracker device.
     */
    public Config(List<Boolean> checkBoxes, List<String> labels, Double sampleFreq, String pythonInterpreter, String dataOutputPath, Integer eyeTrackerDevice) {
        this.checkBoxes = checkBoxes;
        this.labels = labels;
        this.sampleFreq = sampleFreq;
        this.pythonInterpreter = pythonInterpreter;
        this.dataOutputPath = dataOutputPath;
        this.eyeTrackerDevice = eyeTrackerDevice;
    }

    /**
     * The constructor of the Config class.
     */
    public Config() {
    }

    public boolean configExists() {
        FileReader fileReader;
        try{
            if(OSDetector.isMac()){
                fileReader = new FileReader(macConfigPath);
            } else{
                fileReader = new FileReader(otherConfigPath);
            }
            return true;
        } catch (Exception e){
            return false;
        }

    }

    /**
     * Save the configuration as a JSON file.
     */
    public void saveAsJson() {
        JsonObject jsonObject = new JsonObject();
        if (sampleFreq == null) sampleFreq = 30.0;
        jsonObject.addProperty("pythonInterpreter", pythonInterpreter);
        jsonObject.addProperty("sampleFreq", sampleFreq);
        jsonObject.addProperty("labels", labels.toString());
        jsonObject.addProperty("checkBoxes", checkBoxes.toString());
        jsonObject.addProperty("dataOutputPath", dataOutputPath);
        jsonObject.addProperty("eyeTrackerDevice", eyeTrackerDevice);

        if(OSDetector.isMac()){
            try {
                File directory = new File(System.getProperty("user.home")+"/Library/CodeGRITS/");
                if(!directory.exists()) directory.mkdir();
                FileWriter fileWriter = new FileWriter(macConfigPath);
                fileWriter.write(jsonObject.toString());
                fileWriter.flush();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
        else {
            try (FileWriter fileWriter = new FileWriter(otherConfigPath)) {
                fileWriter.write(jsonObject.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Load the configuration from the JSON file.
     */
    public void loadFromJson() {
        try {
            FileReader fileReader;
            if(OSDetector.isMac()) fileReader = new FileReader(macConfigPath);
                else fileReader = new FileReader(otherConfigPath);
            Gson gson = new Gson();
            JsonElement jsonElement = JsonParser.parseReader(fileReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            pythonInterpreter = jsonObject.get("pythonInterpreter").getAsString();
            sampleFreq = jsonObject.get("sampleFreq").getAsDouble();
            dataOutputPath = jsonObject.get("dataOutputPath").getAsString();
            eyeTrackerDevice = jsonObject.get("eyeTrackerDevice").getAsInt();
            String labelsString = jsonObject.get("labels").getAsString().substring(1, jsonObject.get("labels").getAsString().length() - 1);
            if (labelsString.isEmpty()) {
                labels = List.of();
            } else labels = List.of(labelsString.split(", "));
            checkBoxes = gson.fromJson(jsonObject.get("checkBoxes").getAsString(), new TypeToken<List<Boolean>>() {
            }.getType());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPythonInterpreter() {
        return pythonInterpreter;
    }

    public Double getSampleFreq() {
        return sampleFreq;
    }

    public List<Boolean> getCheckBoxes() {
        return checkBoxes;
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getDataOutputPath() {
        return dataOutputPath;
    }

    public Integer getEyeTrackerDevice() {
        return eyeTrackerDevice;
    }

    public String toString() {
        return "Config{" +
                "checkBoxes=" + checkBoxes +
                ", labels=" + labels +
                ", sampleFreq=" + sampleFreq +
                ", pythonInterpreter='" + pythonInterpreter + '\'' +
                ", dataOutputPath='" + dataOutputPath + '\'' +
                ", eyeTrackerDevice=" + eyeTrackerDevice +
                '}';
    }

}
