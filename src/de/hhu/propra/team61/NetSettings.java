package de.hhu.propra.team61;

import de.hhu.propra.team61.GUI.BigStage;
import de.hhu.propra.team61.GUI.CustomGrid;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

import static de.hhu.propra.team61.JavaFxUtils.toHex;

/**
 * Created by Jessypet on 27.05.14.
 */
public class NetSettings extends Application {

    TextField ipField = new TextField();
    BigStage stageToClose;
    TextField nameField = new TextField();
    CheckBox spectator = new CheckBox("Spectator");

    public void openPopUp(BigStage stageToClose) {
        this.stageToClose = stageToClose;
        Stage netpopup = new Stage();
        netpopup.setTitle("Start network game");
        netpopup.setWidth(500);
        netpopup.setHeight(250);
        netpopup.setResizable(false);
        CustomGrid popGrid = new CustomGrid();
        popGrid.setAlignment(Pos.CENTER_LEFT);
        popGrid.getColumnConstraints().add(new ColumnConstraints(110));
        popGrid.getColumnConstraints().add(new ColumnConstraints(210));

        Text enterName = new Text("Enter your name/team-name:");
        popGrid.add(enterName, 0, 0, 2, 1);
        popGrid.add(nameField, 2, 0);
        Text ipError = new Text();
        popGrid.add(ipError, 0, 6);
        Button hostGame = new Button("Host a game");
        popGrid.add(hostGame, 0, 1);
        hostGame.setOnAction(new EventHandler<ActionEvent>() {  //Click on Button 'mexit' closes window
            @Override
            public void handle(ActionEvent e) {
                if (nameField.getText().length() > 0) {
                    netpopup.close();
                    NetLobby netlobby = new NetLobby(nameField.getText(), stageToClose);
                } else {
                    ipError.setText("Error: No name entered.");
                }
            }
        });
        Button joinGame = new Button("Join a game");
        popGrid.add(joinGame, 0, 2);
        joinGame.setOnAction(new EventHandler<ActionEvent>() {  //Click on Button 'mexit' closes window
            @Override
            public void handle(ActionEvent e) {
                if (ipField.getText().length() > 0 && nameField.getText().length() > 0) {
                    netpopup.close();
                    //TODO check if max. number of teams reached
                    NetLobby netlobby = new NetLobby(ipField.getText(), spectator.isSelected(), nameField.getText(), stageToClose);
                } else {
                    ipError.setText("Error: No IP-Address or name entered.");
                }
            }
        });
        ipField.setPromptText("Enter the IP-Address.");
        popGrid.add(ipField, 1, 2);
        popGrid.add(spectator, 2, 2);
        Text note = new Text("Note: If you're on the same computer as the host,");
        Text note2 = new Text("type in 'localhost'.");
        popGrid.add(note, 1, 3, 2, 1);
        popGrid.add(note2, 1, 4);
        Scene popScene = new Scene(popGrid);
        netpopup.setScene(popScene);
        netpopup.show();
    }

    @Override
    public void start(Stage filler) {}
}
