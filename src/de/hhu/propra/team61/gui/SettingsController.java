package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.CustomizeWindow;
import de.hhu.propra.team61.MapWindow;
import de.hhu.propra.team61.io.CustomizeManager;
import de.hhu.propra.team61.io.Settings;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Server;
import javafx.application.Platform;
import javafx.beans.value.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.ArrayList;

/**
 * Created by Jessypet on 29.06.14.
 */
public class SettingsController {

    private SceneController sceneController;
    @FXML private ChoiceBox<String> gameStyle;
    @FXML private Text level = new Text();
    @FXML private Text teamSize = new Text();
    @FXML private Text error = new Text();
    @FXML private GridPane settingGrid = new GridPane();
    @FXML private Button plus = new Button();

    /** increased for every team that is added, used for color comparison */
    private int numberOfTeams;
    /** contains ChoiceBoxes to choose custom team from, one for each team */
    private ArrayList<ChoiceBox<String>> teams = new ArrayList<>();

    Server server;
    Thread serverThread;
    Client client;
    Thread clientThread;

    public void initialize(SceneController sceneController) {
        this.sceneController = sceneController;
        gameStyle.setItems(FXCollections.observableArrayList(getStyles()));
        gameStyle.getSelectionModel().selectFirst();
        showStyleInformation(gameStyle.getValue());
        gameStyle.valueProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue ov, String value, String new_value) {
                showStyleInformation(new_value);
            }
        });
        for (int i=0; i<4; i++) {
            teams.add(new ChoiceBox<>(FXCollections.observableArrayList(getTeams())));
            teams.get(i).getSelectionModel().select(i);
        }
        numberOfTeams = 2;
        settingGrid.add(teams.get(0), 0, 2);
        settingGrid.add(teams.get(1), 0, 3);
    }

    @FXML
    public void handlePlus() {
        numberOfTeams++;
        addTeam(numberOfTeams);
        if (numberOfTeams==4) {
            plus.setVisible(false);
        }
    }

    @FXML
    public void handleEdit() {
        CustomizeWindow customizeWindow = new CustomizeWindow(sceneController);
    }

    @FXML
    public void handleContinue() {
        //Check if there are two players with same team or with same color
        boolean differentColors = true;
        boolean differentTeams = true;
        for (int i=0; i<numberOfTeams-1; i++) {
            for (int h=i+1; h<numberOfTeams; h++) {
                if (teams.get(i).getValue().equals(teams.get(h).getValue())) {
                    differentTeams = false;
                }
            }
        }
        for (int i=0; i<numberOfTeams-1; i++) {
            JSONObject objecti = CustomizeManager.getSavedSettings("teams/" + teams.get(i).getValue());
            for(int h=i+1; h<numberOfTeams; h++) {
                JSONObject objecth = CustomizeManager.getSavedSettings("teams/"+teams.get(h).getValue());
                if (objecti.getString("color").equals(objecth.getString("color"))) {
                    differentColors = false;
                }
            }
        }
        if (differentTeams) {
            if (differentColors) {
                // our local game is also client/server based, with server running on localhost
                serverThread = new Thread(server = new Server(() -> {
                    clientThread = new Thread(client = new Client(() -> {
                        Settings.saveJson(toJson(), "SETTINGS_FILE");
                        System.out.println("GameSettings: saved settings");
                        JSONObject styleObject = CustomizeManager.getSavedSettings("gamestyles/" + gameStyle.getValue());
                        String map = styleObject.getString("map");
                        Platform.runLater(() -> new MapWindow(map, "SETTINGS_FILE.conf", client, clientThread, server, serverThread, sceneController));
                    }));
                    clientThread.start();
                }));
                serverThread.start();
            } else {
                error.setText("You should not choose teams with the same color!");
            }
        } else {
            error.setText("You should not choose the same team more than once!");
        }
    }

    @FXML
    public void handleBack() {
        sceneController.switchToMenu();
    }

    /**
     * Shows another ChoiceBox for new team
     * @param number number of teams already existing, index for ArrayList of ChoiceBoxes
     */
    private void addTeam(int number) {
        settingGrid.add(teams.get(number-1), 0, number+1);
    }

    /**
     * Gets available custom teams.
     * @return ArrayList of custom teams to be shown in the ChoiceBoxes
     */
    private ArrayList<String> getTeams() {
        ArrayList<String> availableTeams = CustomizeManager.getAvailableTeams();
        return availableTeams;
    }

    /**
     * Gets available custom game styles.
     * @return ArrayList of custom game styles to be shown in the ChoiceBox
     */
    private ArrayList<String> getStyles() {
        ArrayList<String> availableGameStyles = CustomizeManager.getAvailableGameStyles();
        return availableGameStyles;
    }

    /**
     * Gets gameStyle-information to show it beneath ChoiceBox.
     * @param file name of the game gameStyle (= name of that gameStyle's name)
     */
    private void showStyleInformation(String file) {
        JSONObject styleObject = CustomizeManager.getSavedSettings("gamestyles/"+file);
        level.setText("Map: " + styleObject.getString("map"));
        teamSize.setText("Team-Size: "+styleObject.getString("team-size"));
    }

    /**
     * Builds the JSON-Object for chosen teams and game gameStyle
     * @return a JSON-Object containing all game settings
     */
    private JSONObject toJson() {
        JSONObject styleObject = CustomizeManager.getSavedSettings("gamestyles/" + gameStyle.getValue());
        JSONObject output = new JSONObject();
        output.put("numberOfTeams", numberOfTeams);   //save number of teams
        output.put("team-size", styleObject.getString("team-size")); //save size of teams
        output.put("map", styleObject.getString("map"));
        JSONArray weaponsStyle = styleObject.getJSONArray("inventory");
        JSONArray weaponsSettings = new JSONArray();
        for(int i=0; i<weaponsStyle.length(); i++) {
            weaponsSettings.put(weaponsStyle.getInt(i));
        }
        output.put("inventory", weaponsSettings);
        JSONArray teamsArray = new JSONArray();
        JSONObject team1 = getJsonForTeam(teams.get(0).getValue());
        teamsArray.put(team1);
        JSONObject team2 = getJsonForTeam(teams.get(1).getValue());
        teamsArray.put(team2);
        if (numberOfTeams > 2) {
            JSONObject team3 = getJsonForTeam(teams.get(2).getValue());
            teamsArray.put(team3);
        }
        if (numberOfTeams > 3) {
            JSONObject team4 = getJsonForTeam(teams.get(3).getValue());
            teamsArray.put(team4);
        }
        output.put("teams", teamsArray);
        return output;
    }

    /**
     * Gets and saves team-settings from JSON-Object
     * @param fileName the name of the chosen team (= name of that team's JSON-file)
     * @return JSON-Object that contains all information about the chosen team
     */
    private JSONObject getJsonForTeam(String fileName) {
        JSONObject teamObject = CustomizeManager.getSavedSettings("teams/"+fileName);
        JSONObject team = new JSONObject();
        team.put("name", teamObject.getString("name"));
        team.put("color", teamObject.getString("color"));
        team.put("figure", teamObject.getString("figure"));
        JSONArray figureNames = new JSONArray();
        JSONObject figureNamesObject = teamObject.getJSONObject("figure-names");
        for (int i=0; i<6; i++) {
            JSONObject figure = new JSONObject();
            figure.put("figure", figureNamesObject.getString("figure"+i));
            figureNames.put(figure);
        }
        team.put("figure-names", figureNames);
        return team;
    }
}
