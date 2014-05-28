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

    public static void main(String[] args) {
        launch(args);
    }

    public void showWinner(int currentTeam, Stage stageToGoBack, String map, String file, Client client, Thread clientThread, Server server, Thread serverThread) {
        BigStage overStage = new BigStage("Game over");
        CustomGrid overGrid = new CustomGrid();
        overGrid.setAlignment(Pos.CENTER);
        Text winner = new Text("The winner is team " + currentTeam + ".");
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
        Button back = new Button("Back");
        back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                stageToGoBack.show();
                overStage.close();
            }
        });
        overGrid.add(back, 0, 4);
        Scene overScene = new Scene(overGrid);
        overStage.setScene(overScene);
        overStage.show();
    }

    @Override
    public void start(Stage filler) { }
}
