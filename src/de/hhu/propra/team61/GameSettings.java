package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.SettingsController;
import de.hhu.propra.team61.gui.SceneController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The window that is shown when starting a local game.
 *<p>
 * This class contains GUI for choosing teams and game style for the game.
 * Custom teams and game styles are loaded from different JSON-Files and, when starting the game, saved in SETTINGS_FILE.conf.
 * <p>
 *
 * Created by Jessypet on 21.05.14.
 */

public class GameSettings extends Application {

    /** Controls GUI-elements from FXML-file */
    private SettingsController settingsController = new SettingsController();
    /** root for settingScene */
    private Pane root;

    public GameSettings(SceneController sceneController) {
        try {
            FXMLLoader settingLoader = new FXMLLoader(getClass().getResource("gui/gamesettings.fxml"));
            root = settingLoader.load();
            settingsController = settingLoader.getController();
            Scene settingScene = new Scene(root, 1000, 600);
            settingScene.getStylesheets().add("file:resources/layout/css/settings.css");
            sceneController.switchScene(settingScene, "Game settings");
            settingsController.initialize(sceneController);
        } catch (IOException e) {
            System.out.println("FXML-file not found");
        }
    }

    @Override
    public void start(Stage filler) {}
}