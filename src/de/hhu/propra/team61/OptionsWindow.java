package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.OptionController;
import de.hhu.propra.team61.gui.SceneController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;

/*
 * Created by Jessypet on 11.05.14.
 * This class shows options. 
 *
 */

public class OptionsWindow extends Application {

    SceneController sceneController;
    OptionController optionController = new OptionController();
    Pane root;

    public OptionsWindow(SceneController sceneController) {
        this.sceneController = sceneController;
    }

	public void doOptions() throws IOException {
        FXMLLoader optionLoader = new FXMLLoader(getClass().getResource("gui/options.fxml"));
        root = optionLoader.load();
        optionController = optionLoader.getController();
        Scene optionScene = new Scene(root, 1000, 600);
        optionScene.getStylesheets().add("file:resources/layout/css/options.css");
        sceneController.setOptionsScene(optionScene);
        sceneController.switchToOptions();
        optionController.setSceneController(sceneController);
	}

	@Override
	public void start(Stage ostage) { }
 
}
