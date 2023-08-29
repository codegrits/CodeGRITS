package entity;

import java.io.Serializable;
import java.util.List;

public class Config implements Serializable {
    private List<Boolean> checkBoxes;
    private List<String> notes;
    private Integer freq;

    public Config(List<Boolean> checkBoxes, List<String> notes, Integer freq) {
        this.checkBoxes = checkBoxes;
        this.notes = notes;
        this.freq = freq;
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
