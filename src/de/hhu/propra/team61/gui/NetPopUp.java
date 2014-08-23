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
        netpopup.setHeight(140);
        netpopup.setResizable(true);
        CustomGrid popGrid = new CustomGrid();
        popGrid.setAlignment(Pos.CENTER_LEFT);
        popGrid.getColumnConstraints().add(new ColumnConstraints(110));
        popGrid.getColumnConstraints().add(new ColumnConstraints(210));

        Text nameFieldLabel = new Text("Your name: ");
        Text errorLabel = new Text();
        Button hostGameButton = new Button("Host a game");
        hostGameButton.setOnAction(e -> {
            if (nameField.getText().length() > 0) {
                netpopup.close();
                NetLobby netlobby = new NetLobby(nameField.getText(), sceneController);
            } else {
                errorLabel.setText("Please enter your name.");
            }
        });
        Button joinGameButton = new Button("Join a game");
        joinGameButton.setOnAction(e -> {
            if (ipField.getText().length() > 0 && nameField.getText().length() > 0) {
                netpopup.close();
                NetLobby netlobby = new NetLobby(ipField.getText(), nameField.getText(), sceneController);
            } else {
                errorLabel.setText("You forgot to enter the IP address or name.");
            }
        });
        ipField.setText(Server.getFirstIp());
        Text ipFieldLabel = new Text("IP address of host: ");
        popGrid.add(nameField, 1, 0); // keep tab order in mind
        popGrid.add(nameFieldLabel, 0, 0);
        popGrid.add(ipField, 1, 1);
        popGrid.add(ipFieldLabel, 0, 1);
        popGrid.add(hostGameButton, 2, 0);
        popGrid.add(joinGameButton, 2, 1);
        popGrid.add(errorLabel, 0, 3, 3, 1);
        Scene popScene = new Scene(popGrid);
        netpopup.setScene(popScene);
        netpopup.show();
        nameField.requestFocus();
    }

    @Override
    public void start(Stage filler) {}
}
