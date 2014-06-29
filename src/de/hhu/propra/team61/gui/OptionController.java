package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.io.Options;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;

/**
 * Controls GUI for options.
 *
 * Created by Jessypet on 04.06.14.
 */

public class OptionController {

    /** to switch back to menue */
    private SceneController sceneController;
    /** contains image */
    @FXML private ImageView imageView = new ImageView();
    /** contains heading */
    private Image image = new Image("file:resources/layout/options.png");
    /** to change volume of background music */
    @FXML private Slider volumeMusic = new Slider();
    /** to change volume of sound effects */
    @FXML private Slider volumeEffects = new Slider();
    /** to enter how many rounds till sudden death */
    @FXML private TextField suddenDeath = new TextField();
    /** to choose wind force */
    @FXML private Slider windForce = new Slider();
    /** to enter time per turn */
    @FXML private TextField timePerTurn = new TextField();
    /** to decide whether turn is over after getting fal damage */
    @FXML private CheckBox fallDamage = new CheckBox();

    /**
     * Called when options are opened. Sets the header-image and labels of the windforce-slider, which are supposed
     * to be texts instead of just numbers. Also calls {@link #loadSavedOptions()}.
     * @param sceneController to switch back to menue
     */
    public void initialize(SceneController sceneController) {
        this.sceneController = sceneController;
        imageView.setImage(image);
        timePerTurn.setDisable(true);
        loadSavedOptions();
        windForce.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n == 1) return "Easy";
                if (n == 2) return "Normal";
                if (n == 3) return "Hard";
                return "You idiot!";
            }
            @Override
            public Double fromString(String s) {
                return 3.141592;
            }
        });
    }

    /**
     * Switches back to main menue,
     */
    @FXML
    public void handleOptionExit() {
        Options.save(toJson());
        System.out.println("OptionsWindow: saved settings");
        sceneController.switchToMenue();
    }

    /**
     * Loads the saved options.
     */
    public void loadSavedOptions() {
        JSONObject savedSettings = Options.getSavedSettings();
        if(savedSettings.has("volumeMusic")) {
            volumeMusic.setValue(savedSettings.getDouble("volumeMusic"));
        }
        if(savedSettings.has("volumeEffects")) {
            volumeEffects.setValue(savedSettings.getDouble("volumeEffects"));
        }
        if(savedSettings.has("sd")) {
            suddenDeath.setText(savedSettings.getString("sd"));
        }
        if(savedSettings.has("windForce")) {
            windForce.setValue(savedSettings.getDouble("windForce"));
        }
        if(savedSettings.has("timePerTurn")) {
            timePerTurn.setText(savedSettings.getString("timePerTurn"));
        }
        if(savedSettings.has("fallDamage")) {
            fallDamage.setSelected(savedSettings.getBoolean("fallDamage"));
        }
    }

    /**
     * Saves options when going back to menue.
     * @return JSONObject containing custom options
     */
    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("volumeMusic", volumeMusic.getValue());
        output.put("volumeEffects", volumeEffects.getValue());
        if (suddenDeath.getText().equals("") || !suddenDeath.getText().matches("[0-9]*")) {
            output.put("sd", String.valueOf(15));
        } else {
            output.put("sd", suddenDeath.getText());
        }
        output.put("windForce", windForce.getValue());
        output.put("timePerTurn", timePerTurn.getText());
        output.put("fallDamage", fallDamage.isSelected());
        return output;
    }
}
