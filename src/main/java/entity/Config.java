package entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.List;

public class Config implements Serializable {
    private List<Boolean> checkBoxes;
    private List<String> notes;
    private Integer freq;
    private String pythonInterpreter;

    public Config(List<Boolean> checkBoxes, List<String> notes, Integer freq, String pythonInterpreter) {
        this.checkBoxes = checkBoxes;
        this.notes = notes;
        this.freq = freq;
        this.pythonInterpreter = pythonInterpreter;
    }

    public Config() {
    }

    public void saveAsJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pythonInterpreter", pythonInterpreter);
        jsonObject.addProperty("freq", freq);
        jsonObject.addProperty("notes", notes.toString());
        jsonObject.addProperty("checkBoxes", checkBoxes.toString());
        //write to json file
        try (FileWriter fileWriter = new FileWriter("config.json")) {
            fileWriter.write(jsonObject.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadFromJson(){
        //read from json file
        try (FileReader fileReader = new FileReader("config.json")) {
            Gson gson = new Gson();
            JsonElement jsonElement = JsonParser.parseReader(fileReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            pythonInterpreter = jsonObject.get("pythonInterpreter").getAsString();
            freq = jsonObject.get("freq").getAsInt();
            notes = gson.fromJson(jsonObject.get("notes").getAsString(), List.class);
            checkBoxes = gson.fromJson(jsonObject.get("checkBoxes").getAsString(), List.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPythonInterpreter() {
        return pythonInterpreter;
    }

    public Integer getFreq() {
        return freq;
    }

    public List<Boolean> getCheckBoxes() {
        return checkBoxes;
    }

    public List<String> getNotes() {
        return notes;
    }

}
