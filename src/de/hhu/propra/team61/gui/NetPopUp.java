package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.network.Server;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Shows a modal dialog for hosting/joining a team and entering team name and ip address.
 *
 * Created by Jessypet on 27.05.14.
 */
public class NetPopUp extends Application {

    /** to enter ip-address before joining a game */
    private TextField ipField = new TextField();
    /** to enter player-name */
    private TextField nameField = new TextField();

    /**
     * Opens a pop-up where you can either host or join a game. In both cases you need to enter a name, if you want to
     * join you also need to enter the ip-address of the server. If one of them is missing an error message is shown.
     * @param sceneController used to switch to network lobby
     */
    public NetPopUp(SceneController sceneController) {
        Stage netpopup = new Stage();
        netpopup.initModality(Modality.APPLICATION_MODAL);
        netpopup.setTitle("Start network game");
        netpopup.setWidth(500);
        netpopup.setHeight(200);
        netpopup.setResizable(false);
        CustomGrid popGrid = new CustomGrid();
        popGrid.setAlignment(Pos.CENTER_LEFT);
        popGrid.getColumnConstraints().add(new ColumnConstraints(110));
        popGrid.getColumnConstraints().add(new ColumnConstraints(210));

        Text enterName = new Text("Your name:");
        popGrid.add(enterName, 0, 0, 1, 1);
        Text ipError = new Text();
        popGrid.add(ipError, 0, 5);
        Button hostGame = new Button("Host a game");
        hostGame.setOnAction(e -> {
            if (nameField.getText().length() > 0) {
                netpopup.close();
                NetLobby netlobby = new NetLobby(nameField.getText(), sceneController);
            } else {
                ipError.setText("Please enter your name.");
            }
        });
        Button joinGame = new Button("Join a game");
        joinGame.setOnAction(e -> {
            if (ipField.getText().length() > 0 && nameField.getText().length() > 0) {
                netpopup.close();
                NetLobby netlobby = new NetLobby(ipField.getText(), nameField.getText(), sceneController);
            } else {
                ipError.setText("You forgot to enter the IP address or name.");
            }
        });
        ipField.setPromptText("IP address of host:");
        ipField.setText("localhost");
        popGrid.add(nameField, 2, 0); // keep tab order in mind
        popGrid.add(ipField, 1, 2);
        popGrid.add(hostGame, 0, 1);
        popGrid.add(joinGame, 0, 2);
        Text ips = new Text("Your IP address: " + Server.getIps());
        popGrid.add(ips, 1, 1, 2, 1);
        Scene popScene = new Scene(popGrid);
        netpopup.setScene(popScene);
        netpopup.show();
        nameField.requestFocus();
    }

    @Override
    public void start(Stage filler) {}
}
