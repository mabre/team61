package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.CustomGrid;
import de.hhu.propra.team61.gui.SceneController;
import de.hhu.propra.team61.io.CustomizeManager;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.io.Settings;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * The window that is shown when starting a local game.
 *<p>
 * This class contains GUI for choosing teams and game style for the game.
 * Custom teams and game styles are loaded from different JSON-Files and, when starting the game, saved in SETTINGS_FILE.conf.
 * <p>
 *
 * Created by Jessypet on 21.05.14.
 */

public class GameSettings extends Application {
    /** increased for every team that is added, used for color comparison */
    private int numberOfTeams = 2;
    /** grid for GUI elements */
    private CustomGrid settingGrid = new CustomGrid();
    /** contains ChoiceBoxes to choose custom team from, one for each team */
    private ArrayList<ChoiceBox<String>> teams = new ArrayList<>();
    /** choose custom game style */
    private ChoiceBox<String> style;
    /** contains addTeamButton to be able to make button invisible */
    private HBox hboxPlus = new HBox(5);
    /** add one more team per click, removed when 4 teams exist */
    private Button addTeamButton = new Button("+");
    /** shows level of currently chosen game style */
    private Text level = new Text();
    /** shows team-size of currently chosen game style */
    private Text teamSize = new Text();

    Server server;
    Thread serverThread;
    Client client;
    Thread clientThread;

    /**
     * Builds the GUI, switches the scene and checks conditions to start the game by comparing the team's colors.
     * @param sceneController makes switching between scenes in one stage possible
     */
    public GameSettings(SceneController sceneController) {
        initializeArrayLists();
        settingGrid.setAlignment(Pos.TOP_LEFT);
        Text teamsText = new Text("Choose Teams:");
        teamsText.setFont(Font.font("Verdana", 18));
        settingGrid.add(teamsText, 0, 1);
        settingGrid.add(teams.get(0), 0, 2);
        settingGrid.add(teams.get(1), 0, 3);
        hboxPlus.getChildren().add(addTeamButton);
        settingGrid.add(hboxPlus, 0, 6);
        addTeamButton.setOnAction(e -> {
            numberOfTeams++;
            addTeam(numberOfTeams);
        });
        Text stylesText = new Text("Choose Game Style:");
        stylesText.setFont(Font.font("Verdana", 18));
        settingGrid.add(stylesText, 3, 1);
        style = new ChoiceBox<>(FXCollections.observableArrayList(getStyles()));
        style.getSelectionModel().selectFirst();
        settingGrid.add(style, 3, 2);
        settingGrid.add(level, 3, 3);
        settingGrid.add(teamSize, 3, 4, 2, 1);
        showStyleInformation(style.getValue());
        style.valueProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue ov, String value, String new_value) {
                showStyleInformation(new_value);
            }
        });
        Text error = new Text();
        settingGrid.add(error, 0, 17, 5, 1);
        Button edit = new Button("Edit settings");
        edit.setOnAction(e -> {
            CustomizeWindow customizeWindow = new CustomizeWindow(sceneController);
        });
        settingGrid.add(edit, 0, 16);
        Button cont = new Button("Continue");
        settingGrid.add(cont, 0, 17);
        cont.setOnAction(e -> {
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
                JSONObject objecti = CustomizeManager.getSavedSettings("teams/"+teams.get(i).getValue());
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
                            Settings.save(toJson(), "SETTINGS_FILE");
                            System.out.println("GameSettings: saved settings");
                            JSONObject styleObject = CustomizeManager.getSavedSettings("gamestyles/"+style.getValue());
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
        });
        Button back = new Button("Back");
        settingGrid.add(back, 1, 17);
        back.setOnAction(e -> {
            sceneController.switchToMenue();
        });
        Scene settingScene = new Scene(settingGrid, 1000, 600);
        settingScene.getStylesheets().add("file:resources/layout/css/settings.css");
        settingGrid.getStyleClass().add("settingpane");
        sceneController.switchScene(settingScene, "Game settings");
    }

    /**
     * Builds the JSON-Object for chosen teams and game style
     * @return a JSON-Object containing all game settings
     */
    private JSONObject toJson() {
        JSONObject styleObject = CustomizeManager.getSavedSettings("gamestyles/"+style.getValue());
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

    /**
     * Gets style-information to show it beneath ChoiceBox
     * @param file name of the game style (= name of that style's name)
     */
    private void showStyleInformation(String file) {
        JSONObject styleObject = CustomizeManager.getSavedSettings("gamestyles/"+file);
        level.setText("Map: " + styleObject.getString("map"));
        teamSize.setText("Team-Size: "+styleObject.getString("team-size"));
    }

    /**
     * Gets available custom teams
     * @return ArrayList of custom teams to be shown in the ChoiceBoxes
     */
    private ArrayList<String> getTeams() {
        ArrayList<String> availableTeams = CustomizeManager.getAvailableTeams();
        return availableTeams;
    }

    /**
     * Gets available custom game styles
     * @return ArrayList of custom game styles to be shown in the ChoiceBox
     */
    private ArrayList<String> getStyles() {
        ArrayList<String> availableGameStyles = CustomizeManager.getAvailableGameStyles();
        return availableGameStyles;
    }

    /**
     * Shows another ChoiceBox for new team
     * @param number number of teams already existing, index for ArrayList of ChoiceBoxes
     */
    private void addTeam(int number) {
        settingGrid.add(teams.get(number-1), 0, number+1);
    }

    /**
     * Creates ArrayList team containing ChoiceBoxes
     */
    private void initializeArrayLists() {
        for (int i=0; i<4; i++) {
            teams.add(new ChoiceBox<>(FXCollections.observableArrayList(getTeams())));
            teams.get(i).getSelectionModel().select(i);
        }
    }

    @Override
    public void start(Stage filler) {}
}