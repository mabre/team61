package de.hhu.propra.team61;

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.collections.*;

/*
 * Created by Jessypet on 11.05.14.
 * This class shows options. 
 *
 */

public class OptionsWindow extends Application {

	public void do_options() {
		Stage ostage = new Stage();
		ostage.setTitle("Options");
		ostage.setWidth(800);
		ostage.setHeight(500);
		ostage.setResizable(false);
		GridPane ogrid = new GridPane();
		ogrid.setAlignment(Pos.CENTER);
		ogrid.setHgap(10);
		ogrid.setVgap(10);
		ogrid.setPadding(new Insets(25, 25, 25, 25));
			
		Text optionst = new Text("Options"); 
		optionst.setFont(Font.font ("Verdana", 20));
		ogrid.add(optionst, 0, 0, 3, 1); 
			
		Text soundt = new Text("Music: ");
		ogrid.add(soundt, 0, 2);
		ToggleGroup sound = new ToggleGroup();
		RadioButton sound1 = new RadioButton("On");
		sound1.setToggleGroup(sound);
		sound1.setSelected(true);
		RadioButton sound2 = new RadioButton("Off");
		sound2.setToggleGroup(sound);
		ogrid.add(sound1, 1, 2);
		ogrid.add(sound2, 2, 2);
			
		Text volt = new Text("Volume: ");
		ogrid.add(volt, 0, 3); 
		Slider volume = new Slider(0, 100, 50);
		volume.setShowTickMarks(true);
		volume.setShowTickLabels(true);
		ogrid.add(volume, 1, 3, 2, 1); 
			
		Text resot = new Text("Resolution: ");
		ogrid.add(resot, 0, 4); 
		CheckBox fullscreen = new CheckBox("Full Screen");
		ogrid.add(fullscreen, 1, 4);
		ChoiceBox<String> resolution = new ChoiceBox<>(FXCollections.observableArrayList("800x600", "1024x768", "1280x720", "1366x768", "1440x900", "1920x1080"));
		resolution.getSelectionModel().selectFirst();
		ogrid.add(resolution, 2, 4);
			
		Text gammat = new Text("Gamma: ");
		ogrid.add(gammat, 0, 5); 
		Slider gamma = new Slider(0, 100, 50);
		gamma.setShowTickMarks(true);
		gamma.setShowTickLabels(true);
		ogrid.add(gamma, 1, 5, 2, 1); 
			
		Button oexit = new Button("Close");                    //options-exit, click closes options-window
		ogrid.add(oexit, 1, 8);
		oexit.setOnAction(new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent e) {
			ostage.close();
			}
		});
			
		Scene oscene = new Scene(ogrid, 800, 500);
		ostage.setScene(oscene);
		ostage.show();
	}
	
	@Override
	public void start(Stage ostage) { }
 
}
