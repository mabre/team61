package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.*;
import de.hhu.propra.team61.io.GameState;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Server;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.IOException;

/**
 * Controls the main menue.
 *
 * Contains the methods triggered by the FXML file when a button in the menue is clicked.
 *
 * Created by Jessypet on 04.06.14.
 */
public class MenueController {

    Server server;
    Thread serverThread;
    Client client;
    Thread clientThread;
    /** stage of the game */
    private BigStage mainwindow;
    /** used to switch to different scenes */
    SceneController sceneController;

    /**
     * Initializes stage and sceneController.
     * @param mainwindow stage of the game
     * @param sceneController used to switch to different scenes
     */
    public void setMainWindow(BigStage mainwindow, SceneController sceneController) {
        this.mainwindow = mainwindow;
        this.sceneController = sceneController;
    }

    /**
     * Switches to game settings.
     */
    @FXML
    public void handleStartLocal() {
        GameSettings gamesettings = new GameSettings(sceneController);
    }

    /**
     * Opens pop-up to start network game.
     */
    @FXML
    public void handleStartNetwork() {
        NetPopUp netPopUp = new NetPopUp(sceneController);
    }

    /**
     * Loads the last played game.
     */
    @FXML
    public void handleStartSaved() {
    // our local game is also client/server based, with server running on localhost
        serverThread = new Thread(server = new Server(() -> {
            clientThread = new Thread(client = new Client(() -> {
                Platform.runLater(() -> new MapWindow(GameState.getSavedGameState(), client, clientThread, server, serverThread, sceneController));
            }));
            clientThread.start();
        }));
        serverThread.start();
    }

    /**
     * Switches to Customize.
     */
    @FXML
    public void handleCustomize() {
        CustomizeWindow customizeWindow = new CustomizeWindow(sceneController);
    }

    /**
     * Opens options.
     * @throws IOException in case the file loaded in {@link de.hhu.propra.team61.OptionsWindow} does not exist.
     */
    @FXML
    public void handleOptions() throws IOException {
            OptionsWindow optionWindow = new OptionsWindow(sceneController);
    }

    /**
     * Opens credits.
     */
    @FXML
    public void handleCredits() {
        CreditsWindow creditsWindow = new CreditsWindow(sceneController);
    }

    /**
     * Closes the game.
     */
    @FXML
    public void handleExit() {
            mainwindow.close();
    }

}
