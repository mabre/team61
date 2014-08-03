package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.JavaFxUtils;
import de.hhu.propra.team61.io.CustomizeManager;
import de.hhu.propra.team61.io.VorbisPlayer;
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

import static de.hhu.propra.team61.JavaFxUtils.extractPart;
import static de.hhu.propra.team61.JavaFxUtils.removeExtension;

/**
 * The lobby that is shown when joining or hosting a network game.
 *
 * This class contains all GUI-elements for the lobby and the methods for exchanging information between host and clients
 * before the game starts.
 * In the lobby players can choose their team, the host can choose the game style (containing level etc.). The game can
 * be started as soon as all clients are ready.
 *
 * Created by Jessypet on 27.05.14.
 */
public class NetLobby extends Application implements Networkable {

    /** used to switch back to menu  or the game */
    private SceneController sceneController = new SceneController();

    /** contains all GUI-elements */
    private CustomGrid overviewGrid = new CustomGrid();
    /** every hbox contains information for one team */
    private ArrayList<HBox> hboxes = new ArrayList<>();
    /** to choose a custom team */
    private ArrayList<ChoiceBox<String>> teamChoosers = new ArrayList<>();
    /** indicators next to each team, shows if the player is ready */
    private ArrayList<Text> readys = new ArrayList<>();
    /** contains list of players/spectators */
    private ArrayList<String> spectators = new ArrayList<>();
    /** button next to each team, makes host able to remove player */
    private ArrayList<Button> removeButtons = new ArrayList<>();

    /** for choosing game style */
    private ChoiceBox<String> style = new ChoiceBox<>();
    /** for choosing the level */
    private ChoiceBox<String> levelChooser = new ChoiceBox<>();
    /** number of figures per team */
    private TextField sizeField = new TextField("4");
    /** number of allowed teams */
    private TextField numberOfTeams = new TextField("2");
    /** number of existing teams = players */
    private int teamsCreated = 1;
    /** contains spectatorBox and chatBox */
    private CustomGrid listGrid;
    /** contains chat */
    Chat chatBox;
    /** contains list of players and spectators */
    private VBox spectatorBox;
    /** clients can decide whether they want to play or watch the game */
    private CheckBox spectator = new CheckBox("Spectator");
    /** makes showing different buttons for host and clients possible */
    private boolean isHost;
    /** click starts game, only shown to host */
    private Button start;
    /** click marks player as ready, only shown to clients */
    private Button ready;
    /** shows error-message in case of same team/color twice */
    private Text sameColor = new Text("You should not choose the same color or team!");
    /** -1 = spectator, 0 = host, 1+ = clients */
    private int associatedTeam;

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

    /**
     * builds basic GUI, adds first team and elements for choosing game style.
     */
    private void buildGUI() {
        sceneController.getStage().setOnCloseRequest(event -> {
            shutdown();
        });

        BorderPane root = new BorderPane();
        HBox topBox = addTopHBox();
        root.setTop(topBox);
        overviewGrid = new CustomGrid();
        overviewGrid.setPrefSize(672, 550);
        overviewGrid.setMaxSize(672, 550);
        root.setLeft(overviewGrid);

        Text teamsText = new Text("Teams:");
        teamsText.setFont(Font.font(16));
        overviewGrid.add(teamsText, 0, 7);

        Text team1 = new Text("Team 1");
        hboxes.add(new HBox(20));
        hboxes.get(0).getChildren().addAll(team1, teamChoosers.get(0));
        overviewGrid.add(hboxes.get(0), 0, 9, 3, 1);

        sameColor.setVisible(false);
        overviewGrid.add(sameColor, 0, 14, 6, 1);
        Text generalSettings = new Text("General settings:");
        generalSettings.setFont(Font.font(16));
        overviewGrid.add(generalSettings, 0, 0, 2, 1);
        ArrayList<String> availableGameStyles = CustomizeManager.getAvailableGameStyles();
        for (int i=0; i<availableGameStyles.size(); i++) {
            style.getItems().add(JavaFxUtils.removeExtension(availableGameStyles.get(i), 5));
        }
        style.getSelectionModel().selectFirst();
        style.valueProperty().addListener((ov, value, new_value) -> {
            if(new_value != null) {
                JSONObject styleObject = CustomizeManager.getSavedSettings("gamestyles/" + new_value + ".json");
                levelChooser.setValue(removeExtension(styleObject.getString("level"), 4));
                sizeField.setText(styleObject.getInt("teamSize")+"");
                server.send(getStateForNewClient());
            }
        });
        overviewGrid.add(style, 0, 1, 2, 1);
        Text chooseLevelText = new Text("Level:");
        overviewGrid.add(chooseLevelText, 2, 1);
        ArrayList<String> availableLevels = getLevels();
        int numberOfLevels = TerrainManager.getNumberOfAvailableTerrains();
        for (int i=0; i<numberOfLevels; i++) {
            levelChooser.getItems().add(JavaFxUtils.removeExtension(availableLevels.get(i), 4));
        }
        levelChooser.getSelectionModel().selectFirst();
        overviewGrid.add(levelChooser, 3, 1, 2, 1);
        levelChooser.valueProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue ov, String value, String new_value) {
                server.send(getStateForNewClient());
            }
        });
        Text teamSize = new Text("Size of teams: ");
        overviewGrid.add(teamSize, 0, 2);
        sizeField.setPrefWidth(40);
        overviewGrid.add(sizeField, 1, 2);
        sizeField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (Integer.parseInt(newValue) > 6) {
                    sizeField.setText("6");
                } else if (Integer.parseInt(newValue) < 1) {
                    sizeField.setText("1");
                }
                server.send(getStateForNewClient());
            }
        });
        Text teamNumber = new Text("Max. number of teams: ");
        overviewGrid.add(teamNumber, 2, 2, 2, 1);
        numberOfTeams.setPrefWidth(40);
        overviewGrid.add(numberOfTeams, 4, 2);
        numberOfTeams.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                server.send(getStateForNewClient());
            }
        });
        Text infoGameStyle = new Text("You can choose a game style and change level, team size, and number \n" +
                "of teams, but not items.");
        overviewGrid.add(infoGameStyle, 0, 5, 6, 1);

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
        back.getStyleClass().add("mainButton");
        bottomBox.getChildren().addAll(back);
        back.setOnAction(e -> {
            shutdown();
            sceneController.switchToMenu();
        });
        bottomBox.setId("bottomBox");
        root.setBottom(bottomBox);

        Scene lobbyScene = new Scene(root);
        lobbyScene.getStylesheets().add("file:resources/layout/css/lobby.css");
        overviewGrid.getStyleClass().add("overviewGrid");
        listGrid.getStyleClass().add("listGrid");
        sceneController.switchScene(lobbyScene, "Network Lobby");

        VorbisPlayer.readVolumeSetting();
        VorbisPlayer.play("resources/audio/BGM/Storm Seeker - Elevator.ogg", true);
    }

    /**
     * Shuts down server/client threads, stops BGM
     */
    private void shutdown() {
        if(serverThread != null) serverThread.interrupt();
        clientThread.interrupt();
        System.out.println("NetLobby client/server threads interrupted");
        if(server != null) server.stop();
        client.stop();
        System.out.println("NetLobby client/server (if any) stopped");
        VorbisPlayer.stop();
    }

    /**
     * Creates the top menu containing a label and a button. Dependant on whether you're host or client you are shown
     * the 'Start' or the 'Ready' button. When clicking 'Start' it is checked if there are two teams with the same color.
     * Clicking 'Ready' will change the text next to the client's team.
     * @return hbox that is set in top of the window in {@link #buildGUI()}.
     */
    private HBox addTopHBox() {
        HBox topBox = new HBox(850);
        HBox startBox = new HBox();
        Text lobbyText = new Text("Lobby");
        lobbyText.setFont(Font.font("Sans", 20));
        start = new Button("Start");
        start.setDisable(true); // enabled when clients are ready
        start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                boolean differentColors = true;
                for (int i=0; i<teamsCreated-1; i++) {
                    JSONObject objecti = CustomizeManager.getSavedSettings("teams/"+teamChoosers.get(i).getValue()+".json");
                    for(int h=i+1; h<teamsCreated; h++) {
                        JSONObject objecth = CustomizeManager.getSavedSettings("teams/"+teamChoosers.get(h).getValue()+".json");
                        if (objecti.getString("color").equals(objecth.getString("color"))) {
                            differentColors = false;
                        }
                    }
                }
                if (differentColors) {
                    VorbisPlayer.stop();
                    MapWindow mapwindow = new MapWindow(levelChooser.getValue()+".lvl", toJson(), client, clientThread, server, serverThread, sceneController);
                } else {
                    sameColor.setVisible(true);
                }
            }
        });
        start.getStyleClass().add("mainButton");
        ready = new Button("Ready");
        ready.setDisable(true); //enabled when disabling spectator mode
        ready.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                ready.setText("Waiting …");
                ready.setDisable(true);
                disableForbiddenSettings(-1);
                client.send("LOBBY_CHANGE " + toJson());
                client.send("CLIENT_READY");
            }
        });
        ready.getStyleClass().add("mainButton");
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

    /**
     * Builds the spectators-box.
     */
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

    /**
     * The list of spectators is updated in this method.
     * @param spectators contains current spectators
     */
    private void updateSpectators(JSONObject spectators) {
        this.spectators.clear();
        JSONArray spectatorList = spectators.getJSONArray("spectators");
        for(int i=0; i<spectatorList.length(); i++) {
            this.spectators.add(spectatorList.getJSONObject(i).getString("name") +
                    " (" + spectatorList.getJSONObject(i).getString("team") + ")");
        }
        generateSpectatorsBox();
    }

    /**
     * Calls method {@link de.hhu.propra.team61.io.TerrainManager#getAvailableTerrains()} to search for existing levels that
     * can be chosen by the host.
     * @return ArrayList of available levels
     */
    private ArrayList<String> getLevels() {
        ArrayList<String> levels = TerrainManager.getAvailableTerrains();
        return levels;
    }

    /**
     * Saves all settings (teams and game style) in a JSON-Object when the game is started and also when a new client connects
     * so that he can see the current settings.
     * @return
     */
    private JSONObject toJson() {
        if(style.getValue() == null) return new JSONObject();

        JSONObject output = new JSONObject();
        output.put("numberOfTeams", Integer.parseInt(numberOfTeams.getText()));   //save max. number of teams
        output.put("teamsCreated", teamsCreated);       //save current number of players
        output.put("teamSize", Integer.parseInt(sizeField.getText())); //save size of teams
        output.put("level", levelChooser.getValue());
        output.put("gameStyle", style.getValue());

        //Prepare inventory
        JSONObject gameStyleSettings = CustomizeManager.getSavedSettings("gamestyles/"+style.getValue()+".json");
        JSONArray weaponsSettings = gameStyleSettings.getJSONArray("inventory");
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

    /**
     * Saves all settings for one team in a JSONObject.
     * @param team name of the team
     * @param ready indicates if team is ready or not
     * @return JSONObject containing all settings for one team
     */
    private JSONObject getJsonForTeam(String team, Text ready) {
        JSONObject teamObject = CustomizeManager.getSavedSettings("teams/"+team+".json");
        JSONObject teamSettings = new JSONObject();
        teamSettings.put("chosenTeam", team);
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

    /**
     * Loads settings from a JSONObject. When a new client connects all current settings are loaded so that he can see them.
     * @param json the JSONObject containing settings
     */
    private void fromJson(JSONObject json) {
        for (int i=0; i<=3 ;i++) {          //remove all teams first when sending state to new client
            removeTeam(i, false);
        }
        teamsCreated = 0;
        if(json.has("numberOfTeams")) {
            numberOfTeams.setText(json.getInt("numberOfTeams")+"");
        }
        if(json.has("teamSize")) {
            sizeField.setText(json.getInt("teamSize")+"");
        }
        if(json.has("level")) {
            levelChooser.setValue(json.getString("level"));
        }
        if(json.has("gameStyle")) {
            style.setValue(json.getString("gameStyle"));
        }
        if(json.has("teams")) {
            JSONArray teamsArray = json.getJSONArray("teams");
            for(int i=0; i<teamsArray.length(); i++) {
                addTeam(i);
                teamChoosers.get(i).setValue(teamsArray.getJSONObject(i).getString("chosenTeam"));
                readys.get(i).setText(teamsArray.getJSONObject(i).getString("ready"));
            }
        }
    }

    /**
     * adds the team with the given number
     * @param number team number, counting starts from 0 = host
     */
    private void addTeam(int number) {                   //add a new team
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
        hboxes.get(number).getChildren().addAll(team, teamChoosers.get(number), readys.get(number));
        if (number!=0) {
            hboxes.get(number).getChildren().add(removeButtons.get(number));
        }
        overviewGrid.add(hboxes.get(number), 0, number + 9, 5, 1);
        start.setDisable(!Server.teamsAreReady());
    }

    /**
     * Opens a pop-up asking the host if he really wants to remove the player.
     * @param i index of the team that should be removed
     */
    private void removePlayer(int i) {
        Stage popUp = new Stage();
        Text wantToRemove = new Text("Do you really want to remove this player?");
        Button yes = new Button("Yes");
        yes.setOnAction(e -> {
            popUp.close();
            if(server != null) {
                removeTeam(i, true);
                server.changeTeamByNumber(i, -1); // change team to spectator
                ready.setText("Ready");
                disableForbiddenSettings(-1);
                server.send(getStateForNewClient()); // send new lobby state to clients
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
        if(teamsCreated < team || teamsCreated == 0) {
            System.err.println("WARNING " + teamsCreated + " teams exist, hence cannot remove team #" + team);
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

    /**
     * Creates GUI-elements for 4 possible teams, including the ChoiceBox to choose the team, the ready-indicator and the
     * button to remove.
     */
    private void initializeArrayLists() {
        ArrayList<String> availableTeams = getTeams();
        for (int i=0; i<=3; i++) {
            teamChoosers.add(new ChoiceBox<>());
            for (int j=0; j<availableTeams.size(); j++) {
                teamChoosers.get(i).getItems().add(JavaFxUtils.removeExtension(availableTeams.get(j), 5));
            }
            teamChoosers.get(i).getSelectionModel().selectFirst();
            final int thisTeam = i;
            teamChoosers.get(i).valueProperty().addListener((ov, value, new_value) -> {
//                if(!value.equals(new_value) && thisTeam == associatedTeam) { // TODO leads to recursive call
//                    System.err.println(thisTeam);
//                    client.send("LOBBY_CHANGE " + toJson());
//                }
            });
            readys.add(new Text("ready"));
            removeButtons.add(new Button("X"));
            removeButtons.get(i).getStyleClass().add("removeButton");
            final int finalI = i;
            removeButtons.get(i).setOnAction(e -> {
                removePlayer(finalI);
            });
        }
    }

    /**
     * Calls method {@link de.hhu.propra.team61.io.CustomizeManager#getAvailableTeams()} to search for existing teams.
     * @return ArrayList of available teams
     */
    private ArrayList<String> getTeams() {
        ArrayList<String> availableTeams = CustomizeManager.getAvailableTeams();
        return availableTeams;
    }

    @Override
    public void start(Stage filler) {}

    @Override
    public void handleOnClient(String command) {
        if(command.startsWith("STATUS MAPWINDOW")) {
            VorbisPlayer.stop();
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

    /**
     * Gives a new team a number.
     * @param newTeam number of the new team
     */
    private void setAssociatedTeam(int newTeam) {
        associatedTeam = newTeam;
        disableForbiddenSettings(associatedTeam);
        spectator.setSelected(associatedTeam == -1);
        System.out.println("I belong to team " + associatedTeam);
        client.setAssociatedTeam(newTeam);
    }

    /**
     * Is called when the spectator-box is checked or unchecked. Depending on the state of the box some GUI-elements are
     * enabled or disabled calling method {@link #disableForbiddenSettings(int)}.
     * @param isChecked boolean that indicates whether spectator-box is checked or not
     */
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

    /**
     * Disables GUI-elements depending on what state of player you are:
     * <ul>
     *     <li>Host: team = 0, all elements are enabled.</li>
     *     <li>Spectator: team = -1, all elements are disabled (except for spectator-box)</li>
     *     <li>Team: team = 1 to 4, all elements except for the own settings and the 'Ready'-button are disabled.</li>
     * </ul>
     * @param team number of the team belonging to the client
     */
    private void disableForbiddenSettings(int team) {
        if (!isHost) {
            style.setDisable(true);
            sizeField.setDisable(true);
            numberOfTeams.setDisable(true);
            levelChooser.setDisable(true);
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
    public void handleOnServer(String command) {
        System.out.println("server handling command in lobby: " + command);
        if (command.contains("SPECTATOR ")) {
            boolean checked = !(command.contains("UNCHECKED"));
            int currentTeam = Integer.parseInt(extractPart(command, "CHECKED "));
            String clientId = command.split(" ", 2)[0];
            handleSpectatorBoxChanged(checked, currentTeam, clientId);
        } else if (command.startsWith("READY")) {
            int team = Integer.parseInt(command.split(" ", 3)[1]);
            setTeamReady(team);
        } else if (command.startsWith("LOBBY_CHANGE")) {
            int team = Integer.parseInt(command.split(" ", 3)[1]);
            JSONObject clientSettings = new JSONObject(command.split(" ", 3)[2]);
            applySettingsFromClient(clientSettings, team);
        } else {
            System.out.println("Lobby handleOnServer: unknown command " + command);
        }
    }

    /**
     * applies a given settings object, but only the part which the given team is allowed to change, and sends new state to clients
     * @param clientSettings a JSONObject containing settings
     * @param team the team wanting to change settings (counting starts from 0=host)
     */
    private void applySettingsFromClient(JSONObject clientSettings, int team) {
        JSONObject currentSettings = toJson();
        // clients may only change the team settings, so replace the currently set team settings with the settings sent from the team
        JSONArray teamsArray = currentSettings.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            if(i == team) {
                JSONObject teamObj = teamsArray.getJSONObject(i);
                teamObj.put("chosenTeam", clientSettings.getJSONArray("teams").getJSONObject(i).getString("chosenTeam"));
                teamsArray.set(i, teamObj);
            }
        }
        currentSettings.put("teams", teamsArray);
        fromJson(currentSettings);
        server.send(getStateForNewClient());
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
                server.changeTeamById(clientId, teamsCreated); // associate client with last team; the team number is the number of teams created-1 (the new team has not been created here yet, so no -1)
                addTeam(teamsCreated);
            } else {
                System.out.println("Max. number of teams reached.");
                server.changeTeamById(clientId, -1); // will reset the client's spectator checkbox
            }
        }

        server.send(getStateForNewClient());
    }

    /**
     * marks the given team as ready, informs all clients about the change, and enables the Start button if everyone is ready
     * @param team the team which is ready
     */
    private void setTeamReady(int team) {
        if(team < 1) throw new IllegalArgumentException("Team " + team + " cannot change to ready state.");

        System.out.println("Team #" + team + " is ready");
        readys.get(team).setText("ready");
        server.send(getStateForNewClient());

        if(server.teamsAreReady()) start.setDisable(false);
    }

    @Override
    public String getStateForNewClient() {
        return "STATUS LOBBY " + this.toJson().toString();
    }
}
