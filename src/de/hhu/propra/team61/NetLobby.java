package de.hhu.propra.team61;

import de.hhu.propra.team61.GUI.BigStage;
import de.hhu.propra.team61.GUI.CustomGrid;
import de.hhu.propra.team61.IO.TerrainManager;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Created by Jessypet on 27.05.14.
 */
public class NetLobby extends Application {

    TextField hostName = new TextField();
    TextField name2 = new TextField();
    TextField name3 = new TextField();
    TextField name4 = new TextField();
    ColorPicker hostColorPicker = new ColorPicker();
    ColorPicker colorPicker2 = new ColorPicker();
    ColorPicker colorPicker3 = new ColorPicker();
    ColorPicker colorPicker4 = new ColorPicker();
    ArrayList<String> players;
    ArrayList<String> spectators = new ArrayList<>();
    TextField weapon1 = new TextField("50");
    TextField weapon2 = new TextField("50");
    TextField weapon3 = new TextField("5");
    TextField sizefield = new TextField("4");
    TextField numberOfTeams = new TextField("2");
    ChoiceBox<String> mapChooser = new ChoiceBox<>();

    public NetLobby(String hostName, BigStage stageToClose) {        //Constructor for host
        this.hostName.setText(hostName);
        buildGUI(stageToClose);
    }

    public NetLobby(String ipAdress, Boolean spectator, String name, BigStage stageToClose) {        //Constructor for player
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
        overviewGrid.add(hostName, 1, 3);
        overviewGrid.add(hostColorPicker, 2, 3);

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

        Text generalSettings = new Text("Choose general settings:");
        generalSettings.setFont(Font.font(16));
        overviewGrid.add(generalSettings, 0, 6, 2, 1);
        Text teamSize = new Text("Size of teams: ");
        overviewGrid.add(teamSize, 0, 7);
        overviewGrid.add(sizefield, 1, 7);
        Text teamNumber = new Text("Max. number of teams: ");
        overviewGrid.add(teamNumber, 2, 7);
        overviewGrid.add(numberOfTeams, 3, 7);
        Text chooseMapText = new Text("Choose map:");
        overviewGrid.add(chooseMapText, 0, 8);
        ArrayList<String> availableLevels = getLevels();
        int numberOfLevels = TerrainManager.getNumberOfAvailableTerrains();
        for (int i=0; i<numberOfLevels; i++) {
            mapChooser.getItems().add(availableLevels.get(i));
        }
        mapChooser.getSelectionModel().selectFirst();
        overviewGrid.add(mapChooser, 1, 8);

        Text enter = new Text ("Enter the quantity of projectiles for each weapon:");
        enter.setFont(Font.font(14));
        overviewGrid.add(enter, 0, 9, 3, 1);
        Text w1 = new Text("Weapon 1: ");
        overviewGrid.add(w1, 0, 10);
        overviewGrid.add(weapon1, 1, 10);
        Text w2 = new Text("Weapon 2: ");
        overviewGrid.add(w2, 0, 11);
        overviewGrid.add(weapon2, 1, 11);
        Text w3 = new Text("Weapon 3: ");
        overviewGrid.add(w3, 0, 12);
        overviewGrid.add(weapon3, 1, 12);

        VBox rightBox = new VBox();
        CustomGrid listGrid = new CustomGrid();
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
        VBox spectatorBox = new VBox();                     //TODO get names of spectators
        Text spectatorText = new Text("Spectators:");
        spectatorBox.getChildren().add(spectatorText);
        for (int i=0; i<spectators.size(); i++) {
            Text newSpectator = new Text(spectators.get(i));
            spectatorBox.getChildren().add(newSpectator);
        }
        return spectatorBox;
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

    public ArrayList<String> getLevels() {
        ArrayList<String> levels = TerrainManager.getAvailableTerrains();
        return levels;
    }

    @Override
    public void start(Stage filler) {}
}
