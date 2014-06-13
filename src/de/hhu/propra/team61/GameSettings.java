package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.CustomGrid;
import de.hhu.propra.team61.gui.SceneController;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.io.Settings;
import de.hhu.propra.team61.io.TerrainManager;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;

import static de.hhu.propra.team61.JavaFxUtils.toHex;

/**
 * Created by Jessypet on 21.05.14.
 */

public class GameSettings extends Application {

    int numberOfTeams = 2;
    CustomGrid settingGrid = new CustomGrid();
    ChoiceBox<String> team1 = new ChoiceBox<>();
    ChoiceBox<String> team2 = new ChoiceBox<>();
    ChoiceBox<String> team3 = new ChoiceBox<>();
    ChoiceBox<String> team4 = new ChoiceBox<>();
    ChoiceBox<String> style = new ChoiceBox<>();
    HBox hboxplus = new HBox(5);
    Button addTeamButton = new Button("+");

    SceneController sceneController = new SceneController();

    Server server;
    Thread serverThread;
    Client client;
    Thread clientThread;

    public GameSettings(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    public void doSettings() {
        settingGrid.setAlignment(Pos.TOP_LEFT);
        Text teamsText = new Text("Choose Teams:");
        teamsText.setFont(Font.font("Verdana", 18));
        settingGrid.add(teamsText, 0, 1);
        settingGrid.add(team1, 0, 2);
        settingGrid.add(team2, 0, 3);
        hboxplus.getChildren().add(addTeamButton);
        settingGrid.add(hboxplus, 0, 6);
        addTeamButton.setOnAction(e -> {
            numberOfTeams++;
            addTeam(numberOfTeams);
        });
        Text stylesText = new Text("Choose Game Style:");
        stylesText.setFont(Font.font("Verdana", 18));
        settingGrid.add(stylesText, 3, 1);
        settingGrid.add(style, 3, 2);
        getTeams();
        getStyles();
        Text sameColor = new Text();
        settingGrid.add(sameColor, 0, 17, 3, 1);
        Button cont = new Button("Continue");
        settingGrid.add(cont, 0, 16);
        cont.setOnAction(e -> {
                boolean differentColors = true;
                /*for (int i=0; i<numberOfTeams-1; i++) {
                    for(int h=i+1; h<numberOfTeams; h++) {
                        if (colorPickers.get(i).getValue().equals(colorPickers.get(h).getValue())) {
                            differentColors = false;
                        }
                    }
                }*/
                if (differentColors) {
                    // our local game is also client/server based, with server running on localhost
                    serverThread = new Thread(server = new Server(() -> {
                        clientThread = new Thread(client = new Client(() -> {
                            //Settings.save(toJson(), "SETTINGS_FILE"); // create JSON object and save it in SETTINGS_FILE.conf
                            System.out.println("GameSettings: saved settings");
                            //Platform.runLater(() -> new MapWindow(mapChooser.getValue(), "SETTINGS_FILE.conf", client, clientThread, server, serverThread, sceneController));
                        }));
                        clientThread.start();
                    }));
                    serverThread.start();
                } else {
                    sameColor.setText("You should not choose the same color!");
                }
        });

        Button back = new Button("Back");
        settingGrid.add(back, 1, 16);
        back.setOnAction(e -> {
            sceneController.switchToMenue();
        });

        Scene settingScene = new Scene(settingGrid, 1000, 600);
        settingScene.getStylesheets().add("file:resources/layout/css/settings.css");
        settingGrid.getStyleClass().add("settingpane");
        sceneController.setSettingsScene(settingScene);
        sceneController.switchToGameSettings();
    }

    public void getTeams() {
        //TODO
    }

    public void getStyles() {
        //TODO
    }

    /*public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("numberOfTeams", numberOfTeams);   //save number of teams
        output.put("team-size", sizefield.getText()); //save size of teams
        output.put("map", mapChooser.getValue());
        output.put("weapon1", weapon1.getText()); // TODO make array instead of using suffix
        output.put("weapon2", weapon2.getText());
        output.put("weapon3", weapon3.getText());
        JSONArray teams = new JSONArray();
        JSONObject team1 = getJsonForTeam(name1.getText(), colorPicker1, figure1);
        teams.put(team1);
        JSONObject team2 = getJsonForTeam(name2.getText(), colorPicker2, figure2);
        teams.put(team2);
        if (numberOfTeams > 2) {
            JSONObject team3 = getJsonForTeam(name3.getText(), colorPicker3, figure3);
            teams.put(team3);
        }
        if (numberOfTeams > 3) {
            JSONObject team4 = getJsonForTeam(name4.getText(), colorPicker4, figure4);
            teams.put(team4);
        }
        output.put("teams", teams);
        return output;
    }

    public void fromJson(String file) {
        JSONObject savedSettings = Settings.getSavedSettings(file);
        if(savedSettings.has("numberOfTeams")) {
            numberOfTeams = savedSettings.getInt("numberOfTeams");
            if (numberOfTeams > 2) {  addTeams(3); }
            if (numberOfTeams > 3) {  addTeams(4); }
        }
        if(savedSettings.has("team-size")) {
            sizefield.setText(savedSettings.getString("team-size"));
        }
        if(savedSettings.has("map")) {
            mapChooser.setValue(savedSettings.getString("map"));
        }
        if(savedSettings.has("weapon1")) {
            weapon1.setText(savedSettings.getString("weapon1"));
        }
        if(savedSettings.has("weapon2")) {
            weapon2.setText(savedSettings.getString("weapon2"));
        }
        if(savedSettings.has("weapon3")) {
            weapon3.setText(savedSettings.getString("weapon3"));
        }
        if(savedSettings.has("teams")) {
            JSONArray teamsArray = savedSettings.getJSONArray("teams");
            for(int i=0; i<teamsArray.length(); i++) {
                names.get(i).setText(teamsArray.getJSONObject(i).getString("name"));
                colorPickers.get(i).setValue(Color.web(teamsArray.getJSONObject(i).getString("color")));
            }
        }
    }*/

    /**
     * @param name of the team
     * @param color of the team
     * @return a JSONObject representing basic settings for a team
     */
    /*public JSONObject getJsonForTeam(String name, ColorPicker color, ToggleGroup figure) {
        JSONObject team = new JSONObject();
        team.put("name", name);
        team.put("color", toHex(color.getValue()));
        team.put("figure", figure.getSelectedToggle());
        return team;
    }*/

    public void addTeam(int number) {
        if (number == 3) {
            settingGrid.add(team3, 0, 4);
        }
        if (number == 4) {
            settingGrid.add(team4, 0, 5);
        }
    }

    @Override
    public void start(Stage filler) {}
}