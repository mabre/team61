package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.GameState;
import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.IO.TerrainManager;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by kegny on 08.05.14.
 * Edited by DiniiAntares on 15.05.14
 * This class is supposed to draw the Array given by "TerrainManager" rendering the Map visible.
 */
public class MapWindow extends Application {
    private ArrayList<ArrayList<Character>> terrain;
    private ArrayList<Team> teams;
    private Scene drawing;
    private Stage primaryStage;
    private StackPane root;
    private GridPane grid;
    private int activeTeam = 0;
    private int turnCount = 0;
    private int levelCounter = 0;


    public MapWindow(String map) {
        try {
            terrain = TerrainManager.load(map);
        } catch (FileNotFoundException e) {
            // TODO do something sensible here
            e.printStackTrace();
        }

        teams = new ArrayList<>();
        for(int i=0; i<2; i++) { // TODO hard coded 2 teams
            // TODO use Terrain class to get spawn points
            ArrayList<Point2D> spawnPoints = new ArrayList<>();
            spawnPoints.add(new Point2D(0,10*i));
            spawnPoints.add(new Point2D(0,30*i));
            teams.add(new Team(spawnPoints));
        }

        initialize();
    }

    public MapWindow(JSONObject input) {
        try {
            this.terrain = TerrainManager.loadSavedLevel();
        } catch (FileNotFoundException e) {
            // TODO do something sensible here
            e.printStackTrace();
        }

        teams = new ArrayList<>();
        JSONArray teamsArray = input.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            teams.add(new Team(teamsArray.getJSONObject(i)));
        }

        initialize();
    }

    private void initialize() {
        primaryStage = new Stage();
        primaryStage.setOnCloseRequest(event -> {
            GameState.save(this.toJson());
            //TerrainManager.save(terrain.toArrayList()); // TODO on other branch
            System.out.println("MapWindow: saved game state");
        });

        root = new StackPane();
        grid = new GridPane();

        draw();

        root.getChildren().add(grid);

        drawing = new Scene(root, 800, 600);
        drawing.setOnKeyPressed(
                keyEvent -> {
                    System.out.println("key pressed: " + keyEvent.getCode());
                    switch (keyEvent.getCode()) {
                        case NUMBER_SIGN:
                            cheatMode();
                            break;
                        case SPACE:
                            endTurn();
                            break;
                    }
                }
        );

        primaryStage.setTitle("The Playground");
        primaryStage.setScene(drawing);
        primaryStage.show();
    }

    /**
     * @return the whole state of the window as JSONObject (except terrain, use terrain.toArrayList())
     */
    public JSONObject toJson() {
        // TODO @DiniiAntares save/restore turnCount
        JSONObject output = new JSONObject();
        JSONArray teamsArray = new JSONArray();
        for(Team t: teams) {
            teamsArray.put(t.toJson());
        }
        output.put("teams", teamsArray);
        return output;
    }

    /**
     * creates Image objects for the fields
     */
    private void draw() {
        //Draw Terrain
        grid.getChildren().clear();
        grid.setGridLinesVisible(true); //Gridlines on/off
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < terrain.size(); i++) {
            for (int j = 0; j < terrain.get(i).size(); j++) {
                char terraintype = terrain.get(i).get(j);
                String loadImg = "file:resources/";
                switch (terraintype) {
                    case 'P':
                        loadImg += "spawn.png"; // TODO just temporary shown
                        break;
                    case '_':
                        loadImg += "plain_ground.png";
                        break;
                    case '/':
                        loadImg += "slant_ground_ri.png";
                        break;
                    case '\\':
                        loadImg += "slant_ground_le.png";
                        break;
                    case '|':
                        loadImg += "wall_le.png";
                        break;
                    case 'S':
                        loadImg += "stones.png";
                        break;
                    case 'E':
                        loadImg += "earth.png";
                        break;
                    case 'W':
                        loadImg += "water.png";  //Add Ice and Grass "I" and "G"
                        break;
                    case 'I':
                        loadImg += "ice.png";
                        break;
                    default:
                        loadImg += "sky.png";
                }
                Image image = new Image(loadImg);
                ImageView content = new ImageView();
                content.setImage(image);

                grid.add(content, j, i);
                //grid.setConstraints(content,j,i);
            }
        }
    }

    public void cheatMode() {
        try {
            levelCounter++;
            terrain = TerrainManager.load(TerrainManager.getAvailableTerrains().get(levelCounter = levelCounter % TerrainManager.getNumberOfAvailableTerrains()));
            draw();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void endTurn() {
        //activeTeam = (activeTeam == team.length()-1 ? 0 : activeTeam+1);
        turnCount++;
        turnCount = turnCount % teams.size(); //TODO graphical output for turnCount
    }

    @Override
    public void start(Stage ostage) {
    }

}
