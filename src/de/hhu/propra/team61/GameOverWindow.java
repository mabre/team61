package de.hhu.propra.team61;

import de.hhu.propra.team61.GUI.BigStage;
import de.hhu.propra.team61.GUI.CustomGrid;
import de.hhu.propra.team61.Network.Client;
import de.hhu.propra.team61.Network.Server;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Created by Jessypet on 28.05.14.
 */

public class GameOverWindow extends Application {

    Server server;
    Client client;
    Thread clientThread, serverThread;

    public static void main(String[] args) {
        launch(args);
    }

    public void showWinner(int currentTeam, Stage stageToGoBack, String map, String file, Client client, Thread clientThread, Server server, Thread serverThread) {
        this.server = server;
        this.serverThread = serverThread;
        this.client = client;
        this.clientThread = clientThread;

        BigStage overStage = new BigStage("Game over");
        overStage.setOnCloseRequest((e) -> {
            shutdown();
        });
        CustomGrid overGrid = new CustomGrid();
        overGrid.setAlignment(Pos.CENTER);
        Text winner = new Text("The winner is team " + (currentTeam+1) + ".");
        winner.setFont(Font.font("Verdana", 20));
        overGrid.add(winner, 0, 0, 2, 1);
        Text whatNext = new Text("What do you want to do next?");
        overGrid.add(whatNext, 0, 1, 2, 1);
        Button revenge = new Button("Revenge");
        Text revengeText = new Text("Play another game with the same settings.");
        overGrid.add(revenge, 0, 3);
        overGrid.add(revengeText, 1, 3);
        revenge.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                MapWindow mapwindow = new MapWindow(map, stageToGoBack, file, client, clientThread, server, serverThread);
                overStage.close();
            }
        });
        Button end = new Button("End");
        end.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                shutdown();
                stageToGoBack.show();
                overStage.close();
            }
        });
        overGrid.add(end, 0, 4);
        Scene overScene = new Scene(overGrid);
        overStage.setScene(overScene);
        overStage.show();
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
}
