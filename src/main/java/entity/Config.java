package entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.List;

public class Config implements Serializable {
    private List<Boolean> checkBoxes;
    private List<String> labels;
    private Double sampleFreq;
    private String pythonInterpreter;
    private String dataOutputPath;
    private Integer eyeTrackerDevice;

    public Config(List<Boolean> checkBoxes, List<String> labels, Double sampleFreq, String pythonInterpreter, String dataOutputPath, Integer eyeTrackerDevice) {
        this.checkBoxes = checkBoxes;
        this.labels = labels;
        this.sampleFreq = sampleFreq;
        this.pythonInterpreter = pythonInterpreter;
        this.dataOutputPath = dataOutputPath;
        this.eyeTrackerDevice = eyeTrackerDevice;
    }

    public Config() {
    }

    public boolean configExists() {
        try (FileReader fileReader = new FileReader("config.json")) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void saveAsJson() {
        JsonObject jsonObject = new JsonObject();
        if (sampleFreq == null) sampleFreq = 30.0;
        jsonObject.addProperty("pythonInterpreter", pythonInterpreter);
        jsonObject.addProperty("sampleFreq", sampleFreq);
        jsonObject.addProperty("labels", labels.toString());
        jsonObject.addProperty("checkBoxes", checkBoxes.toString());
        jsonObject.addProperty("dataOutputPath", dataOutputPath);
        jsonObject.addProperty("eyeTrackerDevice", eyeTrackerDevice);

        try (FileWriter fileWriter = new FileWriter("config.json")) {
            fileWriter.write(jsonObject.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadFromJson() {
        try (FileReader fileReader = new FileReader("config.json")) {
            Gson gson = new Gson();
            JsonElement jsonElement = JsonParser.parseReader(fileReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            pythonInterpreter = jsonObject.get("pythonInterpreter").getAsString();
            sampleFreq = jsonObject.get("sampleFreq").getAsDouble();
            dataOutputPath = jsonObject.get("dataOutputPath").getAsString();
            eyeTrackerDevice = jsonObject.get("eyeTrackerDevice").getAsInt();
            String labelsString = jsonObject.get("labels").getAsString().substring(1, jsonObject.get("labels").getAsString().length() - 1);
            if (labelsString.equals("")) {
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
