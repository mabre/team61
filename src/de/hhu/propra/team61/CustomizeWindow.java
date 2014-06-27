package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.CustomGrid;
import de.hhu.propra.team61.gui.SceneController;
import de.hhu.propra.team61.io.CustomizeManager;
import de.hhu.propra.team61.io.TerrainManager;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.objects.CollisionException;
import de.hhu.propra.team61.objects.Figure;
import de.hhu.propra.team61.objects.Terrain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.FileNotFoundException;
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
    BorderPane newMapPane = new BorderPane();
    CustomGrid newMapGrid = new CustomGrid();
    ArrayList<TextField> figureNames = new ArrayList<>();
    ArrayList<String> itemNames = new ArrayList<>();
    ArrayList<CheckBox> itemCheckBoxes = new ArrayList<>();
    ArrayList<Slider> itemSliders = new ArrayList<>();
    TextField name = new TextField("player");
    ColorPicker color = new ColorPicker(Color.web("#FF00FF"));
    ChoiceBox<String> figureChooser = new ChoiceBox<>();
    TextField styleNameField = new TextField("Custom");
    TextField sizeField = new TextField("4");
    ChoiceBox<String> mapChooser = new ChoiceBox<>();
    ChoiceBox<String> musicChooser = new ChoiceBox<>();
    ChoiceBox<String> imageChooser = new ChoiceBox<>();
    ChoiceBox<String> liquidChooser = new ChoiceBox<>();
    ScrollPane scrollPane;
    AnchorPane anchorPane;
    Terrain levelTerrain;
    StackPane levelPane;
    Pane background = new Pane();
    CustomGrid selectionGrid = new CustomGrid();
    private char chosenTerrainType = 'S';
    private int chosenBrushWidth = 3;
    private String keysEntered = "";
    private boolean cheatEnabled = false;
    private Figure block = null;
    private Thread moveBlockThread = null;
    Clip clip = null;
    Text terrainType = new Text();
    Button stone = new Button();
    String chosenMap = new String("editor/basic.lvl");
    TextField mapNameField = new TextField("Custom map");
    Slider brushWidth = new Slider(1, 7, 3);
    private String img_path = new String("file:resources/");
    private Image image = new Image(img_path + "stone.png");
    private ImageView imageView = new ImageView(image);

    public CustomizeWindow(SceneController sceneController) {
        this.sceneController = sceneController;
        initializeArrayLists();
        createEditGrid();
        createTeam();
        createGameStyle();
        createMap();
        createTopBox();
        root.setLeft(editGrid);
        Scene customizeScene = new Scene(root, 1000, 600);
        customizeScene.getStylesheets().add("file:resources/layout/css/customize.css");
        sceneController.setCustomizeScene(customizeScene);
        sceneController.switchToCustomize();
    }

    private void createTopBox() {
        HBox topBox = new HBox(20);
        Button edit = new Button("Edit team/game style/map");
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
        Button newMap = new Button("Create new map");
        newMap.setOnAction(e -> {
            refresh();
            root.getChildren().remove(weaponsGrid);
            chosenMap = "editor/basic.lvl";
            initializeLevelEditor();
            root.setLeft(newMapPane);
            scrollPane.requestFocus(); // to make cheat work right away
        });
        Button backToMenue = new Button("Go back to menue");
        backToMenue.setOnAction(e -> {
            stopCheat();
            sceneController.switchToMenue();
        });
        topBox.getChildren().addAll(edit, newTeam, newGameStyle, newMap, backToMenue);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);
    }

    private void createEditGrid() {
        editGrid = new CustomGrid();
        Text whatToDoHere = new Text("Here you can edit or remove an existing team or game style.");
        editGrid.add(whatToDoHere, 0, 2, 15, 1);
        Text teamsText = new Text("Teams:");
        teamsText.setFont(Font.font("Verdana", 20));
        editGrid.add(teamsText, 0, 4, 2, 1);
        getTeams();
        Text stylesText = new Text("Game Styles:");
        stylesText.setFont(Font.font("Verdana", 20));
        editGrid.add(stylesText, 5, 4, 2, 1);
        getGameStyles();
        Text mapsText = new Text("Maps:");
        mapsText.setFont(Font.font("Verdana", 20));
        editGrid.add(mapsText, 10, 4, 2, 1);
        getMaps();
    }

    private void createTeam() {
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
        figureChooser.getItems().addAll("Penguin", "Unicorn");
        figureChooser.getSelectionModel().selectFirst();
        newTeamGrid.add(figureChooser, 2, 7);
        Button saveTeam = new Button("Save");
        saveTeam.setOnAction(e -> {
            CustomizeManager.save(teamToJson(), "teams/" + name.getText());
            createEditGrid();
            root.setLeft(editGrid);
            root.getChildren().remove(weaponsGrid);
        });
        newTeamGrid.add(saveTeam, 0, 10);
    }

    private void createGameStyle() {
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
        Button changeItems = new Button("Change items");
        newGameStyleGrid.add(changeItems, 0, 5);
        changeItems.setOnAction(e -> {
            root.setRight(weaponsGrid);
        });
        Text whatIsChangeItems = new Text("Add/remove/edit weapons or other items.");
        newGameStyleGrid.add(whatIsChangeItems, 1, 5, 2, 1);
        Label items = new Label("Items");
        items.setFont(Font.font("Verdana", 20));
        weaponsGrid.add(items, 0, 2);
        Text enter = new Text ("Enter the quantity of each item.\n " +
                "(number of projectiles for weapons)");
        weaponsGrid.add(enter, 0, 3, 2, 2);
    }

    private void createMap() {
        Text music = new Text("Background music:");
        newMapGrid.add(music, 0, 0);
        musicChooser.getItems().add("dummy");
        musicChooser.getSelectionModel().selectFirst();
        newMapGrid.add(musicChooser, 1, 0);
        Text image = new Text("Background image:");
        newMapGrid.add(image, 2, 0);
        getBackgroundImages();
        newMapGrid.add(imageChooser, 3, 0);
        imageChooser.valueProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue ov, String value, String new_value) {
                background.setStyle("-fx-background-image: url('" + "file:resources/levels/"+new_value + "')");
            }
        });
        Text liquid = new Text("Liquid:");
        newMapGrid.add(liquid, 4, 0);
        liquidChooser.getItems().addAll("Water", "Lava");
        liquidChooser.getSelectionModel().selectFirst();
        newMapGrid.add(liquidChooser, 5, 0);
        liquidChooser.valueProperty().addListener((ov, value, new_value) -> {
            if (new_value.equals("Water")) {
            } else {
                for (int i = 0; i < levelTerrain.getTerrainWidth()/Terrain.BLOCK_SIZE; i++) {
                    levelTerrain.replaceBlock(i, levelTerrain.getTerrainHeight()/Terrain.BLOCK_SIZE-1, 'W');
                }
                for (int i = 0; i < levelTerrain.getTerrainWidth()/Terrain.BLOCK_SIZE; i++) {
                    levelTerrain.replaceBlock(i, levelTerrain.getTerrainHeight()/Terrain.BLOCK_SIZE-1, 'L');
                }
            }
        });
        Text mapName = new Text("Name:");
        newMapGrid.add(mapName, 6, 0);
        newMapGrid.add(mapNameField, 7, 0);
        newMapPane.setTop(newMapGrid);
        newMapPane.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            System.out.println("key pressed: " + keyEvent.getCode());
            if (!cheatEnabled) {
                switch (keyEvent.getCode()) {
                    case UP:
                    case RIGHT:
                    case DOWN:
                    case LEFT:
                    case A:
                    case B:
                        keysEntered += keyEvent.getCode();
                        if (!"UPUPDOWNDOWNLEFTRIGHTLEFTRIGHTBA".startsWith(keysEntered)) {
                            keysEntered = "";
                        } else if (keysEntered.equals("UPUPDOWNDOWNLEFTRIGHTLEFTRIGHTBA")) {
                            System.out.println("What is this?");
                            startCheat();
                        }
                        break;
                    default:
                        keysEntered = "";
                }
            } else {
                switch (keyEvent.getCode()) {
                    case RIGHT:
                        if(block.getPosition().getX()+8 < levelTerrain.getTerrainWidth())
                            moveBlock(8, 0);
                        break;
                    case DOWN:
                        moveBlock(0, 16);
                        break;
                    case LEFT:
                        if(block.getPosition().getX() > 8)
                            moveBlock(-8, 0);
                        break;
                    default:
                        stopCheat();
                }
                keyEvent.consume();
            }
        });
        initializeLevelEditor();
        stone.setGraphic(new ImageView(new Image(img_path + "stone.png")));
        actionForTerrainButton(stone, "Stone", 'S');
        selectionGrid.add(stone, 0, 0);
        Button soil = new Button();
        soil.setGraphic(new ImageView(new Image(img_path + "soil.png")));
        actionForTerrainButton(soil, "Soil", 'E');
        selectionGrid.add(soil, 0, 1);
        Button sand = new Button();
        sand.setGraphic(new ImageView(new Image(img_path + "stone.png")));
        actionForTerrainButton(sand, "Sand", 's');
        selectionGrid.add(sand, 0, 2);
        Button ice = new Button();
        ice.setGraphic(new ImageView(new Image(img_path + "ice.png")));
        actionForTerrainButton(ice, "Ice", 'I');
        selectionGrid.add(ice, 0, 3);
        Button snow = new Button();
        snow.setGraphic(new ImageView(new Image(img_path + "stone.png")));
        actionForTerrainButton(snow, "Snow", 'i');
        selectionGrid.add(snow, 0, 4);
        Button rightEdge = new Button();
        rightEdge.setGraphic(new ImageView(new Image(img_path + "rightedge.png")));
        actionForTerrainButton(rightEdge, "Right edge", '/');
        selectionGrid.add(rightEdge, 0, 5);
        Button leftEdge = new Button();
        leftEdge.setGraphic(new ImageView(new Image(img_path + "leftedge.png")));
        actionForTerrainButton(leftEdge, "Left edge", '\\');
        selectionGrid.add(leftEdge, 0, 6);
        selectionGrid.add(terrainType, 0, 7, 5, 1);
        Button eraser = new Button("Eraser");
        actionForTerrainButton(eraser, "Erase parts of the map.", ' ');
        selectionGrid.add(eraser, 0, 11);
        Button spawnPoint = new Button("Spawn point");
        actionForTerrainButton(spawnPoint, "Set a spawn point.", 'P');
        selectionGrid.add(spawnPoint, 0, 12);
        Button reset = new Button("Reset");
        reset.setOnAction(e -> {
            //Remove all blocks, set to board.png and water
            initializeLevelEditor();
        });
        actionForTerrainButton(reset, "Remove your masterpiece :(", chosenTerrainType);
        selectionGrid.add(reset, 0, 13);
        Button save = new Button("Save");
        save.setOnAction(e -> {
            CustomizeManager.saveMap(mapToJson(), "levels/" + mapNameField.getText());
            createEditGrid();
            root.setLeft(editGrid);
        });
        actionForTerrainButton(save, "Save the map.", chosenTerrainType);
        selectionGrid.add(save, 0, 14);
        Text brush = new Text("Brush width: ");
        selectionGrid.add(brush, 1, 0);
        brushWidth.setOrientation(Orientation.VERTICAL);
        brushWidth.setShowTickLabels(true);
        brushWidth.setShowTickMarks(true);
        brushWidth.setMinorTickCount(0);
        brushWidth.setMajorTickUnit(2);
        brushWidth.setSnapToTicks(true);
        brushWidth.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                chosenBrushWidth = new_val.intValue();
            }
        });
        selectionGrid.add(brushWidth, 1, 1, 1, 4);
        newMapPane.setRight(selectionGrid);
    }

    private void startCheat() {
        moveBlockThread = new Thread(() -> {
            try {
                long before = System.currentTimeMillis(), now, sleep;
                while (true) {
                    if(block != null) {
                        final Point2D oldPos = block.getPosition();
                        try {
                            Point2D newPos = levelTerrain.getPositionForDirection(oldPos, new Point2D(0, 8), block.getHitRegion(), false, true, false, true);
                            block.setPosition(new Point2D(newPos.getX(), newPos.getY()));
                        } catch (CollisionException e) {
                            System.out.println("CollisionWithTerrainException");
                            final Figure oldBlock = block;
                            block = null; // do not continue with moving this block
                            oldBlock.setPosition(new Point2D(e.getLastGoodPosition().getX(), e.getLastGoodPosition().getY()));
                            Platform.runLater(() -> {
                                int minX = (int) oldBlock.getPosition().getX() / 8;
                                int maxX = (int) oldBlock.getPosition().getX() / 8 + 1;
                                int minY = (int) oldBlock.getPosition().getY() / 8;
                                int maxY = (int) oldBlock.getPosition().getY() / 8 + 1;
                                anchorPane.getChildren().removeAll(oldBlock);
                                spawnBlock();
                                for (int x = minX; x <= maxX; x++) {
                                    for (int y = minY; y <= maxY; y++) {
                                        levelTerrain.replaceBlock(x, y, oldBlock.getName().charAt(0));
                                    }
                                }
                            });
                        }
                    }
                    // sleep thread, and assure constant frame rate
                    now = System.currentTimeMillis();
                    sleep = Math.max(0, (int)(1000 / (90.0 / 60)) - (now - before)); // 90 BPM
                    Thread.sleep(sleep);
                    before = System.currentTimeMillis();
                }
            } catch (InterruptedException e) {
                System.out.println("moveObjectsThread shut down");
            }
        });
        cheatEnabled = true;
        keysEntered = "";
        playRussianFolkSong();
        spawnBlock();
        moveBlockThread.start();
    }

    private void stopCheat() {
        cheatEnabled = false;
        if(moveBlockThread != null) moveBlockThread.interrupt();
        if(clip != null) clip.stop();
        if(block != null) anchorPane.getChildren().removeAll(block);
    }

    private void spawnBlock() {
        String path;
        switch((int)(Math.random()*3)) {
            case 0:
                chosenTerrainType = 'S';
                path = "../stones";
                break;
            case 1:
                chosenTerrainType = 'E';
                path = "../earth";
                break;
            default:
                chosenTerrainType = 'I';
                path = "../ice";
        }
        int hp;
        switch((int)(Math.random()*4)) {
            case 0:
                hp = 2;
                break;
            case 1:
                hp = 42;
                break;
            case 2:
                hp = 61;
                break;
            default:
                hp = 1337;
        }
        Platform.runLater(() -> {
            block = new Figure(chosenTerrainType+"", path, hp, 0, false, false, false);
            block.setPosition(new Point2D(512, 16));
            anchorPane.getChildren().add(block);
        });
    }

    private void moveBlock(int dx, int dy) {
        if(block == null) return;
        final Point2D direction = new Point2D(dx, dy);
        final Point2D oldPos = block.getPosition();
        try {
            Point2D newPos = levelTerrain.getPositionForDirection(oldPos, direction, block.getHitRegion(), false, false, false, false);
            block.setPosition(new Point2D(newPos.getX(), newPos.getY()));
        } catch (CollisionException e) {
            System.out.println("CollisionWithTerrainException");
            block.setPosition(new Point2D(e.getLastGoodPosition().getX(), e.getLastGoodPosition().getY()));
        }
    }

    private void playRussianFolkSong() {
        try {
            clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("resources/audio/BGM/korobeiniki.wav"));
            clip.open(inputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void actionForTerrainButton(Button terrainButton, final String terrain, char character) {
        terrainButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        terrainType.setText(terrain);
                    }
                });
        terrainButton.addEventHandler(MouseEvent.MOUSE_EXITED,
        new EventHandler<MouseEvent>() {
        @Override
            public void handle(MouseEvent e) {
                terrainType.setText("");
            }
        });
        String terrainToUse = terrain.replaceAll("\\s","");
        terrainButton.setOnAction(e -> {
            chosenTerrainType = character;
            image = new Image(img_path + terrainToUse.toLowerCase() + ".png");
            imageView.setImage(image);
        });
    }

    private void initializeLevelEditor() {
        try {
            levelTerrain = new Terrain(TerrainManager.load(chosenMap), true);
            scrollPane = new ScrollPane();
            scrollPane.setPrefSize(750, 560);
            scrollPane.setMaxHeight(560);

            //anchor the editor to the bottom left corner (ScrollPane cannot do that)
            anchorPane = new AnchorPane();
            AnchorPane.setBottomAnchor(levelTerrain, 0.0);
            AnchorPane.setLeftAnchor(levelTerrain, 0.0);
            anchorPane.getChildren().add(levelTerrain);
            scrollPane.setId("scrollPane");
            scrollPane.viewportBoundsProperty().addListener((observableValue, oldBounds, newBounds) ->
                anchorPane.setPrefSize(Math.max(levelTerrain.getBoundsInParent().getMaxX(), newBounds.getWidth()), Math.max(levelTerrain.getBoundsInParent().getMaxY(), newBounds.getHeight()))
            );
            scrollPane.setContent(anchorPane);
            background.setStyle("-fx-background-image: url('" + "file:resources/levels/board.png" + "')");
            levelPane = new StackPane();
            Pane forImageView = new Pane();
            forImageView.setMaxSize(750, 560);
            forImageView.getChildren().add(imageView);
            levelPane.getChildren().addAll(background, forImageView, scrollPane);
            newMapPane.setLeft(levelPane);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        levelTerrain.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                draw(mouseEvent);
            }
        });
        levelTerrain.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                draw(mouseEvent);
            }
        });
        levelTerrain.setCursor(Cursor.NONE);
        levelTerrain.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int x = (int) mouseEvent.getX();
                int y = (int) mouseEvent.getY();
                imageView.setLayoutX(x);
                imageView.setLayoutY(y);
                levelTerrain.setCursor(Cursor.NONE);
            }
        });
    }

    private void draw(MouseEvent mouseEvent) {
        int x = (int)mouseEvent.getX()/Terrain.BLOCK_SIZE;
        int y = (int)mouseEvent.getY()/Terrain.BLOCK_SIZE;
        //Check to avoid IndexOutOfBoundsException (tries to replace block that doesn't exist) & erasing liquid
        if (chosenTerrainType != 'P') {
            for (int i = x - (chosenBrushWidth / 2); i <= x + (chosenBrushWidth / 2); i++) {
                for (int j = y - (chosenBrushWidth / 2); j <= y + (chosenBrushWidth / 2); j++) {
                    if (j < (levelTerrain.getTerrainHeight() - 1) / Terrain.BLOCK_SIZE && i < levelTerrain.getTerrainWidth() / Terrain.BLOCK_SIZE && j >= 0 && i >= 0) {
                        levelTerrain.replaceBlock(i, j, chosenTerrainType);
                    }
                }
            }
        } else {
            if (y < (levelTerrain.getTerrainHeight() - 1) / Terrain.BLOCK_SIZE && x < levelTerrain.getTerrainWidth() / Terrain.BLOCK_SIZE && y >= 0 && x >= 0) {
                levelTerrain.replaceBlock(x, y, chosenTerrainType);
            }
        }
    }

    private void editTeam(String teamName) {
        fromJson(teamName, true);
        root.setLeft(newTeamGrid);
    }

    private void editStyle(String styleName) {
        fromJson(styleName, false);
        root.setLeft(newGameStyleGrid);
    }

    private ArrayList<String> getLevels() {
        ArrayList<String> levels = TerrainManager.getAvailableTerrains();
        return levels;
    }

    private void getBackgroundImages() {
        ArrayList<String> backgroundImages = CustomizeManager.getAvailableBackgrounds();
        for (int i=0; i<backgroundImages.size(); i++) {
            imageChooser.getItems().add(backgroundImages.get(i));
        }
        imageChooser.getSelectionModel().selectFirst();
    }

    private void getTeams() {
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
            remove.setId("removeButton");
            remove.setOnAction(e -> {
                deleteFile("teams/" + chooseTeamToEdit.getText());
            });
            editGrid.add(remove, 1, i+5);
        }
    }

    private void getGameStyles() {
        ArrayList<String> availableGameStyles = CustomizeManager.getAvailableGameStyles();
        for (int i=0; i<availableGameStyles.size(); i++) {
            Button chooseStyleToEdit = new Button(availableGameStyles.get(i));
            final int finalI = i;
            chooseStyleToEdit.setOnAction(e -> {
                refresh();
                editStyle(availableGameStyles.get(finalI));
            });
            editGrid.add(chooseStyleToEdit, 5, i+5);
            Button remove = new Button("X");
            remove.setId("removeButton");
            remove.setOnAction(e -> {
                deleteFile("gamestyles/"+chooseStyleToEdit.getText());
            });
            editGrid.add(remove, 6, i+5);
        }
    }

    private void getMaps() {
        ArrayList<String> availableMaps = CustomizeManager.getAvailableMaps();
        for (int i=0; i<availableMaps.size(); i++) {
            Button chooseMapToEdit = new Button(availableMaps.get(i));
            final int finalI = i;
            chooseMapToEdit.setOnAction(e -> {
                refresh();
                chosenMap = availableMaps.get(finalI);
                initializeLevelEditor();
                mapNameField.setText(chosenMap);
                root.setLeft(newMapPane);
            });
            editGrid.add(chooseMapToEdit, 10, i+5);
            Button remove = new Button("X");
            remove.setId("removeButton");
            remove.setOnAction(e -> {
                deleteFile("levels/" + chooseMapToEdit.getText());
            });
            editGrid.add(remove, 11, i+5);
        }
    }

    private void deleteFile(String fileName) {
        File file = new File("resources/"+fileName);
        if (file.delete()){
            System.out.println("File "+fileName+" deleted.");
        }
        createEditGrid();
        root.setLeft(editGrid);
    }

    private JSONObject teamToJson() {
        JSONObject output = new JSONObject();
        output.put("name", name.getText());
        output.put("color", toHex(color.getValue()));
        output.put("figure", figureChooser.getValue());
        JSONObject figureNamesJson = new JSONObject();
        for (int i=0; i<6; i++) {
            figureNamesJson.put("figure"+i, figureNames.get(i).getText());
        }
        output.put("figure-names", figureNamesJson);
        return output;
    }

    private JSONObject styleToJson() {
        JSONObject output = new JSONObject();
        output.put("name", styleNameField.getText());
        output.put("team-size", sizeField.getText());
        output.put("map", mapChooser.getValue());
        JSONArray inventory = new JSONArray();
        for (int i=0; i< itemNames.size(); i++) {
            inventory.put((int) itemSliders.get(i).getValue());
        }
        output.put("inventory", inventory);
        return output;
    }

    private JSONObject mapToJson() {
        JSONObject output = new JSONObject();
        output.put("background", imageChooser.getValue());
        output.put("music", musicChooser.getValue());
        JSONArray jsonTerrain = levelTerrain.toJson().getJSONArray("terrain");
        output.put("terrain", jsonTerrain);
        return output;
    }

    private void fromJson(String file, Boolean choseTeam) {
        if (choseTeam) {
            JSONObject savedTeam = CustomizeManager.getSavedSettings("teams/" + file);
            if (savedTeam.has("name")) {
                name.setText(savedTeam.getString("name"));
            }
            if (savedTeam.has("color")) {
                color.setValue(Color.web(savedTeam.getString("color")));
            }
            if (savedTeam.has("figure")) {
                figureChooser.setValue(savedTeam.getString("figure"));
            }
            if (savedTeam.has("figure-names")) {
                JSONObject figureNamesJson = savedTeam.getJSONObject("figure-names");
                for (int i = 0; i < 6; i++) {
                    figureNames.get(i).setText(figureNamesJson.getString("figure"+i));
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
            if (savedStyle.has("inventory")) {
                JSONArray inventory = savedStyle.getJSONArray("inventory");
                for (int i=0; i< itemNames.size(); i++) {
                    itemSliders.get(i).setValue(inventory.getInt(i));
                    itemCheckBoxes.get(i).setSelected(inventory.getInt(i)>0);
                }
            }
        }
    }

    private void initializeArrayLists() {
        for (int i=0; i<6; i++) {
            figureNames.add(new TextField("Character" + (i+1)));
            newTeamGrid.add(figureNames.get(i), 0, i+3);
        }
        itemNames.add("Bazooka");
        itemNames.add("Grenade");
        itemNames.add("Pistol");
        itemNames.add("Poisoned arrow");
        itemNames.add("Medipack");
        itemNames.add("Rifle");
        itemNames.add("Banana-Bomb");
        for (int i=0; i< itemNames.size(); i++) {
            itemCheckBoxes.add(new CheckBox(itemNames.get(i)));
            itemCheckBoxes.get(i).setSelected(true);
            final int finalI = i;
            itemCheckBoxes.get(i).selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        itemSliders.get(finalI).setValue(0);
                    } else {
                        itemSliders.get(finalI).setValue(50);
                    }
                }
            });
            weaponsGrid.add(itemCheckBoxes.get(i), 0, i + 6);
        }
        for (int i=0; i<itemNames.size(); i++) {
            itemSliders.add(new Slider(0, 100, 50));
            itemSliders.get(i).setShowTickMarks(true);
            itemSliders.get(i).setShowTickLabels(true);
            itemSliders.get(i).setBlockIncrement(50);
            weaponsGrid.add(itemSliders.get(i), 1, i+6, 3, 1);
        }
    }

    private void refresh() {
        name.setText("player");
        color.setValue(Color.web("#FF00FF"));
        for (int i=0; i<6; i++) {
            figureNames.get(i).setText("Character"+(i+1));
        }
        styleNameField.setText("Custom");
        sizeField.setText("4");
        mapChooser.getSelectionModel().selectFirst();
        for (int i=0; i< itemNames.size(); i++) {
            itemCheckBoxes.get(i).setSelected(true);
            itemSliders.get(i).setValue(50);
        }
        mapNameField.setText("Custom map");
    }

    @Override
    public void start(Stage filler) {}

}
