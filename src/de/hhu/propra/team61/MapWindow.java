package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.GameState;
import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.IO.Settings;
import de.hhu.propra.team61.IO.TerrainManager;
import de.hhu.propra.team61.Objects.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

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
    private Projectile flyingProjectile = null;
    private Thread moveObjectsThread;
    private Stage stageToClose;
    private int teamquantity;
    private int teamsize;

    @Deprecated
    public MapWindow(String map) {
        try {
            terrain = new Terrain(TerrainManager.load(map));
        } catch (FileNotFoundException e) {
            // TODO do something sensible here
            e.printStackTrace();
        }

        teams = new ArrayList<>();
        for(int i=0; i<2; i++) { // TODO hard coded 2 teams, 2 figures
            ArrayList<Weapon> weapons = new ArrayList<>();
            weapons.add(new Gun("file:resources/weapons/temp1.png", 50, 2));
            weapons.add(new Grenade("file:resources/weapons/temp2.png", 40, 2));
            teams.add(new Team(terrain.getRandomSpawnPoints(2), weapons));
        }

        initialize();
    }

    public MapWindow(String map, Stage stageToClose, String file) {
        try {
            terrain = new Terrain(TerrainManager.load(map));
        } catch (FileNotFoundException e) {
            // TODO do something sensible here
            e.printStackTrace();
        }

        this.stageToClose = stageToClose;
        JSONObject settings = Settings.getSavedSettings(file);
        this.teamquantity = settings.getInt("quantity");
        this.teamsize = Integer.parseInt(settings.getString("team-size"));
        teams = new ArrayList<>();
        for(int i=0; i<teamquantity; i++) {
            ArrayList<Weapon> weapons = new ArrayList<>();
            weapons.add(new Gun("file:resources/weapons/temp1.png", 50, settings.getInt("weapon1")));
            weapons.add(new Grenade("file:resources/weapons/temp2.png", 40, settings.getInt("weapon2")));
            weapons.add(new Gun("file:resources/weapons/temp3.png", 30, settings.getInt("weapon3")));
            teams.add(new Team(terrain.getRandomSpawnPoints(teamsize), weapons));
        }

        initialize();
    }

    public MapWindow(JSONObject input, Stage stageToClose) {
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
        this.stageToClose = stageToClose;
        initialize();
    }

    /**
     * creates the stage, so that everything is visible
     */
    private void initialize() {
        primaryStage = new Stage();
        primaryStage.setOnCloseRequest(event -> {
            moveObjectsThread.interrupt();

            GameState.save(this.toJson());
            TerrainManager.save(terrain.toArrayList());
            System.out.println("MapWindow: saved game state");
            stageToClose.show();
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
            terrain.addFigures(team.getFigures());
        }
        nextUp = teams.get((turnCount % teams.size())).getFigures().iterator().next();
        teamLabel = new Label("Team" + (turnCount % teams.size()) + "s turn. What will " + nextUp.getName() + " do?");

        root.setBottom(teamLabel);

        drawing = new Scene(root, 800, 600);
        drawing.setOnKeyPressed(
                keyEvent -> {
                    System.out.println("key pressed: " + keyEvent.getCode());
                    Point2D v = null;
                    switch (keyEvent.getCode()) {
                        case L:
                        case NUMBER_SIGN:
                            cheatMode();
                            break;
                        case SPACE: //Fire
                            if(nextUp.getSelectedItem() != null) {
                                try {
                                    Projectile projectile = nextUp.shoot(); // ToDo Do something with the projectile
                                    flyingProjectile = projectile;
                                    centerView.getChildren().add(flyingProjectile);
                                } catch (NoMunitionException e) {
                                    System.out.println("no munition");
                                    break;
                                }

                                centerView.getChildren().remove(nextUp.getSelectedItem().getCrosshair());
                                centerView.getChildren().remove(nextUp.getSelectedItem());

                                nextUp.setSelectedItem(null);
                            }
                            break;
                        case UP:
                        case W:
                            if(nextUp.getSelectedItem() != null) {
                                nextUp.getSelectedItem().angle_up(nextUp.getFacing_right());
                            }
                            break;
                        case LEFT:
                        case A:
                            nextUp.setFacing_right(false);
                            if(nextUp.getSelectedItem() != null) {
                                nextUp.getSelectedItem().angle_draw(nextUp.getFacing_right());
                            }
                            break;
                        case DOWN:
                        case S:
                            if(nextUp.getSelectedItem() != null) {
                                nextUp.getSelectedItem().angle_down(nextUp.getFacing_right());
                            }
                            break;
                        case RIGHT:
                        case D:
                            nextUp.setFacing_right(true);
                            if(nextUp.getSelectedItem() != null) {
                                nextUp.getSelectedItem().angle_draw(nextUp.getFacing_right());
                            }
                            break;
                        case DIGIT1: // ToDo hardcoded, but sufficient for now
                            if(teams.get(turnCount%teams.size()).getNumberOfWeapons() >= 1) {
                                if (nextUp.getSelectedItem() != null) {
                                    centerView.getChildren().remove(nextUp.getSelectedItem().getCrosshair());
                                    centerView.getChildren().remove(nextUp.getSelectedItem());
                                }
                                nextUp.setSelectedItem(teams.get(turnCount % teams.size()).getWeapon(0));
                                centerView.getChildren().add(nextUp.getSelectedItem());
                                centerView.getChildren().add(nextUp.getSelectedItem().getCrosshair());
                            }
                            break;
                        case DIGIT2:
                            if(teams.get(turnCount%teams.size()).getNumberOfWeapons() >= 2) {
                                if (nextUp.getSelectedItem() != null) {
                                    centerView.getChildren().remove(nextUp.getSelectedItem().getCrosshair());
                                    centerView.getChildren().remove(nextUp.getSelectedItem());
                                }
                                nextUp.setSelectedItem(teams.get(turnCount % teams.size()).getWeapon(1));
                                centerView.getChildren().add(nextUp.getSelectedItem());
                                centerView.getChildren().add(nextUp.getSelectedItem().getCrosshair());
                            }
                            break;
                        case DIGIT3:
                            if(teams.get(turnCount%teams.size()).getNumberOfWeapons() >= 3) {
                                if (nextUp.getSelectedItem() != null) {
                                    centerView.getChildren().remove(nextUp.getSelectedItem().getCrosshair());
                                    centerView.getChildren().remove(nextUp.getSelectedItem());
                                }
                                nextUp.setSelectedItem(teams.get(turnCount % teams.size()).getWeapon(2));
                                centerView.getChildren().add(nextUp.getSelectedItem());
                                centerView.getChildren().add(nextUp.getSelectedItem().getCrosshair());
                            }
                            break;
                        case K: // Kollision test // TODO remove, replace with real movement
                            v = new Point2D(+10, 0);
                        case J:
                            if(v==null) v = new Point2D(-10, 0);
                            Figure f = teams.get(0).getFigures().get(0);
                            Point2D pos = new Point2D(f.getPosition().getX()*8, f.getPosition().getY()*8);
                            Rectangle2D hitr = new Rectangle2D(pos.getX(), pos.getY(), 8, 8);
                            Point2D newPos = null;
                            try {
                                newPos = terrain.getPositionForDirection(pos, v, hitr, true, true, true);
                                // TODO rethink parameters of setPosition(), /8 is bad!
                            } catch (CollisionWithTerrainException e) {
                                System.out.println("CollisionWithTerrainException, stopped movement");
                                newPos = e.getLastGoodPosition();
                            } catch (CollisionWithFigureException e) {
                                // figures can walk through each other // TODO really?
                                System.out.println("ERROR How did we get here?");
                            }
                            f.setPosition(new Point2D(newPos.getX()/8, newPos.getY()/8));
                            break;
                    }
                }
        );

        primaryStage.setTitle("The Playground");
        primaryStage.setScene(drawing);
        primaryStage.show();

        moveObjectsThread = new Thread(() -> { // TODO move this code to own class
            try {
                long before = System.currentTimeMillis(), now, sleep;
                while (true) {
                    //System.out.println("here");
                    if(flyingProjectile != null) {
                        try {
                            final Point2D newPos;
                            newPos = terrain.getPositionForDirection(flyingProjectile.getPosition(), flyingProjectile.getVelocity(), flyingProjectile.getHitRegion(), false, false, false);
                            Platform.runLater(() -> flyingProjectile.setPosition(new Point2D(newPos.getX(), newPos.getY())));
                        } catch (CollisionWithTerrainException e) {
                            System.out.println("CollisionWithTerrainException, let's destroy something!"); // TODO
                            final Point2D newPos = e.getLastGoodPosition();
                            //Platform.runLater(() -> flyingProjectile.setPosition(new Point2D(newPos.getX(), newPos.getY())));
                            Platform.runLater(() -> {
                                centerView.getChildren().remove(flyingProjectile);
                                flyingProjectile = null;
                                endTurn();
                            }); // TODO potential race condition
                        } catch (CollisionWithFigureException e) {
                            System.out.println("CollisionWithFigureException, let's harm somebody!");
                            Platform.runLater(() -> {
                                e.getCollisionPartner().sufferDamage(flyingProjectile.getDamage());
                                centerView.getChildren().remove(flyingProjectile);
                                flyingProjectile = null;
                                endTurn();
                            }); // TODO potential race condition
                        }
                    }
                    now = System.currentTimeMillis();
                    sleep = Math.max(0, (1000/10)-(now-before)); // 10 fps
                    Thread.sleep(sleep);
                    before = System.currentTimeMillis();
                }
            } catch (InterruptedException e) {
                System.out.println("moveObjectsThread shut down");
            }
        });
        moveObjectsThread.start();
        stageToClose.close();
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
            for(int i=0; i<teamquantity; i++) { // TODO hard coded 2 teams, 2 figures
                ArrayList<Weapon> weapons = new ArrayList<>();
                weapons.add(new Gun("file:resources/weapons/temp1.png", 50, 2));
                weapons.add(new Grenade("file:resources/weapons/temp2.png", 40, 2));
                Team team = new Team(terrain.getRandomSpawnPoints(teamsize), weapons);
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
