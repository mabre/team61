package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.MapWindow;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Networkable;
import de.hhu.propra.team61.network.Server;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static de.hhu.propra.team61.JavaFxUtils.extractPart;

/**
 * Created by Jessypet on 28.05.14.
 */

public class GameOverWindow extends Application implements Networkable {

    Server server;
    Client client;
    Thread clientThread, serverThread;
    SceneController sceneController;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * displays a game over window
     * @param sceneController
     * @param winnerTeam the number of the winner team (counting starts from 0, -1 means draw)
     * @param winnerName the name of the winner team
     * @param map
     * @param file
     * @param client the client object
     * @param clientThread the thread managing the client connection
     * @param server the server object
     * @param serverThread the thread managing the server connection
     */
    public void showWinner(SceneController sceneController, int winnerTeam, String winnerName, String map, String file, Client client, Thread clientThread, Server server, Thread serverThread) {
        BorderPane root = new BorderPane();
        this.sceneController = sceneController;
        this.server = server;
        this.serverThread = serverThread;
        this.client = client;
        this.clientThread = clientThread;

        sceneController.getStage().setOnCloseRequest((e) -> {
            shutdown();
        });
        CustomGrid overGrid = new CustomGrid();
        overGrid.setId("overGrid");
        overGrid.setAlignment(Pos.CENTER_RIGHT);
        VBox gridBox = new VBox();
        gridBox.setId("gridBox");
        gridBox.getChildren().add(overGrid);
        root.setRight(gridBox);
        Text winner = new Text();
        if (winnerTeam == -1) {
            winner.setText("A tie! You're both winners!");
        } else {
            winner.setText("The winner is team " +winnerName+ ".");
        }
        winner.setFont(Font.font("Verdana", 20));
        overGrid.add(winner, 0, 0, 2, 1);
        Text whatNext = new Text("What do you want to do next?");
        overGrid.add(whatNext, 0, 1, 2, 1);
        Button revenge = new Button("Revenge");
        Text revengeText = new Text("Play another game with the same settings.");
        overGrid.add(revenge, 0, 3);
        overGrid.add(revengeText, 1, 3);
        revenge.setOnAction(e -> {
            MapWindow mapwindow = new MapWindow(map, file, client, clientThread, server, serverThread, sceneController);
        });
        if(server != null) {
            server.registerCurrentNetworkable(this);
        } else {
            revenge.setDisable(true);
        }
        client.registerCurrentNetworkable(this);
        Button end = new Button("End");
        end.setOnAction(e -> {
            shutdown();
            sceneController.switchToMenue();
        });
        overGrid.add(end, 0, 4);
        Scene overScene = new Scene(root);
        overScene.getStylesheets().add("file:resources/layout/css/gameover.css");
        sceneController.setGameOverScene(overScene);
        sceneController.switchToGameOver();
    }

    @Override
    public void start(Stage filler) { }


    private void shutdown() {
        clientThread.interrupt();
        if(serverThread != null) serverThread.interrupt();
        System.out.println("GameOverWindow: threads interrupted");
        client.stop();
        if(server != null) server.stop();
        System.out.println("GameOverWindow: client/server (if any) stopped");
    }

    @Override
    public void handleOnClient(String command) {
        if(command.startsWith("STATUS MAPWINDOW")) {
            JSONObject state = new JSONObject(extractPart(command, "STATUS MAPWINDOW "));
            new MapWindow(state, client, clientThread, sceneController);
//        } else if(command.contains("CHAT ")) { // TODO add chat ?
//            chatBox.processChatCommand(command);
        } else {
            System.out.println("NetLobby: unknown command " + command);
        }
    }

    @Override
    public void handleOnServer(String command) {

    }

    @Override
    public String getStateForNewClient() {
        return null;
    }
}
