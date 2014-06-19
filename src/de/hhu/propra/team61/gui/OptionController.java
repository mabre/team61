package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.gui.SceneController;
import de.hhu.propra.team61.io.Options;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;

/**
 * Created by Jessypet on 04.06.14.
 */

public class OptionController {

    SceneController sceneController;
    @FXML private CheckBox fullscreen;
    @FXML private Slider volume;
    @FXML private Slider gamma;

    public OptionController() {
        loadSavedSettings();
    }

    public void setSceneController(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    @FXML
    public void handleOptionExit() {
        Options.save(toJson());
        System.out.println("OptionsWindow: saved settings");
        sceneController.switchToMenue();
    }

    public void loadSavedSettings() {
        JSONObject savedSettings = Options.getSavedSettings();
        if(savedSettings.has("volume")) {                   //TODO throws NullPointerException?!
            volume.setValue(savedSettings.getDouble("volume"));
        }
        if(savedSettings.has("fullscreen")) {
            fullscreen.setSelected(savedSettings.getBoolean("fullscreen"));
        }
        /*if(savedSettings.has("resolution")) {
            resolution.setValue(savedSettings.getString("resolution"));
        }*/
        if(savedSettings.has("gamma")) {
            gamma.setValue(savedSettings.getDouble("gamma"));
        }
    }

    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("volume", volume.getValue());
        output.put("fullscreen", fullscreen.isSelected());
        //output.put("resolution", resolution.getValue());
        output.put("gamma", gamma.getValue());
        return output;
    }
}
