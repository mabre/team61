package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.CustomGrid;
import de.hhu.propra.team61.gui.SceneController;
import de.hhu.propra.team61.io.CustomizeManager;
import de.hhu.propra.team61.io.TerrainManager;
import de.hhu.propra.team61.io.VorbisPlayer;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static de.hhu.propra.team61.JavaFxUtils.toHex;

/**
 * The window that is shown when clicking 'Customize' in main menue.
 *
 * This class contains GUI for creating new teams, game styles and levels.
 * Custom teams, styles and levels can also be edited or deleted.
 *
 * Created by Jessypet on 10.06.14.
 */

public class CustomizeWindow extends Application {

    /** used to switch back to menue */
    private SceneController sceneController = new SceneController();
    /** root for scene and for all GUI-elements */
    private BorderPane root = new BorderPane();
    /** grid that contains lists of existing teams, game styles and levels */
    private CustomGrid editGrid;

    ///Team editor
    /** grid for all GUI-elements for creating a team */
    private CustomGrid newTeamGrid = new CustomGrid();
    /** contains names of the team's figures */
    private ArrayList<TextField> figureNames = new ArrayList<>();
    /** TextField to enter wanted team name */
    private TextField name = new TextField("player");
    /** ColorPicker to choose wanted team color */
    private ColorPicker color = new ColorPicker(Color.web("#663366"));
    /** choose penguin or unicorn as the team's figure */
    private ChoiceBox<String> figureChooser = new ChoiceBox<>();

    ///Style editor
    /** grid for all GUI-elements for creating a game style */
    private CustomGrid newGameStyleGrid = new CustomGrid();
    /** grid containing elements for changing items */
    private CustomGrid itemsGrid = new CustomGrid();
    /**TextField for entering name of the game style */
    private TextField styleNameField = new TextField("Custom");
    /** TextField for entering team-size of the game-style */
    private TextField sizeField = new TextField("4");
    /** list of all items */
    private ArrayList<String> itemNames = new ArrayList<>();
    /** CheckBoxes to decide whether an item should be in the game */
    private ArrayList<CheckBox> itemCheckBoxes = new ArrayList<>();
    /** Sliders to choose quantity for each item */
    private ArrayList<Slider> itemSliders = new ArrayList<>();

    ///Level editor
    //Buttons to choose terrain type
    private Button stone = new Button();
    private Button soil = new Button();
    private Button sand = new Button();
    private Button ice = new Button();
    private Button snow = new Button();
    private Button rightEdge = new Button();
    private Button leftEdge = new Button();
    Button eraser = new Button("Eraser");
    Button reset = new Button("Reset");
    Button save = new Button("Save");
    Button spawnPoint = new Button("Spawn point");

    private BorderPane newMapPane = new BorderPane();
    private CustomGrid newMapGrid = new CustomGrid();
    /** ChoiceBox to choose map */
    private ChoiceBox<String> mapChooser = new ChoiceBox<>();
    /** ChoiceBox to choose background music */
    private ChoiceBox<String> musicChooser = new ChoiceBox<>();
    /** ChoiceBox to choose background imgae */
    private ChoiceBox<String> imageChooser = new ChoiceBox<>();
    /** ChoiceBox to choose liquid in the bottom of the level */
    private ChoiceBox<String> liquidChooser = new ChoiceBox<>();
    /** shows text of the button the mouse is focusing */
    private Text terrainType = new Text();
    /** the map to be loaded */
    private String chosenMap = new String("editor/basic.lvl");
    /** TextField for wanted name of a custom map */
    private TextField mapNameField = new TextField("Custom map");
    /** contains buttons to choose terrain */
    private CustomGrid selectionGrid = new CustomGrid();
    /** used for drawing the chosen terrain block */
    private char chosenTerrainType = 'S';
    /** chosen brush width */
    private int chosenBrushWidth = 3;
    /** Slider to choose the width of the brush */
    private Slider brushWidth = new Slider(1, 7, 3);
    /** contains level */
    private ScrollPane scrollPane;
    /** anchors levelTerrain at the bottom */
    private AnchorPane anchorPane;
    /** Terrain to draw level in  */
    private Terrain levelTerrain;
    /** contains scrollPane and background */
    private StackPane levelPane;
    /** contains chosen background image */
    private Pane background = new Pane();
     /** directory of terrain-images */
    private String img_path = "file:resources/terrain/";

    ///Tetris-Cheat
    private String keysEntered = "";
    private boolean cheatEnabled = false;
    private Figure block = null;
    private Thread moveBlockThread = null;

    /**
     * initializes all GUI-elements and switches to customizeScene
     * @param sceneController makes switching between scenes in one stage possible
     */
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
        sceneController.switchScene(customizeScene, "Customize");
    }

    /**
     * creates menue to switch between teams, game styles, levels and the main menue
     */
    private void createTopBox() {
        HBox topBox = new HBox(20);
        Button edit = new Button("Edit team/game style/map");
        edit.getStyleClass().add("mainButton");
        edit.setOnAction(e -> {
            root.getChildren().remove(itemsGrid);
            createEditGrid();
            root.setLeft(editGrid);
        });
        Button newTeam = new Button("Create new team");
        newTeam.getStyleClass().add("mainButton");
        newTeam.setOnAction(e -> {
            refresh();
            root.setLeft(newTeamGrid);
            root.getChildren().remove(itemsGrid);
        });
        Button newGameStyle = new Button("Create new game style");
        newGameStyle.getStyleClass().add("mainButton");
        newGameStyle.setOnAction(e -> {
            refresh();
            root.setLeft(newGameStyleGrid);
        });
        Button newMap = new Button("Create new map");
        newMap.getStyleClass().add("mainButton");
        newMap.setOnAction(e -> {
            refresh();
            root.getChildren().remove(itemsGrid);
            chosenMap = "editor/basic.lvl";
            initializeLevelEditor();
            root.setLeft(newMapPane);
            scrollPane.requestFocus(); // to make cheat work right away
        });
        Button backToMenu = new Button("Go back to menu");
        backToMenu.getStyleClass().add("mainButton");
        backToMenu.setOnAction(e -> {
            stopCheat();
            sceneController.switchToMenu();
        });
        topBox.getChildren().addAll(edit, newTeam, newGameStyle, newMap, backToMenu);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);
    }

    /**
     * shows lists of all existing teams, game styles and levels
     */
    private void createEditGrid() {
        editGrid = new CustomGrid();
        Text teamsText = new Text("Teams:");
        teamsText.setFont(Font.font("Verdana", 20));
        editGrid.add(teamsText, 0, 0, 2, 1);
        getTeams();
        Text stylesText = new Text("Game Styles:");
        stylesText.setFont(Font.font("Verdana", 20));
        editGrid.add(stylesText, 1, 0, 2, 1);
        getGameStyles();
        Text mapsText = new Text("Maps:");
        mapsText.setFont(Font.font("Verdana", 20));
        editGrid.add(mapsText, 2, 0, 2, 1);
        getMaps();
        Image customizeImage = new Image("file:resources/layout/customize.png");
        editGrid.add(new ImageView(customizeImage), 3, 1, 3, 7);
    }

    /**
     * GUI for creating and saving a new team
     */
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
        saveTeam.getStyleClass().add("mainButton");
        saveTeam.setOnAction(e -> {
            CustomizeManager.save(teamToJson(), "teams/" + name.getText());
            createEditGrid();
            root.setLeft(editGrid);
            root.getChildren().remove(itemsGrid);
        });
        newTeamGrid.add(saveTeam, 0, 10);
    }

    /**
     * GUI for creating and saving a new game style
     */
    private void createGameStyle() {
        Text styleName = new Text("Style-Name:");
        styleName.setFont(Font.font("Verdana", 15));
        newGameStyleGrid.add(styleName, 0, 2);
        newGameStyleGrid.add(styleNameField, 1, 2);
        Text sizeText = new Text("Team-Size:");
        sizeText.setFont(Font.font("Verdana", 15));
        sizeField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (Integer.parseInt(newValue) > 6) {
                    sizeField.setText("6");
                } else if (Integer.parseInt(newValue) < 1) {
                    sizeField.setText("1");
                }
            }
        });
        newGameStyleGrid.add(sizeText, 0, 3);
        newGameStyleGrid.add(sizeField, 1, 3);
        Text chooseMapText = new Text("Choose map:");
        chooseMapText.setFont(Font.font("Verdana", 15));
        newGameStyleGrid.add(chooseMapText, 0, 4);
        mapChooser = new ChoiceBox<>();
        ArrayList<String> availableLevels = getLevels();
        int numberOfLevels = TerrainManager.getNumberOfAvailableTerrains();
        for (int i=0; i<numberOfLevels; i++) {
            mapChooser.getItems().add(JavaFxUtils.removeExtension(availableLevels.get(i), 4));
        }
        mapChooser.getSelectionModel().selectFirst();
        newGameStyleGrid.add(mapChooser, 1, 4);
        Button saveGameStyle = new Button("Save");
        saveGameStyle.getStyleClass().add("mainButton");
        saveGameStyle.setOnAction(e -> {
            CustomizeManager.save(styleToJson(), "gamestyles/"+styleNameField.getText());
            createEditGrid();
            root.setLeft(editGrid);
            root.getChildren().remove(itemsGrid);
        });
        newGameStyleGrid.add(saveGameStyle, 0, 10);
        Button changeItems = new Button("Change items");
        changeItems.getStyleClass().add("mainButton");
        newGameStyleGrid.add(changeItems, 0, 5);
        changeItems.setOnAction(e -> {
            root.setRight(itemsGrid);
        });
        Text whatIsChangeItems = new Text("Add/remove/edit weapons or other items.");
        newGameStyleGrid.add(whatIsChangeItems, 1, 5, 2, 1);
        Label items = new Label("Items");
        items.setFont(Font.font("Verdana", 20));
        itemsGrid.add(items, 0, 2);
        Text enter = new Text ("Enter the quantity of each item.\n " +
                "(number of projectiles for weapons)");
        itemsGrid.add(enter, 0, 3, 2, 2);
    }

    /**
     * Creating GUI around the level editor. Adds all elements but the drawing area:
     * <ul>
     *     <li>ChoiceBoxes for music, image, fluid and TextField for name + their effect</li>
     *     <li>Terrain-Buttons</li>
     *     <li>Saving and resetting levels</li>
     * </ul>
     * Also contains EventFilter for Tetris-cheat.
     */
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
                background.setStyle("-fx-background-image: url('" + "file:resources/levels/"+new_value + ".png" + "')");
            }
        });
        Text liquid = new Text("Liquid:");
        newMapGrid.add(liquid, 4, 0);
        liquidChooser.getItems().addAll("Water", "Lava");
        liquidChooser.getSelectionModel().selectFirst();
        newMapGrid.add(liquidChooser, 5, 0);
        liquidChooser.valueProperty().addListener((ov, value, new_value) -> {
            if (new_value.equals("Water")) {
                for (int i = 0; i < levelTerrain.getTerrainWidth()/Terrain.BLOCK_SIZE; i++) {
                    levelTerrain.replaceBlock(i, levelTerrain.getTerrainHeight()/Terrain.BLOCK_SIZE-1, 'W');
                }
            } else {
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
                        if (block.getPosition().getX() + 8 < levelTerrain.getTerrainWidth())
                            moveBlock(8, 0);
                        break;
                    case DOWN:
                        moveBlock(0, 16);
                        break;
                    case LEFT:
                        if (block.getPosition().getX() > 8)
                            moveBlock(-8, 0);
                        break;
                    default:
                        stopCheat();
                }
                keyEvent.consume();
            }
        });
        initializeLevelEditor();
        stone.setGraphic(new ImageView(new Image(img_path + "stones.png")));
        actionForTerrainButton(stone, "Stone", 'S');
        selectionGrid.add(stone, 0, 0);
        soil.setGraphic(new ImageView(new Image(img_path + "soil.png")));
        actionForTerrainButton(soil, "Soil", 'E');
        selectionGrid.add(soil, 0, 1);
        sand.setGraphic(new ImageView(new Image(img_path + "sand.png")));
        actionForTerrainButton(sand, "Sand", 's');
        selectionGrid.add(sand, 0, 2);
        ice.setGraphic(new ImageView(new Image(img_path + "ice.png")));
        actionForTerrainButton(ice, "Ice", 'I');
        selectionGrid.add(ice, 0, 3);
        snow.setGraphic(new ImageView(new Image(img_path + "snow.png")));
        actionForTerrainButton(snow, "Snow", 'i');
        selectionGrid.add(snow, 0, 4);
        rightEdge.setGraphic(new ImageView(new Image(img_path + "slant_ground_ri.png")));
        actionForTerrainButton(rightEdge, "Right edge", '/');
        selectionGrid.add(rightEdge, 0, 5);
        leftEdge.setGraphic(new ImageView(new Image(img_path + "slant_ground_le.png")));
        actionForTerrainButton(leftEdge, "Left edge", '\\');
        selectionGrid.add(leftEdge, 0, 6);
        selectionGrid.add(terrainType, 0, 7, 5, 1);
        eraser.getStyleClass().add("mainButton");
        actionForTerrainButton(eraser, "Erase parts of the map.", ' ');
        selectionGrid.add(eraser, 0, 11);
        spawnPoint.getStyleClass().add("mainButton");
        actionForTerrainButton(spawnPoint, "Set a spawn point", 'P');
        selectionGrid.add(spawnPoint, 0, 12);
        reset.setOnAction(e -> {
            //Remove all blocks, set to board.png and water
            initializeLevelEditor();
        });
        reset.getStyleClass().add("mainButton");
        actionForTerrainButton(reset, "Remove your masterpiece :(", 'S');
        selectionGrid.add(reset, 0, 13);
        save.setOnAction(e -> {
            CustomizeManager.saveMap(mapToJson(), "levels/" + mapNameField.getText());
            refresh();
            createEditGrid();
            root.setLeft(editGrid);
        });
        save.getStyleClass().add("mainButton");
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
        VorbisPlayer.play("resources/audio/BGM/korobeiniki.ogg", true);
        spawnBlock();
        moveBlockThread.start();
    }

    private void stopCheat() {
        cheatEnabled = false;
        if(moveBlockThread != null) moveBlockThread.interrupt();
        VorbisPlayer.stop();
        if(block != null) anchorPane.getChildren().removeAll(block);
    }

    private void spawnBlock() {
        String path;
        switch((int)(Math.random()*5)) {
            case 0:
                chosenTerrainType = 'S';
                path = "../terrain/stones";
                break;
            case 1:
                chosenTerrainType = 'E';
                path = "../terrain/soil";
                break;
            case 2:
                chosenTerrainType = 's';
                path = "../terrain/sand";
                break;
            case 3:
                chosenTerrainType = 'i';
                path = "../terrain/snow";
                break;
            default:
                chosenTerrainType = 'I';
                path = "../terrain/ice";
        }
        int hp;
        switch((int)(Math.random()*6)) {
            case 0:
                hp = 2;
                break;
            case 1:
                hp = 21;
                break;
            case 2:
                hp = 42;
                break;
            case 3:
                hp = 61;
                break;
            case 4:
                hp = 1337;
                break;
            default:
                hp = 2014;
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

    /**
     * This method add EventHandlers to every button in selectionGrid. When the mouse is entering a button,
     * a text is displayed giving information about the button. Exiting the button the text disappears. Pressing an
     * terrainButton will change the chosen terrain type.
     * @param terrainButton the button to add EventHandlers to
     * @param terrain the text to be shown when the mouse is over terrainButton
     * @param character the character that is used in Terrain to draw the right block
     */
    private void actionForTerrainButton(Button terrainButton, final String terrain, char character) {
        terrainButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        terrainType.setText(terrain);
                    }
                }
        );
        terrainButton.addEventHandler(MouseEvent.MOUSE_EXITED,
        new EventHandler<MouseEvent>() {
        @Override
            public void handle(MouseEvent e) {
                terrainType.setText("");
            }
        });
        if (terrainButton!=reset && terrainButton!=save) {
            terrainButton.setOnAction(e -> {
                chosenTerrainType = character;
            });
        }
    }

    /**
     * Creates the drawing area. It's a stack of background and levelTerrain. Clicking or dragging will call
     * {@link de.hhu.propra.team61.objects.Terrain#replaceBlock(int, int, char)} and draw the chosen terrain type.
     */
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
            scrollPane.getStyleClass().add("scrollPane");
            scrollPane.viewportBoundsProperty().addListener((observableValue, oldBounds, newBounds) ->
                anchorPane.setPrefSize(Math.max(levelTerrain.getBoundsInParent().getMaxX(), newBounds.getWidth()), Math.max(levelTerrain.getBoundsInParent().getMaxY(), newBounds.getHeight()))
            );
            scrollPane.setContent(anchorPane);
            background.setStyle("-fx-background-image: url('" + "file:resources/levels/" + imageChooser.getValue() + ".png" + "')");
            levelPane = new StackPane();
            levelPane.setPrefSize(750, 560);
            levelPane.setMaxHeight(560);
            levelPane.getChildren().addAll(background, scrollPane);
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
    }

    /**
     * Gets position of the mouse-click and draws block using {@link de.hhu.propra.team61.objects.Terrain#replaceBlock(int, int, char)}.
     * First checks if user dragged the mouse outside of the drawing area to avoid an IndexOutOfBoundsException.
     * @param mouseEvent event that contains information about the position of the mouse
     */
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

    /**
     * Calls method {@link #fromJson(String, Boolean)} to load team chosen to edit.
     * @param teamName name of the team to load
     */
    private void editTeam(String teamName) {
        fromJson(teamName, true);
        root.setLeft(newTeamGrid);
    }

    /**
     * Calls method {@link #fromJson(String, Boolean)} to load game style chosen to edit.
     * @param styleName name of the game style to load
     */
    private void editStyle(String styleName) {
        fromJson(styleName, false);
        root.setLeft(newGameStyleGrid);
    }

    /**
     * Calls method {@link de.hhu.propra.team61.io.TerrainManager#getAvailableTerrains()} to search for existing levels
     * @return ArrayList of available levels
     */
    private ArrayList<String> getLevels() {
        ArrayList<String> levels = TerrainManager.getAvailableTerrains();
        return levels;
    }

    /**
     * searches for the available background images
     */
    private void getBackgroundImages() {
        ArrayList<String> backgroundImages = CustomizeManager.getAvailableBackgrounds();
        for (int i=0; i<backgroundImages.size(); i++) {
            imageChooser.getItems().add(JavaFxUtils.removeExtension(backgroundImages.get(i), 4));
        }
        imageChooser.getSelectionModel().selectFirst();
    }

    /**
     * Searches for existing teams and adds a button for each team. Clicking one button will call {@link #editTeam(String)}.
     * Also adds possibility to delete a team.
     */
    private void getTeams() {
        ArrayList<String> availableTeams = CustomizeManager.getAvailableTeams();
        ScrollPane teamPane = new ScrollPane();
        Pane teamList = new Pane();
        teamList.getStyleClass().add("list");
        ArrayList<HBox> hboxes = new ArrayList<>();
        CustomGrid teamGrid = new CustomGrid();
        for (int i=0; i<availableTeams.size(); i++) {
            Button chooseTeamToEdit = new Button(JavaFxUtils.removeExtension(availableTeams.get(i), 5));
            final int finalI = i;
            chooseTeamToEdit.setOnAction(e -> {
                refresh();
                editTeam(availableTeams.get(finalI));
            });
            chooseTeamToEdit.getStyleClass().add("listButton");
            Button remove = new Button("X");
            remove.setId("removeButton");
            remove.setOnAction(e -> {
                deleteFile("teams/" + chooseTeamToEdit.getText() +".json");
            });
            hboxes.add(new HBox(20));
            hboxes.get(i).getStyleClass().add("listHBox");
            hboxes.get(i).setAlignment(Pos.CENTER);
            hboxes.get(i).getChildren().addAll(chooseTeamToEdit, remove);
            teamGrid.add(hboxes.get(i), 0, i);
        }
        teamList.getChildren().add(teamGrid);
        teamPane.setContent(teamList);
        teamPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        teamPane.getStyleClass().add("scrollPane");
        teamPane.setPrefSize(220, 450);
        editGrid.add(teamPane, 0, 1, 1, 10);
    }

    /**
     * Searches for existing game styles and adds a button for each style. Clicking one button will call {@link #editStyle(String)}.
     * Also adds possibility to delete a game style.
     */
    private void getGameStyles() {
        ArrayList<String> availableGameStyles = CustomizeManager.getAvailableGameStyles();
        ScrollPane stylePane = new ScrollPane();
        Pane styleList = new Pane();
        styleList.getStyleClass().add("list");
        ArrayList<HBox> hboxes = new ArrayList<>();
        CustomGrid styleGrid = new CustomGrid();
        for (int i=0; i<availableGameStyles.size(); i++) {
            Button chooseStyleToEdit = new Button(JavaFxUtils.removeExtension(availableGameStyles.get(i), 5));
            final int finalI = i;
            chooseStyleToEdit.setOnAction(e -> {
                refresh();
                editStyle(availableGameStyles.get(finalI));
            });
            chooseStyleToEdit.getStyleClass().add("listButton");
            Button remove = new Button("X");
            remove.setId("removeButton");
            remove.setOnAction(e -> {
                deleteFile("gamestyles/"+chooseStyleToEdit.getText()+".json");
            });
            hboxes.add(new HBox(20));
            hboxes.get(i).setAlignment(Pos.CENTER);
            hboxes.get(i).getStyleClass().add("listHBox");
            hboxes.get(i).getChildren().addAll(chooseStyleToEdit, remove);
            styleGrid.add(hboxes.get(i), 0, i);
        }
        styleList.getChildren().add(styleGrid);
        stylePane.setContent(styleList);
        stylePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        stylePane.getStyleClass().add("scrollPane");
        stylePane.setPrefSize(220, 450);
        editGrid.add(stylePane, 1, 1, 1, 10);
    }

    /**
     * Searches for existing levels and adds a button for each level. Clicking one button will call
     * {@link #initializeLevelEditor()} and load the chosen level into the editor.
     * Also adds possibility to delete a level.
     */
    private void getMaps() {
        ArrayList<String> availableMaps = CustomizeManager.getAvailableLevels();
        ScrollPane mapPane = new ScrollPane();
        Pane mapList = new Pane();
        mapList.getStyleClass().add("list");
        ArrayList<HBox> hboxes = new ArrayList<>();
        CustomGrid mapGrid = new CustomGrid();
        for (int i=0; i<availableMaps.size(); i++) {
            Button chooseMapToEdit = new Button(JavaFxUtils.removeExtension(availableMaps.get(i), 4));
            final int finalI = i;
            chooseMapToEdit.setOnAction(e -> {
                refresh();
                chosenMap = availableMaps.get(finalI);
                initializeLevelEditor();
                mapNameField.setText(chosenMap);
                root.setLeft(newMapPane);
            });
            chooseMapToEdit.getStyleClass().add("listButton");
            Button remove = new Button("X");
            remove.setId("removeButton");
            remove.setOnAction(e -> {
                deleteFile("levels/" + chooseMapToEdit.getText() +".lvl");
            });
            hboxes.add(new HBox(20));
            hboxes.get(i).setAlignment(Pos.CENTER);
            hboxes.get(i).getStyleClass().add("listHBox");
            hboxes.get(i).getChildren().addAll(chooseMapToEdit, remove);
            mapGrid.add(hboxes.get(i), 0, i);
        }
        mapList.getChildren().add(mapGrid);
        mapPane.setContent(mapList);
        mapPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mapPane.getStyleClass().add("scrollPane");
        mapPane.setPrefSize(220, 450);
        editGrid.add(mapPane, 2, 1, 1, 10);
    }

    /**
     * Deletes a file.
     * @param fileName name of the file to be deleted
     */
    private void deleteFile(String fileName) {
        File file = new File("resources/"+fileName);
        if (file.delete()){
            System.out.println("File "+fileName+" deleted.");
        }
        createEditGrid();
        root.setLeft(editGrid);
    }

    /**
     * Saves settings for the created team.
     * @return JSON-Object that contains all settings for the created team
     */
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

    /**
     * Saves settings for the created game style.
     * @return JSON-Object that contains all settings for the created game style
     */
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

    /**
     * Saves the created level and additional settings as background music.
     * @return JSON-Object that contains all settings for the created level + the level itself
     */
    private JSONObject mapToJson() {
        JSONObject output = new JSONObject();
        output.put("background", imageChooser.getValue()+".png");
        output.put("music", musicChooser.getValue());
        JSONArray jsonTerrain = levelTerrain.toJson().getJSONArray("terrain");
        output.put("terrain", jsonTerrain);
        return output;
    }

    /**
     * Loads settings for either a chosen game style or a chosen team into the GUI-elements which can be edited afterwards.
     * @param file File to load settings from
     * @param choseTeam boolean to indicate whether a team or a game style was chosen
     */
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

    /**
     * Initializes the list of items that can be changed for a game style. Creates a CheckBox and a Slider for each item
     * and sets them to default values.
     */
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
            itemsGrid.add(itemCheckBoxes.get(i), 0, i + 6);
        }
        for (int i=0; i<itemNames.size(); i++) {
            itemSliders.add(new Slider(0, 100, 50));
            itemSliders.get(i).setShowTickMarks(true);
            itemSliders.get(i).setShowTickLabels(true);
            itemSliders.get(i).setBlockIncrement(50);
            itemsGrid.add(itemSliders.get(i), 1, i + 6, 3, 1);
        }
    }

    /**
     * Sets all TextFields etc. back to default values when another menue-point is clicked.
     */
    private void refresh() {
        name.setText("player");
        color.setValue(Color.web("#663366"));
        for (int i=0; i<6; i++) {
            figureNames.get(i).setText("Character"+(i+1));
        }
        styleNameField.setText("Custom");
        sizeField.setText("4");
        for (int i=0; i< itemNames.size(); i++) {
            itemCheckBoxes.get(i).setSelected(true);
            itemSliders.get(i).setValue(50);
        }
        mapChooser.getSelectionModel().selectFirst();
        mapChooser = new ChoiceBox<>();
        ArrayList<String> availableLevels = getLevels();
        int numberOfLevels = TerrainManager.getNumberOfAvailableTerrains();
        for (int i=0; i<numberOfLevels; i++) {
            mapChooser.getItems().add(JavaFxUtils.removeExtension(availableLevels.get(i), 4));
        }
        mapChooser.getSelectionModel().selectFirst();
        newGameStyleGrid.add(mapChooser, 1, 4);
        mapNameField.setText("Custom map");
    }

    @Override
    public void start(Stage filler) {}

}
