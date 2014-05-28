package de.hhu.propra.team61;

import de.hhu.propra.team61.GUI.BigStage;
import de.hhu.propra.team61.GUI.CustomGrid;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.IO.Options;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/*
 * Created by Jessypet on 11.05.14.
 * This class shows options. 
 *
 */

public class OptionsWindow extends Application {

    RadioButton sound1, sound2;
    Slider volume;
    CheckBox fullscreen;
    ChoiceBox<String> resolution;
    Slider gamma;

	public void doOptions(Stage stageToClose) {
		BigStage optionStage = new BigStage("Options");
        optionStage.setOnHiding(event -> {
            Options.save(this.toJson());
            System.out.println("OptionsWindow: saved settings");
        });
		CustomGrid optionGrid = new CustomGrid();
		optionGrid.setAlignment(Pos.CENTER);

        Text optionst = new Text("Options");
		optionst.setFont(Font.font ("Verdana", 20));
		optionGrid.add(optionst, 0, 0, 3, 1);
			
		Text soundt = new Text("Music: ");
		optionGrid.add(soundt, 0, 2);
		ToggleGroup sound = new ToggleGroup();
		sound1 = new RadioButton("On"); // TODO can be removed? (set volume to 0 has same effect)
		sound1.setToggleGroup(sound);
		sound1.setSelected(true);
		sound2 = new RadioButton("Off");
		sound2.setToggleGroup(sound);
		optionGrid.add(sound1, 1, 2);
		optionGrid.add(sound2, 2, 2);
			
		Text volt = new Text("Volume: ");
		optionGrid.add(volt, 0, 3);
		volume = new Slider(0, 100, 50);
		volume.setShowTickMarks(true);
		volume.setShowTickLabels(true);
		optionGrid.add(volume, 1, 3, 2, 1);
			
		Text resot = new Text("Resolution: ");
		optionGrid.add(resot, 0, 4);
		fullscreen = new CheckBox("Full Screen");
		optionGrid.add(fullscreen, 1, 4);
		resolution = new ChoiceBox<>(FXCollections.observableArrayList("800x600", "1024x768", "1280x720", "1366x768", "1440x900", "1920x1080"));
		resolution.getSelectionModel().selectFirst();
		optionGrid.add(resolution, 2, 4);
			
		Text gammat = new Text("Gamma: ");
		optionGrid.add(gammat, 0, 5);
		gamma = new Slider(0, 100, 50);
		gamma.setShowTickMarks(true);
		gamma.setShowTickLabels(true);
		optionGrid.add(gamma, 1, 5, 2, 1);
			
		Button oexit = new Button("Close");                    //options-exit, click closes options-window
		optionGrid.add(oexit, 1, 8);
		oexit.setOnAction(new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent e) {
            stageToClose.show();
			optionStage.close();
			}
		});

        loadSavedSettings();

		Scene oscene = new Scene(optionGrid, 1000, 600);
		optionStage.setScene(oscene);
        oscene.getStylesheets().add("file:resources/layout/css/options.css");
        optionGrid.getStyleClass().add("optionspane");
        optionst.getStyleClass().add("optionstext");
        optionStage.show();
        stageToClose.close();
	}

    private void loadSavedSettings() {
        JSONObject savedSettings = Options.getSavedSettings();
        if(savedSettings.has("sound1")) {
            sound1.setSelected(savedSettings.getBoolean("sound1"));
        }
        if(savedSettings.has("sound2")) {
            sound2.setSelected(savedSettings.getBoolean("sound2"));
        }
        if(savedSettings.has("volume")) {
            volume.setValue(savedSettings.getDouble("volume"));
        }
        if(savedSettings.has("fullscreen")) {
            fullscreen.setSelected(savedSettings.getBoolean("fullscreen"));
        }
        if(savedSettings.has("resolution")) {
            resolution.setValue(savedSettings.getString("resolution"));
        }
        if(savedSettings.has("gamma")) {
            gamma.setValue(savedSettings.getDouble("gamma"));
        }
    }

    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("sound1", sound1.isSelected());
        output.put("sound2", sound2.isSelected());
        output.put("volume", volume.getValue());
        output.put("fullscreen", fullscreen.isSelected());
        output.put("resolution", resolution.getValue());
        output.put("gamma", gamma.getValue());
        return output;
    }
	
	@Override
	public void start(Stage ostage) { }
 
}
