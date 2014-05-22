package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.GameState;
import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.IO.TerrainManager;
import de.hhu.propra.team61.Objects.Figure;
import de.hhu.propra.team61.Objects.Gun;
import de.hhu.propra.team61.Objects.Terrain;
import de.hhu.propra.team61.Objects.Weapon;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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
    private Figure nextUp;
    private Scene drawing;
    private Stage primaryStage;
    private BorderPane root;
    private StackPane centerView;
    private Terrain terrain;
    private Label teamLabel;
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
        for(int i=0; i<2; i++) { // TODO hard coded 2 teams, 2 figures
            teams.add(new Team(terrain.getRandomSpawnPoints(2)));
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

        turnCount = input.getInt("turnCount");

        initialize();
    }

    /**
     * creates the stage, so that everything is visible
     */
    private void initialize() {
        primaryStage = new Stage();
        primaryStage.setOnCloseRequest(event -> {
            GameState.save(this.toJson());
            TerrainManager.save(terrain.toArrayList());
            System.out.println("MapWindow: saved game state");
        });

        // pane containing terrain, labels at the bottom etc.
        root = new BorderPane();
        // contains the terrain with figures
        centerView = new StackPane();
        centerView.setAlignment(Pos.TOP_LEFT);
        centerView.getChildren().add(terrain);
        root.setCenter(centerView);
        for(Team team: teams) {
            centerView.getChildren().add(team);
        }
        nextUp = teams.get((turnCount % teams.size())).getFigures().iterator().next();
        teamLabel = new Label("Team" + (turnCount % teams.size()) + "s turn. What will " + nextUp.getName() + " do?");

        root.setBottom(teamLabel);

        drawing = new Scene(root, 800, 600);
        drawing.setOnKeyPressed(
                keyEvent -> {
                    System.out.println("key pressed: " + keyEvent.getCode());
                    switch (keyEvent.getCode()) {
                        case L:
                        case NUMBER_SIGN:
                            cheatMode();
                            break;
                        case SPACE: //Fire
                            nextUp.getSelectedItem().shoot();

                            centerView.getChildren().remove(nextUp.getSelectedItem().getCrosshair());
                            centerView.getChildren().remove(nextUp.getSelectedItem());

                            endTurn();
                            break;
                        case UP:
                        case W:
                            nextUp.getSelectedItem().angle_up(nextUp.getFacing_right());
                            break;
                        case LEFT:
                        case A:
                            nextUp.setFacing_right(false);
                            nextUp.getSelectedItem().angle_draw(nextUp.getFacing_right());
                            break;
                        case DOWN:
                        case S:
                            nextUp.getSelectedItem().angle_down(nextUp.getFacing_right());
                            break;
                        case RIGHT:
                        case D:
                            nextUp.setFacing_right(true);
                            nextUp.getSelectedItem().angle_draw(nextUp.getFacing_right());
                            break;
                        case DIGIT1:
                            Weapon w1 = new Gun(nextUp.getPosition(),nextUp.getFacing_right());
                            nextUp.setSelectedItem(w1);
                            centerView.getChildren().add(nextUp.getSelectedItem()); // ToDo hardcoded, but sufficient for now
                            centerView.getChildren().add(nextUp.getSelectedItem().getCrosshair());
                            break;
                      /*  case DIGIT2:
                            Weapon w2 = new Gun(nextUp.getPosition(),nextUp.getFacing_right());
                            nextUp.setSelectedItem(w2);
                            centerView.getChildren().add(nextUp.getSelectedItem()); // ToDo hardcoded, but sufficient for now
                            centerView.getChildren().add(nextUp.getSelectedItem().getCrosshair());
                            break;
                        case DIGIT3:
                            Weapon w3 = new Gun(nextUp.getPosition(),nextUp.getFacing_right());
                            nextUp.setSelectedItem(w3);
                            centerView.getChildren().add(nextUp.getSelectedItem()); // ToDo hardcoded, but sufficient for now
                            centerView.getChildren().add(nextUp.getSelectedItem().getCrosshair());
                            break;*/
                        case K: // Kollision test
                            Figure f = teams.get(0).getFigures().get(0);
                            Point2D pos = new Point2D(f.getPosition().getX()*8, f.getPosition().getY()*8);
                            Point2D v = new Point2D(0, 5);
                            Rectangle2D hitr = new Rectangle2D(pos.getX(), pos.getY()+1, 8, 8);
                            terrain.getPositionForDirection(pos, v, hitr, false);
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
        JSONObject output = new JSONObject();
        JSONArray teamsArray = new JSONArray();
        for(Team t: teams) {
            teamsArray.put(t.toJson());
        }
        output.put("teams", teamsArray);
        output.put("turnCount", turnCount);
        return output;
    }


    public void cheatMode() {
        try {
            levelCounter++;
            terrain.load(TerrainManager.load(TerrainManager.getAvailableTerrains().get(levelCounter = levelCounter % TerrainManager.getNumberOfAvailableTerrains())));
            // quite bad hack to reload spawn points, but ok as it's a cheat anyway
            for(Team team: teams) {
                centerView.getChildren().remove(team);
            }
            teams.clear();
            for(int i=0; i<2; i++) { // TODO hard coded 2 teams, 2 figures
                Team team = new Team(terrain.getRandomSpawnPoints(2));
                teams.add(team);
                centerView.getChildren().add(team);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void endTurn() {
        //activeTeam = (activeTeam == team.length()-1 ? 0 : activeTeam+1);
        turnCount++;
        int teamCount = turnCount % teams.size();
        nextUp = teams.get(teamCount).getFigures().iterator().next(); // ToDo add Loop so that last ist connected to first. Either by if here or changing ArrayList into a Ring
        teamLabel.setText("Team" + teamCount + "s turn. What will " + nextUp.getName() + " do?");
        System.out.println("Turn " + turnCount + ", Team " + teamCount + ", Worm \"" + nextUp.getName() + "\"");
    }

    @Override
    public void start(Stage ostage) {
    }

}
