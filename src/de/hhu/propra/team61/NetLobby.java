package de.hhu.propra.team61;

import de.hhu.propra.team61.GUI.BigStage;
import de.hhu.propra.team61.GUI.CustomGrid;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Created by Jessypet on 27.05.14.
 */
public class NetLobby extends Application {

    public NetLobby(JSONObject theSettings, Stage stageToClose) {
        BigStage lobby = new BigStage("Lobby");

        CustomGrid lobbyGrid = new CustomGrid();
        lobbyGrid.setAlignment(Pos.CENTER);
        Text test = new Text("test");


        Scene lobbyScene = new Scene(lobbyGrid);
        lobby.setScene(lobbyScene);
        lobby.show();
        stageToClose.close();
    }

    @Override
    public void start(Stage filler) {}
}
