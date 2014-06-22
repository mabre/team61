package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.io.CustomizeManager;
import de.hhu.propra.team61.io.ItemManager;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.io.Settings;
import de.hhu.propra.team61.io.TerrainManager;
import de.hhu.propra.team61.MapWindow;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Networkable;
import de.hhu.propra.team61.network.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import static de.hhu.propra.team61.JavaFxUtils.toHex;
import static de.hhu.propra.team61.JavaFxUtils.extractPart;

/**
 * Created by Jessypet on 27.05.14.
 */
public class NetLobby extends Application implements Networkable {

    ArrayList<HBox> hboxes = new ArrayList<>();
    ArrayList<ChoiceBox<String>> teamChoosers = new ArrayList();
    ArrayList<Text> readys = new ArrayList<>();
    ArrayList<String> spectators = new ArrayList<>();
    ArrayList<Button> removeButtons = new ArrayList<>();
    ArrayList<TextField> weapons = new ArrayList<>();
    /** number of figures per team */
    TextField sizeField = new TextField("4");
    TextField numberOfTeams = new TextField("1");
    ChoiceBox<String> mapChooser = new ChoiceBox<>();
    CustomGrid overviewGrid = new CustomGrid();
    int teamsCreated = 1;
    Chat chatBox;
    private VBox spectatorBox;
    private CustomGrid listGrid;
    CheckBox spectator = new CheckBox("Spectator");
    SceneController sceneController = new SceneController();
    Button start;
    Button ready;
    Text sameColor = new Text();

    boolean isHost;
    /** -1 = spectator, 0 = host, 1+ = clients */
    int associatedTeam;

    Server server;
    Thread serverThread;
    Client client;
    Thread clientThread;

    /**
     * constructor for the host
     * @param hostName the name of the first team (ie the first team on the host system)
     */
    public NetLobby(String hostName, SceneController sceneController) {
        initializeArrayLists();
        this.isHost = true;
        this.associatedTeam = 0;
        this.sceneController = sceneController;
        serverThread = new Thread(server = new Server(() -> {
            clientThread = new Thread(client = new Client(hostName, () -> {
                client.send("GET_STATUS"); // TODO race condition
                Platform.runLater(() -> buildGUI());
            }));
            clientThread.start();
            client.registerCurrentNetworkable(this);
        }));
        serverThread.start();
        server.registerCurrentNetworkable(this);
    }

    /**
     * constructor for players wanting to join a game
     * @param ipAddress ip address of the server
     * @param name name of the player/team
     */
    public NetLobby(String ipAddress, String name, SceneController sceneController) {
        initializeArrayLists();
        this.isHost = false;
        this.associatedTeam = -1;
        this.sceneController = sceneController;
        clientThread = new Thread(client = new Client(ipAddress, name, () -> {
            client.send("GET_STATUS");
            Platform.runLater(() -> buildGUI());
        }));
        clientThread.start();
        client.registerCurrentNetworkable(this);
    }

    public void buildGUI() {
        sceneController.getStage().setOnCloseRequest(event -> {
            clientThread.interrupt();
            if(serverThread != null) serverThread.interrupt();
            System.out.println("NetLobby threads interrupted");
            client.stop();
            if(server != null) server.stop();
            System.out.println("NetLobby client/server (if any) stopped");
        });

        BorderPane root = new BorderPane();
        HBox topBox = addTopHBox();
        root.setTop(topBox);
        overviewGrid = new CustomGrid();
        overviewGrid.setPrefWidth(672);
        overviewGrid.setPrefHeight(550);
        root.setLeft(overviewGrid);

        Text teamsText = new Text("Teams:");
        teamsText.setFont(Font.font(16));
        overviewGrid.add(teamsText, 0, 8);

        Text team1 = new Text("Team 1");
        hboxes.add(new HBox(20));
        hboxes.get(0).getChildren().addAll(team1, teamChoosers.get(0));
        overviewGrid.add(hboxes.get(0), 0, 10, 3, 1);

        Text generalSettings = new Text("Choose general settings:");
        generalSettings.setFont(Font.font(16));
        overviewGrid.add(generalSettings, 0, 0, 2, 1);
        Text teamSize = new Text("Size of teams: ");
        overviewGrid.add(teamSize, 0, 1);
        overviewGrid.add(sizeField, 1, 1);
        sizeField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                client.send(getStateForNewClient());
            }
        });
        Text teamNumber = new Text("Max. number of teams: ");
        overviewGrid.add(teamNumber, 2, 1, 2, 1);
        overviewGrid.add(numberOfTeams, 4, 1, 2, 1);
        numberOfTeams.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                client.send(getStateForNewClient());
            }
        });
        Text chooseMapText = new Text("Map:");
        overviewGrid.add(chooseMapText, 0, 2);
        ArrayList<String> availableLevels = getLevels();
        int numberOfLevels = TerrainManager.getNumberOfAvailableTerrains();
        for (int i=0; i<numberOfLevels; i++) {
            mapChooser.getItems().add(availableLevels.get(i));
        }
        mapChooser.getSelectionModel().selectFirst();
        overviewGrid.add(mapChooser, 1, 2);
        mapChooser.valueProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue ov, String value, String new_value) {
                client.send(getStateForNewClient());
            }
        });

        Text enter = new Text ("Enter the quantity of projectiles for each weapon:");
        enter.setFont(Font.font(14));
        overviewGrid.add(enter, 0, 3, 3, 1);

        VBox rightBox = new VBox();
        rightBox.setPrefWidth(328);
        listGrid = new CustomGrid();
        listGrid.setPrefHeight(250);
        if (!isHost) {
            listGrid.add(spectator, 0, 0);
            spectator.setSelected(true);
            disableForbiddenSettings(-1);
        }

        generateSpectatorsBox();
        chatBox = new Chat(client);
        chatBox.setPrefHeight(300);
        rightBox.getChildren().addAll(listGrid, chatBox);
        root.setRight(rightBox);

        HBox bottomBox = new HBox();
        Button back = new Button("Back");
        bottomBox.getChildren().addAll(back, sameColor);
        back.setOnAction(e -> {
            sceneController.switchToMenue();
        });
        bottomBox.setId("bottomBox");
        root.setBottom(bottomBox);

        Scene lobbyScene = new Scene(root);
        lobbyScene.getStylesheets().add("file:resources/layout/css/lobby.css");
        overviewGrid.getStyleClass().add("overviewGrid");
        listGrid.getStyleClass().add("listGrid");
        sceneController.setLobbyScene(lobbyScene);
        sceneController.switchToLobby();
    }

    public HBox addTopHBox() {
        HBox topBox = new HBox(850);
        HBox startBox = new HBox();
        Text lobbyText = new Text("Lobby");
        lobbyText.setFont(Font.font("Sans", 20));
        start = new Button("Start");
        start.setDisable(!Server.teamsAreReady()); // enabled when clients are ready
        start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                boolean differentColors = true;
                for (int i=0; i<teamsCreated-1; i++) {
                    JSONObject objecti = CustomizeManager.getSavedSettings("teams/"+teamChoosers.get(i).getValue());
                    for(int h=i+1; h<teamsCreated; h++) {
                        JSONObject objecth = CustomizeManager.getSavedSettings("teams/"+teamChoosers.get(h).getValue());
                        if (objecti.getString("color").equals(objecth.getString("color"))) {
                            differentColors = false;
                        }
                    }
                }
                if (differentColors) {
                    Settings.save(toJson(), "NET_SETTINGS_FILE");
                    System.out.println("Network-GameSettings: saved settings");
                    MapWindow mapwindow = new MapWindow(mapChooser.getValue(), "NET_SETTINGS_FILE.conf", client, clientThread, server, serverThread, sceneController);
                } else {
                    sameColor.setText("You should not choose the same color!");
                }
            }
        });
        ready = new Button("Ready");
        ready.setDisable(true); //enabled when disabling spectator mode
        ready.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                ready.setText("Waiting …");
                ready.setDisable(true);
                disableForbiddenSettings(-1);
                client.send("CLIENT_READY " + toJson());
            }
        });
        startBox.setAlignment(Pos.CENTER_RIGHT);
        startBox.setPrefWidth(400);
        if (isHost) {
            startBox.getChildren().add(start);
        } else {
            startBox.getChildren().add(ready);
        }
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.getChildren().addAll(lobbyText, startBox);
        topBox.setId("topBox");
        return topBox;
    }

    private void generateSpectatorsBox() {
        spectator.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                spectatorBoxChanged(newValue);
            }
        });
        if(spectatorBox != null) listGrid.getChildren().removeAll(spectatorBox);
        spectatorBox = new VBox();
        Text spectatorText = new Text("Spectators & Players:");
        spectatorBox.getChildren().add(spectatorText);
        for (int i=0; i<spectators.size(); i++) {
            Text newSpectator = new Text(spectators.get(i));
            spectatorBox.getChildren().add(newSpectator);
        }
        listGrid.add(spectatorBox, 0, 1);
    }

    private void updateSpectators(JSONObject spectators) {
        this.spectators.clear();
        JSONArray spectatorList = spectators.getJSONArray("spectators");
        for(int i=0; i<spectatorList.length(); i++) {
            this.spectators.add(spectatorList.getJSONObject(i).getString("name") +
                    " (" + spectatorList.getJSONObject(i).getString("team") + ")");
        }
        generateSpectatorsBox();
    }

    public ArrayList<String> getLevels() {
        ArrayList<String> levels = TerrainManager.getAvailableTerrains();
        return levels;
    }

    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("numberOfTeams", numberOfTeams.getText());   //save max. number of teams
        output.put("teamsCreated", teamsCreated);       //save current number of players
        output.put("team-size", sizeField.getText()); //save size of teams
        output.put("map", mapChooser.getValue());

        //TODO Here was originally some code reading out the weapons ArrayList<Textfield> which then had been put into the json
        JSONArray weaponsSettings = new JSONArray();
        weaponsSettings.put(5);
        weaponsSettings.put(4);
        weaponsSettings.put(3);
        weaponsSettings.put(2);
        weaponsSettings.put(1);
        weaponsSettings.put(1);

        output.put("inventory", weaponsSettings);

        JSONArray teams = new JSONArray();
        JSONObject team1 = getJsonForTeam(teamChoosers.get(0).getValue(), readys.get(0));
        teams.put(team1);
        if (teamsCreated > 1) {
            JSONObject team2 = getJsonForTeam(teamChoosers.get(1).getValue(), readys.get(1));
            teams.put(team2);
        }
        if (teamsCreated > 2) {
            JSONObject team3 = getJsonForTeam(teamChoosers.get(2).getValue(), readys.get(2));
            teams.put(team3);
        }
        if (teamsCreated > 3) {
            JSONObject team4 = getJsonForTeam(teamChoosers.get(3).getValue(), readys.get(3));
            teams.put(team4);
        }
        output.put("teams", teams);
        return output;
    }

    public JSONObject getJsonForTeam(String team, Text ready) {
        JSONObject teamObject = CustomizeManager.getSavedSettings("teams/"+team);
        JSONObject teamSettings = new JSONObject();
        teamSettings.put("name", teamObject.getString("name"));
        teamSettings.put("color", teamObject.getString("color"));
        teamSettings.put("figure", teamObject.getString("figure"));
        JSONArray figureNames = new JSONArray();
        JSONObject figureNamesObject = teamObject.getJSONObject("figure-names");
        for (int i=0; i<6; i++) {
            JSONObject figure = new JSONObject();
            figure.put("figure", figureNamesObject.getString("figure"+i));
            figureNames.put(figure);
        }
        teamSettings.put("figure-names", figureNames);
        teamSettings.put("ready", ready.getText());
        return teamSettings;
    }

    public void fromJson(JSONObject json) {
        for (int i=0; i<=3 ;i++) {          //remove all teams first when sending state to new client
            removeTeam(i, false);
        }
        teamsCreated = 0;
        if(json.has("numberOfTeams")) {
            numberOfTeams.setText(json.getString("numberOfTeams"));
        }
        if(json.has("team-size")) {
            sizeField.setText(json.getString("team-size"));
        }
        if(json.has("map")) {
            mapChooser.setValue(json.getString("map"));
        }
        /*if(json.has("weapons")) {
            JSONArray weaponsArray = json.getJSONArray("weapons");
            for (int i=0; i<weaponsArray.length(); i++) {
                weapons.get(i).setText(String.valueOf(weaponsArray.getJSONObject(i).getInt("weapon" + (i + 1))));
            }
        }*/
        if(json.has("teams")) {
            JSONArray teamsArray = json.getJSONArray("teams");
            for(int i=0; i<teamsArray.length(); i++) {
                addTeam(i);
                teamChoosers.get(i).setValue(teamsArray.getJSONObject(i).getString("name")+".json");
                readys.get(i).setText(teamsArray.getJSONObject(i).getString("ready"));
            }
        }
    }

    /**
     * adds the team with the given number
     * @param number team number, counting starts from 0 = host
     */
    public void addTeam(int number) {                   //add a new team
        if(number != teamsCreated) {
            System.out.println("WARNING creating team #" + number + ", but " + teamsCreated + " teams already exist");
        }
        teamsCreated++;
        Text team = new Text("Team " + (number+1));
        hboxes.add(new HBox(20));                   //HBox makes it easier remove a player
        readys.get(number).setText("not ready");
        // TODO Shouldn't we just change the visibility, instead of adding/removing it all the time (less risk for exceptions)?
        if (hboxes.get(number).getChildren().size() != 0) {
            System.out.println("WARNING hboxes.get("+number+").getChildren().size() is not 0, but " + hboxes.get(number).getChildren().size() +
                    ", so we wanted to add a team which already exists.");
            return;
        }
        hboxes.get(number).getChildren().addAll(team, teamChoosers.get(number), readys.get(number), removeButtons.get(number));
        overviewGrid.add(hboxes.get(number), 0, number + 10, 5, 1);
        start.setDisable(!Server.teamsAreReady());
    }

    private void removePlayer(int i) {
        Stage popUp = new Stage();
        Text wantToRemove = new Text("Do you really want to remove this player?");
        Button yes = new Button("Yes");
        yes.setOnAction(e -> {
            popUp.close();
            if(server != null) {
                removeTeam(i, true);
                server.changeTeamByNumber(i, -1); // change team to spectator
                server.sendCommand(getStateForNewClient()); // send new lobby state to clients
            } else {
                spectator.setSelected(true);
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

    /**
     * removes the given team; the team numbers are updated to fill the gap
     * NOTE: Does NOT set the client belonging to that team to spectator mode, and does NOT send the new lobby state to
     * all clients. The caller must assure that this is done when the change is not temporary (e.g. when re-writing the
     * list of teams).
     * @param team the team to be removed
     * @param changeClientsAssociatedTeam // TODO temporary work-around for the case we are removing all teams and re-add them
     */
    private void removeTeam(int team, boolean changeClientsAssociatedTeam) {
        if(teamsCreated < team) {
            System.out.println("WARNING " + teamsCreated + " teams exist, hence cannot remove team #" + team);
            return;
        }

        System.out.println("removing team #" + team);
        if (team != (teamsCreated-1)) {
            for (int i = team; i < Integer.parseInt(numberOfTeams.getText()) - 1; i++) { // go through every player after the one to remove and move names and colors
                teamChoosers.get(i).setValue(teamChoosers.get(i + 1).getValue());
                teamChoosers.get(i+1).getSelectionModel().selectFirst(); // do that so that the last team will be empty afterwards
                readys.get(i).setText(readys.get(i+1).getText());
                readys.get(i+1).setText("not ready");
                if(server != null && changeClientsAssociatedTeam) server.changeTeamByNumber(i+1, i);
            }
        }

        hboxes.get(teamsCreated-1).getChildren().clear();      // remove the last fields so that the number of players is reduced
        overviewGrid.getChildren().removeAll(hboxes.get(teamsCreated-1));
        teamsCreated--;
    }

    public void initializeArrayLists() {
        /*for (int j=0; j<=4; j++) {            //TODO fix
            overviewGrid.add(new Text("Weapon "+(j+1)), 0, (j+4));
            weapons.add(new TextField("50"));
            overviewGrid.add(weapons.get(j), 1, (j+4));
        }*/
        for (int i=0; i<=3; i++) {
            teamChoosers.add(new ChoiceBox<>(FXCollections.observableArrayList(getTeams())));
            teamChoosers.get(i).getSelectionModel().selectFirst();
            readys.add(new Text("ready"));
            removeButtons.add(new Button("X"));
            removeButtons.get(i).getStyleClass().add("removeButton");
            final int finalI = i;
            removeButtons.get(i).setOnAction(e -> {
                removePlayer(finalI);
            });
        }
    }

    public ArrayList<String> getTeams() {
        ArrayList<String> availableTeams = CustomizeManager.getAvailableTeams();
        return availableTeams;
    }

    @Override
    public void start(Stage filler) {}

    @Override
    public void handleOnClient(String command) {
        if(command.startsWith("STATUS MAPWINDOW")) {
            JSONObject state = new JSONObject(extractPart(command, "STATUS MAPWINDOW "));
            new MapWindow(state, client, clientThread, sceneController);
        } else if(command.startsWith("STATUS LOBBY")) {
            JSONObject state = new JSONObject(extractPart(command, "STATUS LOBBY "));
            if(server == null) fromJson(state); // the server has the current state, do not overwrite it (has side-effects)
        } else if(command.startsWith("SPECTATOR_LIST")) {
            JSONObject spectators = new JSONObject(extractPart(command, "SPECTATOR_LIST "));
            updateSpectators(spectators);
        } else if(command.startsWith("SET_TEAM_NUMBER")) {
            setAssociatedTeam(Integer.parseInt(extractPart(command, "SET_TEAM_NUMBER ")));
        } else if(command.contains("CHAT ")) {
            chatBox.processChatCommand(command);
        } else {
            System.out.println("NetLobby: unknown command " + command);
        }
    }

    private void setAssociatedTeam(int newTeam) {
        associatedTeam = newTeam;
        disableForbiddenSettings(associatedTeam);
        spectator.setSelected(associatedTeam == -1);
        System.out.println("I belong to team " + associatedTeam);
    }

    public void spectatorBoxChanged(boolean isChecked) {
        if (isChecked) {
            System.out.println("Spectator is checked");
            if (!isHost) {
                disableForbiddenSettings(-1);
            }
            client.send("SPECTATOR CHECKED");
        } else {
            System.out.println("Spectator is unchecked");
            if (!isHost) {
                disableForbiddenSettings(associatedTeam);
            }
            client.send("SPECTATOR UNCHECKED");
        }
    }

    private void disableForbiddenSettings(int team) {
        if (!isHost) {
            /*for (int i=0; i<=4; i++) {
                weapons.get(i).setDisable(true);
            }*/
            sizeField.setDisable(true);
            numberOfTeams.setDisable(true);
            mapChooser.setDisable(true);
        }
        for (int i=0; i<=3; i++) {
            teamChoosers.get(i).setDisable(i != team);
            removeButtons.get(i).setDisable(true);
        }
        ready.setDisable(team == -1);
    }

    /**********************************************************************************************/
    /*********************************** SERVER CODE **********************************************/
    /**********************************************************************************************/

    @Override
    public void handleKeyEventOnServer(String keyCode) {
        System.out.println("server handling command in lobby: " + keyCode);
        if (keyCode.contains("SPECTATOR ")) {
            boolean checked = !(keyCode.contains("UNCHECKED"));
            int currentTeam = Integer.parseInt(extractPart(keyCode, "CHECKED "));
            String clientId = keyCode.split(" ", 2)[0];
            handleSpectatorBoxChanged(checked, currentTeam, clientId);
        } else if (keyCode.startsWith("READY")) {
            int team = Integer.parseInt(keyCode.split(" ", 3)[1]);
            JSONObject clientSettings = new JSONObject(keyCode.split(" ", 3)[2]);
            applySettingsFromClient(clientSettings, team);
            setTeamReady(team);
        } else {
            System.out.println("Lobby handleKeyEventOnServer: unknown command " + keyCode);
        }
    }

    /**
     * applies a given settings object, but only the part which the given team is allowed to change, and sends new state to clients
     * @param clientSettings a JSONObject containing settings
     * @param team the team wanting to change settings (counting starts from 0=host)
     */
    private void applySettingsFromClient(JSONObject clientSettings, int team) {
        JSONObject currentSettings = toJson();
        // clients may only change the team settings, so replace the currently set team settings with the settings send from the team

        JSONArray teamsArray = currentSettings.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            if(i == team) {
                teamsArray.getJSONObject(i).put("name", clientSettings.getJSONArray("teams").getJSONObject(i).getString("name"));
                teamsArray.getJSONObject(i).put("color", clientSettings.getJSONArray("teams").getJSONObject(i).getString("color"));
            }
        }

        fromJson(currentSettings);
        server.sendCommand(getStateForNewClient());
    }

    private void handleSpectatorBoxChanged(boolean isSpectating, int currentTeam, String clientId) {
        if(isSpectating) {
            if (currentTeam < 1) {
//                throw new IllegalArgumentException("Cannot remove team " + currentTeam);
                System.out.println("ERROR: Cannot remove team " + currentTeam);
                return;
            }
            removeTeam(currentTeam, true);
            server.changeTeamById(clientId, -1);
        } else {
            System.out.println("handleSpectatorBoxChanged: current number of teams: " + teamsCreated);
            if (currentTeam != -1) {
//                throw new IllegalArgumentException("Team requested, but already in team " + currentTeam);
                System.out.println("ERROR: Team requested, but already in team " + currentTeam);
                return;
            }
            if(teamsCreated < Integer.parseInt(numberOfTeams.getText())) {
                addTeam(teamsCreated);
                server.changeTeamById(clientId, teamsCreated-1); // associate client with last team
            } else {
                System.out.println("Max. number of teams reached.");
                server.changeTeamById(clientId, -1); // will reset the client's spectator checkbox
            }
        }

        server.sendCommand(getStateForNewClient());
    }

    /**
     * marks the given team as ready, informs all clients about the change, and enables the Start button if everyone is ready
     * @param team the team which is ready
     */
    private void setTeamReady(int team) {
        if(team < 1) throw new IllegalArgumentException("Team " + team + " cannot change to ready state.");

        System.out.println("Team #" + team + " is ready");
        readys.get(team).setText("ready");
        server.sendCommand(getStateForNewClient());

        if(server.teamsAreReady()) start.setDisable(false);
    }

    @Override
    public String getStateForNewClient() {
        return "STATUS LOBBY " + this.toJson().toString();
    }
}
