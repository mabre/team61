package de.hhu.propra.team61;

import de.hhu.propra.team61.GUI.BigStage;
import de.hhu.propra.team61.GUI.CustomGrid;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Created by Jessypet on 27.05.14.
 */
public class NetLobby extends Application {

    TextField nameHost = new TextField();
    TextField name2 = new TextField();
    TextField name3 = new TextField();
    TextField name4 = new TextField();
    ColorPicker colorPickerHost = new ColorPicker();
    ColorPicker colorPicker2 = new ColorPicker();
    ColorPicker colorPicker3 = new ColorPicker();
    ColorPicker colorPicker4 = new ColorPicker();
    ArrayList<String> players;
    ArrayList<String> spectators;
    int numberOfTeams;

    public NetLobby(JSONObject settings, String map, BigStage stageToClose) {        //Constructor for host
        this.numberOfTeams = Integer.parseInt(settings.getString("numberOfTeams"));
        JSONObject host = settings.getJSONObject("teamhost");
        this.nameHost.setText(host.getString("name"));
        this.colorPickerHost.setValue(Color.web(host.getString("color")));
        buildGUI(stageToClose);
    }

    public NetLobby(String ipAdress, Boolean spectator, BigStage stageToClose) {        //Constructor for player
        //TODO use ipAdress
        buildGUI(stageToClose);
    }


    public void buildGUI(BigStage stageToClose) {
        BigStage lobby = new BigStage("Lobby");
        lobby.setOnCloseRequest(event -> {
            lobby.close();
            stageToClose.show();
        });
        BorderPane root = new BorderPane();
        HBox topBox = addTopHBox();
        root.setTop(topBox);
        CustomGrid overviewGrid = new CustomGrid();
        root.setLeft(overviewGrid);

        Text teamsText = new Text("Teams:");
        overviewGrid.add(teamsText, 0, 1);
        Text name = new Text("Team-Name");
        overviewGrid.add(name, 1, 2);
        Text color = new Text("Color");
        overviewGrid.add(color, 2, 2);

        Text team1 = new Text("Team 1");
        overviewGrid.add(team1, 0, 3);
        overviewGrid.add(nameHost, 1, 3);
        overviewGrid.add(colorPickerHost, 2, 3);

        Text team2 = new Text("Team 2");
        overviewGrid.add(team2, 0, 4);
        overviewGrid.add(name2, 1, 4);
        overviewGrid.add(colorPicker2, 2, 4);
        Button rmTeam2 = new Button("X");
        rmTeam2.getStyleClass().add("removeButton");
        overviewGrid.add(rmTeam2, 3, 4);
        rmTeam2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                removePlayer(name2.getText());
            }
        });
        //TODO show more teams

        VBox rightBox = new VBox();
        CustomGrid listGrid = new CustomGrid();
        VBox players = addPlayerList();
        listGrid.add(players, 0, 0);
        VBox spectators = addSpectatorList();
        listGrid.add(spectators, 2, 0);
        VBox chatBox = doChat();
        rightBox.getChildren().addAll(listGrid, chatBox);
        root.setRight(rightBox);

        HBox bottomBox = new HBox();
        Button back = new Button("Back");
        bottomBox.getChildren().add(back);
        back.setOnAction(new EventHandler<ActionEvent>() {  //Click on Button 'mexit' closes window
            @Override
            public void handle(ActionEvent e) {
                stageToClose.show();
                lobby.close();
            }
        });
        bottomBox.setId("bottomBox");
        root.setBottom(bottomBox);

        Scene lobbyScene = new Scene(root);
        lobbyScene.getStylesheets().add("file:resources/layout/css/lobby.css");
        overviewGrid.getStyleClass().add("overviewGrid");
        listGrid.getStyleClass().add("listGrid");
        lobby.setScene(lobbyScene);
        lobby.show();
        stageToClose.close();
    }

    public HBox addTopHBox() {
        HBox topBox = new HBox(850);
        HBox startBox = new HBox();
        Text lobbyText = new Text("Lobby");
        lobbyText.setFont(Font.font("Sans", 20));
        Button start = new Button("Start");
        startBox.setAlignment(Pos.CENTER_RIGHT);
        startBox.getChildren().add(start);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.getChildren().addAll(lobbyText, startBox);
        topBox.setId("topBox");
        return topBox;
    }

    public VBox addSpectatorList() {
        VBox spectatorBox = new VBox();
        Text spectatorText = new Text("Spectators:");
        Text player1 = new Text("Bobby");                               //TODO get names of players and spectators
        spectatorBox.getChildren().addAll(spectatorText, player1);
        spectatorBox.setId("vbox");
        return spectatorBox;
    }

    public VBox addPlayerList() {
        VBox playerBox = new VBox();
        Text playerText = new Text("Players:");
        Text player1 = new Text("Jeff");
        playerBox.getChildren().addAll(playerText, player1);
        playerBox.setId("vbox");
        return playerBox;
    }

    public VBox doChat() {
        VBox chatBox = new VBox();
        chatBox.setId("chatBox");
        Text chatHere = new Text("Chat will be here.");
        chatBox.getChildren().add(chatHere);
        //TODO chat
        return chatBox;
    }

    public void removePlayer(String name) {
        Stage popUp = new Stage();
        Text wantToRemove = new Text("Do you really want to remove this player?");
        Button yes = new Button("Yes");
        yes.setOnAction(new EventHandler<ActionEvent>() {  //Click on Button 'mexit' closes window
            @Override
            public void handle(ActionEvent e) {
                //TODO remove player
            }
        });
        Button no = new Button("No");
        no.setOnAction(new EventHandler<ActionEvent>() {  //Click on Button 'mexit' closes window
            @Override
            public void handle(ActionEvent e) {
                popUp.close();
            }
        });
        CustomGrid removeGrid = new CustomGrid();
        removeGrid.add(wantToRemove, 0, 0, 2, 1);
        removeGrid.add(yes, 0, 1);
        removeGrid.add(no, 1, 1);
        Scene removeScene = new Scene(removeGrid);
        popUp.setScene(removeScene);
        popUp.show();
    }

    @Override
    public void start(Stage filler) {}
}
