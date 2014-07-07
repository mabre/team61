package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.CustomizeWindow;
import de.hhu.propra.team61.JavaFxUtils;
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

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Controls GUI-elements for GameSettings.
 *
 * Created by Jessypet on 29.06.14.
 */
public class SettingsController {

    /** used to switch back to menu or to game */
    private SceneController sceneController;
    /** contains all available game styles */
    @FXML private ChoiceBox<String> gameStyle;
    /** shows the level of the currently chosen game style */
    @FXML private Text level = new Text();
    /** shows the teamSize of the currently chosen game style */
    @FXML private Text teamSize = new Text();
    /** shows message e.g, in case of more than one player with the same team */
    @FXML private Text error = new Text();
    /** contains all GUI-elements */
    @FXML private GridPane settingGrid = new GridPane();
    /** click adds one more team */
    @FXML private Button plus = new Button();
    /** click starts game */
    @FXML private Button cont = new Button();

    /** increased for every team that is added, used for color comparison */
    private int numberOfTeams;
    /** contains ChoiceBoxes to choose custom team from, one for each team */
    private ArrayList<ChoiceBox<String>> teams = new ArrayList<>();

    Server server;
    Thread serverThread;
    Client client;
    Thread clientThread;

    /**
     * Initializes elements of ChoiceBoxes, the number of teams and some styling.
     * @param sceneController used to switch back to menu or to game
     */
    public void initialize(SceneController sceneController) {
        this.sceneController = sceneController;
        ArrayList<String> availableStyles = getStyles();
        for (int h=0; h<availableStyles.size(); h++) {
            gameStyle.getItems().add(JavaFxUtils.removeExtension(availableStyles.get(h), 5));
        }
        gameStyle.getSelectionModel().selectFirst();
        showStyleInformation(gameStyle.getValue()+".json");
        gameStyle.valueProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue ov, String value, String new_value) {
                showStyleInformation(new_value+".json");
            }
        });
        ArrayList<String> availableTeams = getTeams();
        for (int i=0; i<4; i++) {
            teams.add(new ChoiceBox<>());
            for (int j=0; j<availableTeams.size(); j++) {
                teams.get(i).getItems().add(JavaFxUtils.removeExtension(availableTeams.get(j), 5));
            }
            teams.get(i).getSelectionModel().select(i);
        }
        numberOfTeams = 2;
        settingGrid.add(teams.get(0), 0, 2, 3, 1);
        settingGrid.add(teams.get(1), 0, 3, 3, 1);
        cont.getStyleClass().addAll("mainButton", "startButton");
        plus.setPrefWidth(100);
    }

    /**
     * Handles click on '+'-Button. A new team is added, as soon as the number of teams is 4 the button is set invisible.
     */
    @FXML
    public void handlePlus() {
        numberOfTeams++;
        addTeam(numberOfTeams);
        if (numberOfTeams==4) {
            plus.setVisible(false);
        }
    }

    /**
     * Handles click on 'Edit'. Switches to Customize.
     */
    @FXML
    public void handleEdit() {
        CustomizeWindow customizeWindow = new CustomizeWindow(sceneController);
    }

    /**
     * Handles click on 'Start!'. The different teams and their colors are compared, if all teams and colors are different
     * the game starts, otherwise an error message is shown.
     */
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
            JSONObject objecti = CustomizeManager.getSavedSettings("teams/" + teams.get(i).getValue()+".json");
            for(int h=i+1; h<numberOfTeams; h++) {
                JSONObject objecth = CustomizeManager.getSavedSettings("teams/"+teams.get(h).getValue()+".json");
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
                        JSONObject styleObject = CustomizeManager.getSavedSettings("gamestyles/" + gameStyle.getValue()+".json");
                        String map = styleObject.getString("level");
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

    /**
     * Handles click on 'Back'. Switches backto menu.
     */
    @FXML
    public void handleBack() {
        sceneController.switchToMenu();
    }

    /**
     * Shows another ChoiceBox for new team
     * @param number number of teams already existing, index for ArrayList of ChoiceBoxes
     */
    private void addTeam(int number) {
        settingGrid.add(teams.get(number-1), 0, number+1, 3, 1);
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
        level.setText("Level: " + JavaFxUtils.removeExtension(styleObject.getString("level"), 4));
        teamSize.setText("Team size: "+styleObject.getInt("teamSize"));
    }

    /**
     * Builds the JSON-Object for chosen teams and game gameStyle
     * @return a JSON-Object containing all game settings
     */
    private JSONObject toJson() {
        JSONObject styleObject = CustomizeManager.getSavedSettings("gamestyles/" + gameStyle.getValue()+".json");
        JSONObject output = new JSONObject();
        output.put("numberOfTeams", numberOfTeams);   //save number of teams
        output.put("teamSize", styleObject.getInt("teamSize")); //save size of teams
        output.put("level", styleObject.getString("level"));
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
        JSONObject teamObject = CustomizeManager.getSavedSettings("teams/"+fileName+".json");
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
