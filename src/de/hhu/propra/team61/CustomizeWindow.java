package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.CustomGrid;
import de.hhu.propra.team61.gui.SceneController;
import de.hhu.propra.team61.io.CustomizeManager;
import de.hhu.propra.team61.io.TerrainManager;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

import static de.hhu.propra.team61.JavaFxUtils.toHex;

/**
 * Created by Jessypet on 10.06.14.
 */

public class CustomizeWindow extends Application {

    SceneController sceneController = new SceneController();
    BorderPane root = new BorderPane();
    CustomGrid newTeamGrid = new CustomGrid();
    CustomGrid newGameStyleGrid = new CustomGrid();
    CustomGrid weaponsGrid = new CustomGrid();
    CustomGrid editGrid;
    ArrayList<TextField> figureNames = new ArrayList<>();
    ArrayList<String> weaponNames = new ArrayList<>();
    ArrayList<CheckBox> weaponCheckBoxes = new ArrayList<>();
    ArrayList<Slider> weaponSliders = new ArrayList<>();
    TextField name = new TextField("player");
    ColorPicker color = new ColorPicker(Color.web("#FF00FF"));
    ToggleGroup figure = new ToggleGroup();
    RadioButton penguin = new RadioButton("Penguin");
    RadioButton unicorn = new RadioButton("Unicorn");
    TextField styleNameField = new TextField("Custom");
    TextField sizeField = new TextField("4");
    ChoiceBox<String> mapChooser = new ChoiceBox<>();
    ArrayList<String> availableTeams = new ArrayList<>();


    public CustomizeWindow(SceneController sceneController) {
        this.sceneController = sceneController;
        initializeArrayLists();
        createEditGrid();
        createNewTeamGrid();
        createNewGameStyleGrid();
        createTopBox();
        root.setLeft(editGrid);
        Scene customizeScene = new Scene(root, 1000, 600);
        customizeScene.getStylesheets().add("file:resources/layout/css/customize.css");
        sceneController.setCustomizeScene(customizeScene);
        sceneController.switchToCustomize();
    }

    public void createTopBox() {
        HBox topBox = new HBox(20);
        Button edit = new Button("Edit an existing team/game style");
        edit.setOnAction(e -> {
            root.getChildren().remove(weaponsGrid);
            createEditGrid();
            root.setLeft(editGrid);
        });
        Button newTeam = new Button("Create new team");
        newTeam.setOnAction(e -> {
            refresh();
            root.setLeft(newTeamGrid);
            root.getChildren().remove(weaponsGrid);
        });
        Button newGameStyle = new Button("Create new game style");
        newGameStyle.setOnAction(e -> {
            refresh();
            root.setLeft(newGameStyleGrid);
        });
        Button backToMenue = new Button("Go back to menue");
        backToMenue.setOnAction(e -> {
            sceneController.switchToMenue();
        });
        topBox.getChildren().addAll(edit, newTeam, newGameStyle, backToMenue);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);
    }

    public void createEditGrid() {
        editGrid = new CustomGrid();
        Text whatToDoHere = new Text("Here you can edit or remove an existing team or game style.");
        editGrid.add(whatToDoHere, 0, 2, 3, 1);
        Text teamsText = new Text("Teams:");
        teamsText.setFont(Font.font("Verdana", 20));
        editGrid.add(teamsText, 0, 4, 2, 1);
        Text stylesText = new Text("Game Styles:");
        stylesText.setFont(Font.font("Verdana", 20));
        editGrid.add(stylesText, 3, 4, 2, 1);
        getTeams();
        getGameStyles();
    }

    public void createNewTeamGrid() {
        Text wormNamesText = new Text("Figures (enter names):");
        wormNamesText.setFont(Font.font("Verdana", 15));
        newTeamGrid.add(wormNamesText, 0, 2);
        Text nameText = new Text("Team-Name:");
        nameText.setFont(Font.font("Verdana", 15));
        newTeamGrid.add(nameText, 2, 2);
        newTeamGrid.add(name, 2, 3, 2, 1);
        Text colorText = new Text("Team-Color:");
        colorText.setFont(Font.font("Verdana", 15));
        newTeamGrid.add(colorText, 2, 4);
        newTeamGrid.add(color, 2, 5);
        Text figureText = new Text("Figure");
        figureText.setFont(Font.font("Verdana", 15));
        newTeamGrid.add(figureText, 2, 6);
        penguin.setToggleGroup(figure);
        penguin.setSelected(true);
        unicorn.setToggleGroup(figure);
        newTeamGrid.add(penguin, 2, 7);
        newTeamGrid.add(unicorn, 3, 7);
        Button saveTeam = new Button("Save");
        saveTeam.setOnAction(e -> {
            CustomizeManager.save(teamToJson(), "teams/"+name.getText());
            createEditGrid();
            root.setLeft(editGrid);
            root.getChildren().remove(weaponsGrid);
        });
        newTeamGrid.add(saveTeam, 0, 10);
    }

    public void createNewGameStyleGrid() {
        Text styleName = new Text("Style-Name:");
        styleName.setFont(Font.font("Verdana", 15));
        newGameStyleGrid.add(styleName, 0, 2);
        newGameStyleGrid.add(styleNameField, 1, 2);
        Text sizeText = new Text("Team-Size:");
        sizeText.setFont(Font.font("Verdana", 15));
        newGameStyleGrid.add(sizeText, 0, 3);
        newGameStyleGrid.add(sizeField, 1, 3);
        Text chooseMapText = new Text("Choose map:");
        chooseMapText.setFont(Font.font("Verdana", 15));
        newGameStyleGrid.add(chooseMapText, 0, 4);
        ArrayList<String> availableLevels = getLevels();
        int numberOfLevels = TerrainManager.getNumberOfAvailableTerrains();
        for (int i=0; i<numberOfLevels; i++) {
            mapChooser.getItems().add(availableLevels.get(i));
        }
        mapChooser.getSelectionModel().selectFirst();
        newGameStyleGrid.add(mapChooser, 1, 4);
        Button saveGameStyle = new Button("Save");
        saveGameStyle.setOnAction(e -> {
            CustomizeManager.save(styleToJson(), "gamestyles/"+styleNameField.getText());
            createEditGrid();
            root.setLeft(editGrid);
            root.getChildren().remove(weaponsGrid);
        });
        newGameStyleGrid.add(saveGameStyle, 0, 10);
        Button changeWeapons = new Button("Change weapons");
        newGameStyleGrid.add(changeWeapons, 0, 5);
        changeWeapons.setOnAction(e -> {
            root.setRight(weaponsGrid);
        });
        Text howToChangeWeapons = new Text("Add/remove/edit a certain weapon.");
        newGameStyleGrid.add(howToChangeWeapons, 1, 5, 2, 1);
        Label weapons = new Label("Weapons");
        weapons.setFont(Font.font("Verdana", 20));
        weaponsGrid.add(weapons, 0, 2);
        Text enter = new Text ("Enter the quantity of projectiles for each weapon.");
        weaponsGrid.add(enter, 0, 3, 3, 1);
    }

    private void editTeam(String teamName) {
        fromJson(teamName, true);
        root.setLeft(newTeamGrid);
    }

    private void editStyle(String styleName) {
        fromJson(styleName, false);
        root.setLeft(newGameStyleGrid);
    }

    public ArrayList<String> getLevels() {
        ArrayList<String> levels = TerrainManager.getAvailableTerrains();
        return levels;
    }

    public void getTeams() {
        ArrayList<String> availableTeams = CustomizeManager.getAvailableTeams();
        for (int i=0; i<availableTeams.size(); i++) {
            Button chooseTeamToEdit = new Button(availableTeams.get(i));
            final int finalI = i;
            chooseTeamToEdit.setOnAction(e -> {
                refresh();
                editTeam(availableTeams.get(finalI));
            });
            editGrid.add(chooseTeamToEdit, 0, i+5);
            Button remove = new Button("X");
            remove.setOnAction(e -> {
                deleteFile("teams/" + chooseTeamToEdit.getText());
            });
            editGrid.add(remove, 1, i+5);
        }
    }

    public void getGameStyles() {
        ArrayList<String> availableGameStyles = CustomizeManager.getAvailableGameStyles();
        for (int i=0; i<availableGameStyles.size(); i++) {
            Button chooseStyleToEdit = new Button(availableGameStyles.get(i));
            final int finalI = i;
            chooseStyleToEdit.setOnAction(e -> {
                refresh();
                editStyle(availableGameStyles.get(finalI));
            });
            editGrid.add(chooseStyleToEdit, 3, i+5);
            Button remove = new Button("X");
            remove.setOnAction(e -> {
                deleteFile("gamestyles/"+chooseStyleToEdit.getText());
            });
            editGrid.add(remove, 4, i+5);
        }
    }

    public void deleteFile(String fileName) {
        File file = new File("resources/"+fileName);
        if (file.delete()){
            System.out.println("File "+fileName+" deleted.");
        }
        createEditGrid();
        root.setLeft(editGrid);
    }

    public JSONObject teamToJson() {
        JSONObject output = new JSONObject();
        output.put("name", name.getText());
        output.put("color", toHex(color.getValue()));
        output.put("figure", figure.getSelectedToggle());
        JSONObject figureNamesJson = new JSONObject();
        for (int i=0; i<6; i++) {
            figureNamesJson.put("figure"+(i+1), figureNames.get(i).getText());
        }
        output.put("figure-names", figureNamesJson);
        return output;
    }

    public JSONObject styleToJson() {
        JSONObject output = new JSONObject();
        output.put("name", styleNameField.getText());
        output.put("team-size", sizeField.getText());
        output.put("map", mapChooser.getValue());
        JSONArray weapons = new JSONArray();
        for (int i=0; i<weaponNames.size(); i++) {
            JSONObject weapon = new JSONObject();
            weapon.put("weapon"+(i+1), (int) weaponSliders.get(i).getValue());
            weapons.put(weapon);
        }
        output.put("weapons", weapons);
        return output;
    }

    public void fromJson(String file, Boolean choseTeam) {
        if (choseTeam) {
            JSONObject savedTeam = CustomizeManager.getSavedSettings("teams/" + file);
            if (savedTeam.has("name")) {
                name.setText(savedTeam.getString("name"));
            }
            if (savedTeam.has("color")) {
                color.setValue(Color.web(savedTeam.getString("color")));
            }
            //TODO load chosen figure
            if (savedTeam.has("figure-names")) {
                JSONObject figureNamesJson = savedTeam.getJSONObject("figure-names");
                for (int i = 0; i < 6; i++) {
                    figureNames.get(i).setText(figureNamesJson.getString("figure" + (i + 1)));
                }
            }
        } else {
            JSONObject savedStyle = CustomizeManager.getSavedSettings("gamestyles/" + file);
            if (savedStyle.has("name")) {
                styleNameField.setText(savedStyle.getString("name"));
            }
            if (savedStyle.has("team-size")) {
                sizeField.setText(savedStyle.getString("team-size"));
            }
            if (savedStyle.has("map")) {
                mapChooser.setValue(savedStyle.getString("map"));
            }
            if (savedStyle.has("weapons")) {
                JSONArray weapons = savedStyle.getJSONArray("weapons");
                for (int i=0; i<weaponNames.size(); i++) {
                    weaponSliders.get(i).setValue(weapons.getJSONObject(i).getInt("weapon"+(i+1)));
                    weaponCheckBoxes.get(i).setSelected(weapons.getJSONObject(i).getInt("weapon"+(i+1))>0);
                }
            }
        }
    }

    public void initializeArrayLists() {
        for (int i=0; i<6; i++) {
            figureNames.add(new TextField("Character" + (i+1)));
            newTeamGrid.add(figureNames.get(i), 0, i+3);
        }
        weaponNames.add("Bazooka");
        weaponNames.add("Grenade");
        weaponNames.add("Pistol");
        weaponNames.add("Poisoned arrow");
        for (int i=0; i<weaponNames.size(); i++) {
            weaponCheckBoxes.add(new CheckBox(weaponNames.get(i)));
            weaponCheckBoxes.get(i).setSelected(true);
            final int finalI = i;
            weaponCheckBoxes.get(i).selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        weaponSliders.get(finalI).setValue(0);
                    } else {
                        weaponSliders.get(finalI).setValue(50);
                    }
                }
            });
            weaponsGrid.add(weaponCheckBoxes.get(i), 0, i + 5);
        }
        for (int i=0; i<weaponNames.size(); i++) {
            weaponSliders.add(new Slider(0, 100, 50));
            weaponSliders.get(i).setShowTickMarks(true);
            weaponSliders.get(i).setShowTickLabels(true);
            weaponsGrid.add(weaponSliders.get(i), 1, i+5);
        }
    }

    public void refresh() {
        name.setText("player");
        color.setValue(Color.web("#FF00FF"));
        for (int i=0; i<6; i++) {
            figureNames.get(i).setText("Character"+(i+1));
        }
        styleNameField.setText("Custom");
        sizeField.setText("4");
        mapChooser.getSelectionModel().selectFirst();
        for (int i=0; i<weaponNames.size(); i++) {
            weaponCheckBoxes.get(i).setSelected(true);
            weaponSliders.get(i).setValue(50);
        }
    }

    @Override
    public void start(Stage filler) {}

}
