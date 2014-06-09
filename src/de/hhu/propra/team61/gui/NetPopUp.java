package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.SceneController;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Shows a modal dialog for hosting/joining a team and entering team name and ip address
 * Created by Jessypet on 27.05.14.
 */
public class NetPopUp extends Application {

    TextField ipField = new TextField();
    TextField nameField = new TextField();

    public void openPopUp(SceneController sceneController) {
        Stage netpopup = new Stage();
        netpopup.initModality(Modality.APPLICATION_MODAL);
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
        hostGame.setOnAction(e -> {
            if (nameField.getText().length() > 0) {
                netpopup.close();
                NetLobby netlobby = new NetLobby(nameField.getText(), sceneController);
            } else {
                ipError.setText("Error: No name entered.");
            }
        });
        Button joinGame = new Button("Join a game");
        popGrid.add(joinGame, 0, 2);
        joinGame.setOnAction(e -> {
            if (ipField.getText().length() > 0 && nameField.getText().length() > 0) {
                netpopup.close();
                //TODO check if max. number of teams reached
                NetLobby netlobby = new NetLobby(ipField.getText(), nameField.getText(), sceneController);
            } else {
                ipError.setText("Error: No IP-Address or name entered.");
            }
        });
        ipField.setPromptText("Enter the IP-Address.");
        ipField.setText("localhost");
        popGrid.add(ipField, 1, 2);
        Text note = new Text("Note: If you’re on the same computer as the host,\ntype in “localhost”.");
        popGrid.add(note, 1, 3, 2, 1);
        Scene popScene = new Scene(popGrid);
        netpopup.setScene(popScene);
        netpopup.show();
    }

    @Override
    public void start(Stage filler) {}
}
