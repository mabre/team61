package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.CustomGrid;
import de.hhu.propra.team61.gui.SceneController;
import de.hhu.propra.team61.io.TerrainManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Created by Jessypet on 10.06.14.
 */

public class CustomizeWindow extends Application {

    SceneController sceneController = new SceneController();
    BorderPane root = new BorderPane();
    CustomGrid newTeamGrid = new CustomGrid();
    CustomGrid newGameStyleGrid = new CustomGrid();
    CustomGrid weaponsGrid = new CustomGrid();
    CustomGrid editGrid = new CustomGrid();
    ArrayList<TextField> wormNames = new ArrayList<>();
    ArrayList<String> weaponNames = new ArrayList<>();
    ArrayList<CheckBox> weaponCheckBoxes = new ArrayList<>();
    ArrayList<TextField> weaponTextFields = new ArrayList<>();

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
            root.setLeft(editGrid);
            root.getChildren().remove(weaponsGrid);
        });
        Button newTeam = new Button("Create new team");
        newTeam.setOnAction(e -> {
            root.setLeft(newTeamGrid);
            root.getChildren().remove(weaponsGrid);
        });
        Button newGameStyle = new Button("Create new game style");
        newGameStyle.setOnAction(e -> {
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
        Text whatToDoHere = new Text("Here you can edit or remove an existing team or game style.");
        editGrid.add(whatToDoHere, 0, 2, 3, 1);
        Text teamsText = new Text("Teams:");
        teamsText.setFont(Font.font("Verdana", 20));
        editGrid.add(teamsText, 0, 4);
        Text stylesText = new Text("Game Styles:");
        stylesText.setFont(Font.font("Verdana", 20));
        editGrid.add(stylesText, 3, 4);
        //TODO show existing teams and game styles
    }

    public void createNewTeamGrid() {
        Text wormNamesText = new Text("Figures (enter names):");
        wormNamesText.setFont(Font.font("Verdana", 15));
        newTeamGrid.add(wormNamesText, 0, 2);
        Text nameText = new Text("Team-Name:");
        nameText.setFont(Font.font("Verdana", 15));
        newTeamGrid.add(nameText, 2, 2);
        TextField name = new TextField("player");
        newTeamGrid.add(name, 2, 3, 2, 1);
        Text colorText = new Text("Team-Color:");
        colorText.setFont(Font.font("Verdana", 15));
        newTeamGrid.add(colorText, 2, 4);
        ColorPicker color = new ColorPicker(Color.web("#FF00FF"));
        newTeamGrid.add(color, 2, 5);
        Text figureText = new Text("Figure");
        figureText.setFont(Font.font("Verdana", 15));
        newTeamGrid.add(figureText, 2, 6);
        ToggleGroup figure = new ToggleGroup();
        RadioButton penguin = new RadioButton("Penguin");
        RadioButton unicorn = new RadioButton("Unicorn");
        penguin.setToggleGroup(figure);
        penguin.setSelected(true);
        unicorn.setToggleGroup(figure);
        newTeamGrid.add(penguin, 2, 7);
        newTeamGrid.add(unicorn, 3, 7);
        Button saveTeam = new Button("Save");
        saveTeam.setOnAction(e -> {
            //teamToJson();
        });
        newTeamGrid.add(saveTeam, 0, 10);
    }

    public void createNewGameStyleGrid() {
        Text styleName = new Text("Style-Name:");
        styleName.setFont(Font.font("Verdana", 15));
        TextField styleNameField = new TextField("Custom");
        newGameStyleGrid.add(styleName, 0, 2);
        newGameStyleGrid.add(styleNameField, 1, 2);
        Text sizeText = new Text("Team-Size:");
        sizeText.setFont(Font.font("Verdana", 15));
        TextField sizeField = new TextField("4");
        newGameStyleGrid.add(sizeText, 0, 3);
        newGameStyleGrid.add(sizeField, 1, 3);
        ChoiceBox<String> mapChooser = new ChoiceBox<>();
        Text chooseMapText = new Text("Choose map:");
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
            //styleToJson();
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

    public ArrayList<String> getLevels() {
        ArrayList<String> levels = TerrainManager.getAvailableTerrains();
        return levels;
    }

    public void initializeArrayLists() {
        for (int i=0; i<6; i++) {
            wormNames.add(new TextField("Character" + (i+1)));
            newTeamGrid.add(wormNames.get(i), 0, i+3);
        }
        weaponNames.add("Bazooka");
        weaponNames.add("Grenade");
        weaponNames.add("Pistol");
        weaponNames.add("Poisoned arrow");
        for (int i=0; i<weaponNames.size(); i++) {
            weaponCheckBoxes.add(new CheckBox(weaponNames.get(i)));
            weaponCheckBoxes.get(i).setSelected(true);
            weaponsGrid.add(weaponCheckBoxes.get(i), 0, i+5);
        }
        for (int i=0; i<weaponNames.size(); i++) {
            int quantityInt = (int) (50*Math.random());
            String quantity = String.valueOf(quantityInt);
            weaponTextFields.add(new TextField(quantity));
            weaponsGrid.add(weaponTextFields.get(i), 1, i+5);
        }
    }

    @Override
    public void start(Stage filler) {}

}
