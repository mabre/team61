package de.hhu.propra.team61;

import de.hhu.propra.team61.GUI.BigStage;
import de.hhu.propra.team61.GUI.CustomGrid;
import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.IO.Settings;
import de.hhu.propra.team61.IO.TerrainManager;
import de.hhu.propra.team61.Network.Client;
import de.hhu.propra.team61.Network.Server;
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

import static de.hhu.propra.team61.JavaFxUtils.toHex;

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
    TextField sizeField = new TextField("4");
    TextField numberOfTeams = new TextField("2");
    ChoiceBox<String> mapChooser = new ChoiceBox<>();
    CustomGrid overviewGrid;
    Boolean team3Shown = false;

    BigStage lobby;

    Server server;
    Client client;

    /**
     * constructor for the host
     * @param hostName the name of the first team (ie the first team on the host system)
     * @param stageToClose stage to close when opening the window
     */
    public NetLobby(String hostName, BigStage stageToClose) {
        Thread serverThread = new Thread(server = new Server());
        serverThread.start();

        this.hostName.setText(hostName);
        buildGUI(stageToClose);
    }

    /**
     * constructor for players wanting to join a game
     * @param ipAddress ip address of the server
     * @param spectator true when player wants to be a spectator // TODO hardcoded to true atm
     * @param name name of the player/team
     * @param stageToClose stage to close when opening the window
     */
    public NetLobby(String ipAddress, Boolean spectator, String name, BigStage stageToClose) {
        Thread clientThread = new Thread(client = new Client(ipAddress));
        clientThread.start();

        buildGUI(stageToClose);
    }


    public void buildGUI(BigStage stageToClose) {
        lobby = new BigStage("Lobby");
        lobby.setOnCloseRequest(event -> {
            lobby.close();
            stageToClose.show();
        });
        BorderPane root = new BorderPane();
        HBox topBox = addTopHBox();
        root.setTop(topBox);
        overviewGrid = new CustomGrid();
        root.setLeft(overviewGrid);

        Text teamsText = new Text("Teams:");
        teamsText.setFont(Font.font(16));
        overviewGrid.add(teamsText, 0, 8);
        Text name = new Text("Team-Name");
        overviewGrid.add(name, 1, 9);
        Text color = new Text("Color");
        overviewGrid.add(color, 2, 9);

        Text team1 = new Text("Team 1");
        overviewGrid.add(team1, 0, 10);
        overviewGrid.add(hostName, 1, 10);
        overviewGrid.add(hostColorPicker, 2, 10);

        Text team2 = new Text("Team 2");
        overviewGrid.add(team2, 0, 11);
        overviewGrid.add(name2, 1, 11);
        overviewGrid.add(colorPicker2, 2, 11);
        Button rmTeam2 = new Button("X");
        rmTeam2.getStyleClass().add("removeButton");
        overviewGrid.add(rmTeam2, 3, 11);
        rmTeam2.setOnAction(e -> {
            removePlayer(name2.getText());
        });


        Text generalSettings = new Text("Choose general settings:");
        generalSettings.setFont(Font.font(16));
        overviewGrid.add(generalSettings, 0, 0, 2, 1);
        Text teamSize = new Text("Size of teams: ");
        overviewGrid.add(teamSize, 0, 1);
        overviewGrid.add(sizeField, 1, 1);
        Text teamNumber = new Text("Max. number of teams: ");
        overviewGrid.add(teamNumber, 2, 1);
        overviewGrid.add(numberOfTeams, 3, 1);
        Button update = new Button("Update");
        overviewGrid.add(update, 3, 2);
        update.setOnAction(e -> {
            if (Integer.parseInt(numberOfTeams.getText()) > 2 && team3Shown == false) {  addTeams(3); }
            if (Integer.parseInt(numberOfTeams.getText()) > 3) {  addTeams(4); }
        });
        Text chooseMapText = new Text("Choose map:");
        overviewGrid.add(chooseMapText, 0, 2);
        ArrayList<String> availableLevels = getLevels();
        int numberOfLevels = TerrainManager.getNumberOfAvailableTerrains();
        for (int i=0; i<numberOfLevels; i++) {
            mapChooser.getItems().add(availableLevels.get(i));
        }
        mapChooser.getSelectionModel().selectFirst();
        overviewGrid.add(mapChooser, 1, 2);

        Text enter = new Text ("Enter the quantity of projectiles for each weapon:");
        enter.setFont(Font.font(14));
        overviewGrid.add(enter, 0, 3, 3, 1);
        Text w1 = new Text("Weapon 1: ");
        overviewGrid.add(w1, 0, 4);
        overviewGrid.add(weapon1, 1, 4);
        Text w2 = new Text("Weapon 2: ");
        overviewGrid.add(w2, 0, 5);
        overviewGrid.add(weapon2, 1, 5);
        Text w3 = new Text("Weapon 3: ");
        overviewGrid.add(w3, 0, 6);
        overviewGrid.add(weapon3, 1, 6);

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
        back.setOnAction(e -> {
            stageToClose.show();
            lobby.close();
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
        start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Settings.save(toJson(), "NET_SETTINGS_FILE");               //create Json-object and save it in SETTINGS_FILE.conf
                System.out.println("Network-GameSettings: saved settings");
                MapWindow mapwindow = new MapWindow(mapChooser.getValue(), lobby, "NET_SETTINGS_FILE.conf", null, null, null, null);
            }
        });


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
        Text wantToRemove = new Text("Do you really want to remove player " + name + "?");
        Button yes = new Button("Yes");
        yes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                //TODO remove player
            }
        });
        Button no = new Button("No");
        no.setOnAction(new EventHandler<ActionEvent>() {
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

    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("numberOfTeams", numberOfTeams.getText());   //save number of teams
        output.put("team-size", sizeField.getText()); //save size of teams
        output.put("map", mapChooser.getValue());
        output.put("weapon1", weapon1.getText()); // TODO make array instead of using suffix
        output.put("weapon2", weapon2.getText());
        output.put("weapon3", weapon3.getText());
        JSONArray teams = new JSONArray();
        JSONObject team1 = getJsonForTeam(hostName.getText(), hostColorPicker);
        teams.put(team1);
        JSONObject team2 = getJsonForTeam(name2.getText(), colorPicker2);
        teams.put(team2);
        if (Integer.parseInt(numberOfTeams.getText()) > 2) {
            JSONObject team3 = getJsonForTeam(name3.getText(), colorPicker3);
            teams.put(team3);
        }
        if (Integer.parseInt(numberOfTeams.getText()) > 3) {
            JSONObject team4 = getJsonForTeam(name4.getText(), colorPicker4);
            teams.put(team4);
        }
        output.put("teams", teams);
        return output;
    }

    public JSONObject getJsonForTeam(String name, ColorPicker color) {
        JSONObject team = new JSONObject();
        team.put("name", name);
        team.put("color", toHex(color.getValue()));
        return team;
    }

    public void addTeams(int number) {
        if (number == 3) {
            Text team3 = new Text("Team 3");
            overviewGrid.add(team3, 0, 12);
            overviewGrid.add(name3, 1, 12);
            overviewGrid.add(colorPicker3, 2, 12);
            Button rmTeam3 = new Button("X");
            rmTeam3.getStyleClass().add("removeButton");
            overviewGrid.add(rmTeam3, 3, 12);
            rmTeam3.setOnAction(e -> {
                removePlayer(name3.getText());
            });
            team3Shown = true;
        }
        if (number == 4) {
            Text team4 = new Text("Team 4");
            overviewGrid.add(team4, 0, 13);
            overviewGrid.add(name4, 1, 13);
            overviewGrid.add(colorPicker4, 2, 13);
            Button rmTeam4 = new Button("X");
            rmTeam4.getStyleClass().add("removeButton");
            overviewGrid.add(rmTeam4, 3, 13);
            rmTeam4.setOnAction(e -> {
                removePlayer(name4.getText());
            });
        }
    }

    @Override
    public void start(Stage filler) {}
}
