package de.hhu.propra.team61;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Jessypet on 01.06.14.
 */

public class SceneController {

    public Stage mainwindow;
    public Scene menueScene;
    public Scene optionsScene;
    public Scene settingsScene;
    public Scene lobbyScene;
    public Scene gameScene;
    public Scene gameOverScene;

    public void setStage(Stage mainwindow) {
        this.mainwindow = mainwindow;
    }

    public Stage getStage() {
        return mainwindow;
    }

    public void setMenueScene(Scene menueScene) {
        this.menueScene = menueScene;
    }

    public void setOptionsScene(Scene optionsScene) {
        this.optionsScene = optionsScene;
    }

    public void setSettingsScene(Scene settingsScene) {
        this.settingsScene = settingsScene;
    }

    public void setLobbyScene(Scene lobbyScene) {
        this.lobbyScene = lobbyScene;
    }

    public void setGameScene(Scene gameScene) {
        this.gameScene = gameScene;
    }

    public void setGameOverScene(Scene gameOverScene) {
        this.gameOverScene = gameOverScene;
    }

    public void switchToMenue() {
        mainwindow.setScene(menueScene);
        mainwindow.setTitle("Unicorns and penguins <3");
    }

    public void switchToOptions() {
        mainwindow.setScene(optionsScene);
        mainwindow.setTitle("Options");
    }

    public void switchToGameSettings() {
        mainwindow.setScene(settingsScene);
        mainwindow.setTitle("Game Settings");
    }

    public void switchToLobby() {
        mainwindow.setScene(lobbyScene);
        mainwindow.setTitle("Network Lobby");
    }

    public void switchToMapwindow() {
        mainwindow.setScene(gameScene);
        mainwindow.setTitle("The Playground");
    }

    public void switchToGameOver() {
        mainwindow.setScene(gameOverScene);
        mainwindow.setTitle("Game over");
    }
}
