package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.GameState;
import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.IO.TerrainManager;
import de.hhu.propra.team61.Objects.Terrain;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by kegny on 08.05.14.
 * Edited by DiniiAntares on 15.05.14
 * This class is supposed to draw the Array given by "TerrainManager" rendering the Map visible.
 */
public class MapWindow extends Application {
    private ArrayList<Team> teams;
    private Scene drawing;
    private Stage primaryStage;
    private StackPane root;
    private Terrain terrain;
    private int activeTeam = 0;
    private int turnCount = 0;
    private int levelCounter = 0;

    public MapWindow(String map) {
        try {
            terrain = new Terrain(TerrainManager.load(map));
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
            this.terrain = new Terrain(TerrainManager.loadSavedLevel());
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
            TerrainManager.save(terrain.toArrayList());
            System.out.println("MapWindow: saved game state");
        });

        root = new StackPane();
        root.getChildren().add(terrain);

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

    public void cheatMode() {
        try {
            levelCounter++;
            terrain.load(TerrainManager.load(TerrainManager.getAvailableTerrains().get(levelCounter = levelCounter % TerrainManager.getNumberOfAvailableTerrains())));
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
