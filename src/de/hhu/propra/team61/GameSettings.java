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
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import java.lang.reflect.Array;
import java.util.ArrayList;

import static de.hhu.propra.team61.JavaFxUtils.toHex;

/**
 * Created by Jessypet on 21.05.14.
 */

public class GameSettings extends Application {

    int numberOfTeams = 2;
    CustomGrid settingGrid = new CustomGrid();
    TextField name1 = new TextField("player1");
    TextField name2 = new TextField("player2");
    TextField name3 = new TextField("player3");
    TextField name4 = new TextField("player4");
    ArrayList<TextField> names = new ArrayList<TextField>();
    ArrayList<ColorPicker> colorPickers = new ArrayList<ColorPicker>();
    ColorPicker colorPicker1 = new ColorPicker();
    ColorPicker colorPicker2 = new ColorPicker();
    ColorPicker colorPicker3 = new ColorPicker();
    ColorPicker colorPicker4 = new ColorPicker();
    ToggleGroup figure1 = new ToggleGroup();
    ToggleGroup figure2 = new ToggleGroup();
    ToggleGroup figure3 = new ToggleGroup();
    ToggleGroup figure4 = new ToggleGroup();
    RadioButton penguin1 = new RadioButton("Penguin");
    RadioButton unicorn1 = new RadioButton("Unicorn");
    RadioButton penguin2 = new RadioButton("Penguin");
    RadioButton unicorn2 = new RadioButton("Unicorn");
    RadioButton penguin3 = new RadioButton("Penguin");
    RadioButton unicorn3 = new RadioButton("Unicorn");
    RadioButton penguin4 = new RadioButton("Penguin");
    RadioButton unicorn4 = new RadioButton("Unicorn");
    TextField savefield = new TextField();
    TextField weapon1 = new TextField("50");
    TextField weapon2 = new TextField("50");
    TextField weapon3 = new TextField("5");
    TextField sizefield = new TextField();
    HBox hboxplus = new HBox(5);
    Button addTeam = new Button("+");
    ChoiceBox<String> mapChooser = new ChoiceBox<>();
    Boolean differentColors = true;

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
        initializeArrayLists();

        //Save/load settings
        Text saveText = new Text("Save settings in ");
        settingGrid.add(saveText, 0, 0);
        settingGrid.add(savefield, 1, 0);
        Text extension = new Text(".conf");
        settingGrid.add(extension, 2, 0);
        Button saveButton = new Button("Save");
        settingGrid.add(saveButton, 3, 0);
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Settings.save(toJson(), savefield.getText());               //create Json-object and save it in the wanted file
                System.out.println("GameSettings: saved settings");
            }
        });
        Text load = new Text("Load settings: ");
        settingGrid.add(load, 0, 1);
        FileChooser loadChooser = new FileChooser();
        FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("CONF", "*.conf");
        loadChooser.getExtensionFilters().add(fileFilter);
        loadChooser.setTitle("Choose file to load");
        Button loadButton = new Button("Choose file..");
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                File loadFile = loadChooser.showOpenDialog(sceneController.getStage());
                if (loadFile != null) {
                    String loadFileString = loadFile.getAbsolutePath();
                    fromJson(loadFileString);
                }
            }
        });
        settingGrid.add(loadButton, 1, 1);

        //Weapons, TextFields for entering a quantity
        Label weapons = new Label("Weapons");
        weapons.setFont(Font.font("Verdana", 20));
        settingGrid.add(weapons, 0, 3);
        Text enter = new Text ("Enter the quantity of projectiles for each weapon.");
        settingGrid.add(enter, 1, 3, 3, 1);
        Text w1 = new Text("Weapon 1: ");
        settingGrid.add(w1, 0, 4);
        settingGrid.add(weapon1, 1, 4);
        Text w2 = new Text("Weapon 2: ");
        settingGrid.add(w2, 2, 4);
        settingGrid.add(weapon2, 3, 4);
        Text w3 = new Text("Weapon 3: ");
        settingGrid.add(w3, 4, 4);
        settingGrid.add(weapon3, 5, 4);

        Label indi = new Label("Individual Team Settings");
        indi.setFont(Font.font("Verdana", 20));
        settingGrid.add(indi, 0, 6, 3, 1);

        //Categories, type team-name and choose color
        Text name = new Text("Team-Name");
        settingGrid.add(name, 1, 7);
        Text color = new Text("Color");
        settingGrid.add(color, 2, 7);
        Text figure = new Text("Figure");
        settingGrid.add(figure, 3, 7);
        //Team 1
        Text team1 = new Text("Team 1");
        settingGrid.add(team1, 0, 8);
        settingGrid.add(name1, 1, 8);
        settingGrid.add(colorPicker1, 2, 8);
        colorPicker1.setValue(Color.web("#003300"));
        penguin1.setToggleGroup(figure1);
        penguin1.setSelected(true);
        unicorn1.setToggleGroup(figure1);
        settingGrid.add(penguin1, 3, 8);
        settingGrid.add(unicorn1, 4, 8);
        //Team 2
        Text team2 = new Text("Team 2");
        settingGrid.add(team2, 0, 9);
        settingGrid.add(name2, 1, 9);
        settingGrid.add(colorPicker2, 2, 9);
        colorPicker2.setValue(Color.web("#123456"));
        penguin2.setToggleGroup(figure2);
        unicorn2.setSelected(true);
        unicorn2.setToggleGroup(figure2);
        settingGrid.add(penguin2, 3, 9);
        settingGrid.add(unicorn2, 4, 9);

        hboxplus.getChildren().add(addTeam);          //Add plus button
        settingGrid.add(hboxplus, 0, 11);
        addTeam.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                numberOfTeams++;
                addTeams(numberOfTeams);             //add another team, dependent on how many already are there
            }
        });

        Text sizeText = new Text("Team-Size:");
        sizefield.setText("4");
        settingGrid.add(sizeText, 0, 14);
        settingGrid.add(sizefield, 1, 14);
        Text chooseMapText = new Text("Choose map:");
        settingGrid.add(chooseMapText, 2, 14);
        ArrayList<String> availableLevels = getLevels();
        int numberOfLevels = TerrainManager.getNumberOfAvailableTerrains();
        for (int i=0; i<numberOfLevels; i++) {
            mapChooser.getItems().add(availableLevels.get(i));
        }
        mapChooser.getSelectionModel().selectFirst();
        settingGrid.add(mapChooser, 3, 14);

        Text sameColor = new Text();
        settingGrid.add(sameColor, 0, 17, 3, 1);
        Button cont = new Button("Continue");
        settingGrid.add(cont, 0, 16);
        cont.setOnAction(e -> {
            int i = 0;
            while (differentColors && (i<numberOfTeams-1)) {
                int h=i+1;
                while (differentColors && (h<numberOfTeams)) {
                    if (colorPickers.get(i).getValue() == colorPickers.get(h).getValue()) {
                        differentColors = false;
                    }
                    h++;
                }
            }
            if (differentColors) {
                // our local game is also client/server based, with server running on localhost
                serverThread = new Thread(server = new Server(() -> {
                    clientThread = new Thread(client = new Client(() -> {
                        Settings.save(toJson(), "SETTINGS_FILE"); // create JSON object and save it in SETTINGS_FILE.conf
                        System.out.println("GameSettings: saved settings");
                        Platform.runLater(() -> new MapWindow(mapChooser.getValue(), "SETTINGS_FILE.conf", client, clientThread, server, serverThread, sceneController));
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

    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("numberOfTeams", numberOfTeams);   //save number of teams
        output.put("team-size", sizefield.getText()); //save size of teams
        output.put("map", mapChooser.getValue());
        output.put("weapon1", weapon1.getText()); // TODO make array instead of using suffix
        output.put("weapon2", weapon2.getText());
        output.put("weapon3", weapon3.getText());
        JSONArray teams = new JSONArray();
        JSONObject team1 = getJsonForTeam(name1.getText(), colorPicker1);
        teams.put(team1);
        JSONObject team2 = getJsonForTeam(name2.getText(), colorPicker2);
        teams.put(team2);
        if (numberOfTeams > 2) {
            JSONObject team3 = getJsonForTeam(name3.getText(), colorPicker3);
            teams.put(team3);
        }
        if (numberOfTeams > 3) {
            JSONObject team4 = getJsonForTeam(name4.getText(), colorPicker4);
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
    }

    /**
     * @param name of the team
     * @param color of the team
     * @return a JSONObject representing basic settings for a team
     */
    public JSONObject getJsonForTeam(String name, ColorPicker color) {
        JSONObject team = new JSONObject();
        team.put("name", name);
        team.put("color", toHex(color.getValue()));
        return team;
    }

    public void addTeams(int number) {
        if (number == 3) {
            Text team3 = new Text("Team 3");
            settingGrid.add(team3, 0, 10);
            settingGrid.add(name3, 1, 10);
            settingGrid.add(colorPicker3, 2, 10);
            colorPicker3.setValue(Color.web("#e6804d"));
            penguin3.setToggleGroup(figure3);
            penguin3.setSelected(true);
            unicorn3.setToggleGroup(figure3);
            settingGrid.add(penguin3, 3, 10);
            settingGrid.add(unicorn3, 4, 10);
        }
        if (number == 4) {
            Text team4 = new Text("Team 4");
            settingGrid.add(team4, 0, 11);
            settingGrid.add(name4, 1, 11);
            settingGrid.add(colorPicker4, 2, 11);
            colorPicker4.setValue(Color.web("#990000"));
            hboxplus.getChildren().remove(addTeam);              //Maximum is 4, button is hidden now
            Text enough = new Text("Max. 4 teams");
            settingGrid.add(enough, 1, 12);
            penguin4.setToggleGroup(figure4);
            unicorn4.setSelected(true);
            unicorn4.setToggleGroup(figure4);
            settingGrid.add(penguin4, 3, 11);
            settingGrid.add(unicorn4, 4, 11);
        }
    }

    public void initializeArrayLists() {
        names.add(name1);
        names.add(name2);
        names.add(name3);
        names.add(name4);
        colorPickers.add(colorPicker1);
        colorPickers.add(colorPicker2);
        colorPickers.add(colorPicker3);
        colorPickers.add(colorPicker4);
    }

    public ArrayList<String> getLevels() {
        ArrayList<String> levels = TerrainManager.getAvailableTerrains();
        return levels;
    }

    @Override
    public void start(Stage filler) {}
}