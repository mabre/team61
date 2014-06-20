package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.*;
import de.hhu.propra.team61.io.GameState;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Server;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.IOException;

/**
 * Created by Jessypet on 04.06.14.
 */
public class MenueController {

    Server server;
    Thread serverThread;
    Client client;
    Thread clientThread;
    BigStage mainwindow;
    SceneController sceneController;

    public void setMainWindow(BigStage mainwindow, SceneController sceneController) {
        this.mainwindow = mainwindow;
        this.sceneController = sceneController;
    }

    @FXML
    public void handleStartLocal() {
        GameSettings gamesettings = new GameSettings(sceneController);
        gamesettings.doSettings();
    }

    @FXML
    public void handleStartNetwork() {
        NetPopUp netPopUp = new NetPopUp();
        netPopUp.openPopUp(sceneController);
    }

    @FXML
    public void handleStartSaved() {
    // our local game is also client/server based, with server running on localhost
        serverThread = new Thread(server = new Server(() -> {
            clientThread = new Thread(client = new Client(() -> {
                Platform.runLater(() -> new MapWindow(GameState.getSavedGameState(), "SETTINGS_FILE.conf", client, clientThread, server, serverThread, sceneController));
            }));
            clientThread.start();
        }));
        serverThread.start();
    }

    @FXML
    public void handleCustomize() {
        CustomizeWindow customizeWindow = new CustomizeWindow(sceneController);
    }

    @FXML
    public void handleOptions() throws IOException {
            OptionsWindow optionWindow = new OptionsWindow(sceneController);
            optionWindow.doOptions();
    }

    @FXML
    public void handleExit() {
            mainwindow.close();
    }

}
