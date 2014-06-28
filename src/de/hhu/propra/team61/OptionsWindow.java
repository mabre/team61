package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.OptionController;
import de.hhu.propra.team61.gui.SceneController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * This class shows options.
 *
 * Created by Jessypet on 11.05.14.
 *
 */

public class OptionsWindow extends Application {

    /** Controls GUI-elements from FXML-file */
    private OptionController optionController = new OptionController();
    /** root for optionScene */
    private Pane root;

    /**
     * Loads GUI from FXML-file.
     * @param sceneController used to switch back to menue
     * @throws IOException in case the FXML-file does not exist
     */
    public OptionsWindow(SceneController sceneController) throws IOException {
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
	public void start(Stage filler) { }
 
}
