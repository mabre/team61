package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.io.Settings;
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

    /** to switch back to menu */
    private SceneController sceneController;
    /** contains image */
    @FXML private ImageView imageView = new ImageView();
    /** contains heading */
    private Image image = new Image("file:resources/layout/options.png");
    /** to change volume of background music */
    @FXML private Slider volumeBGM = new Slider();
    /** to change volume of sound effects */
    @FXML private Slider volumeSFX = new Slider();
    /** to enter how many seconds till round ends */
    @FXML private TextField secondsPerTurn = new TextField();
    /** to enter how many rounds till sudden death */
    @FXML private TextField turnsTillSuddenDeath = new TextField();
    /** to choose wind force */
    @FXML private Slider windForce = new Slider();

    /**
     * Called when options are opened. Sets the header-image and labels of the windforce-slider, which are supposed
     * to be texts instead of just numbers. Also calls {@link #loadSavedOptions()}.
     * @param sceneController to switch back to menu
     */
    public void initialize(SceneController sceneController) {
        this.sceneController = sceneController;
        imageView.setImage(image);
        loadSavedOptions();
        windForce.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n == 0) return "Off";
                if (n == 1) return "Easy";
                if (n == 2) return "Normal";
                return "Hard";
            }
            @Override
            public Double fromString(String s) {
                return 3.141592;
            }
        });
    }

    /**
     * Switches back to main menu.
     */
    @FXML
    public void handleOptionExit() {
        Settings.savePrefs(toJson());
        System.out.println("OptionsWindow: saved settings");
        sceneController.switchToMenu();
    }

    /**
     * Loads the saved options.
     */
    public void loadSavedOptions() {
        JSONObject savedSettings = Settings.getSavedPrefs();
        if(savedSettings.has("volumeBGM")) {
            volumeBGM.setValue(savedSettings.getDouble("volumeBGM"));
        }
        if(savedSettings.has("volumeSFX")) {
            volumeSFX.setValue(savedSettings.getDouble("volumeSFX"));
        }
        turnsTillSuddenDeath.setText(savedSettings.getInt("turnsTillSuddenDeath", 30)+""); // TODO DefaultOptions class (Settings?) holding default setting constants
        secondsPerTurn.setText(savedSettings.getInt("secondsPerTurn", 30)+"");
        windForce.setValue(savedSettings.getInt("windForce", 2));
    }

    /**
     * Saves options when going back to menu.
     * @return JSONObject containing custom options
     */
    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("volumeBGM", volumeBGM.getValue());
        output.put("volumeSFX", volumeSFX.getValue());
        try {
            output.put("turnsTillSuddenDeath", Integer.parseInt(turnsTillSuddenDeath.getText()));
        } catch(NumberFormatException e) {
            output.put("turnsTillSuddenDeath", 30);
        }
        try {
            output.put("secondsPerTurn", Integer.parseInt(secondsPerTurn.getText()));
        } catch(NumberFormatException e) {
            output.put("secondsPerTurn", 30);
        }
        output.put("windForce", windForce.getValue());
        return output;
    }
}
