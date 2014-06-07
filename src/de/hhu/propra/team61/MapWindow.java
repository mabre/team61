package de.hhu.propra.team61;

import de.hhu.propra.team61.GUI.Chat;
import de.hhu.propra.team61.IO.GameState;
import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.IO.Settings;
import de.hhu.propra.team61.IO.TerrainManager;
import de.hhu.propra.team61.Network.Client;
import de.hhu.propra.team61.Network.Networkable;
import de.hhu.propra.team61.Network.Server;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static de.hhu.propra.team61.JavaFxUtils.arrayToString;
import static de.hhu.propra.team61.JavaFxUtils.extractPart;

/**
 * Created by kevin on 08.05.14.
 * Edited by DiniiAntares on 15.05.14
 * This class is supposed to draw the Array given by "TerrainManager" rendering the Map visible.
 */
public class MapWindow extends Application implements Networkable {
    //JavaFX related variables
    private Scene drawing;
    private Stage primaryStage;
    private BorderPane root;
    private StackPane centerView;
    private Terrain terrain;
    private Label teamLabel;
    //Team related variables
    private ArrayList<Team> teams; //Dynamic list containing all playing teams
    private int currentTeam = 0;
    private int turnCount = 0;
    private int levelCounter = 0;
    private int power = 0; // Power/energy projectile is shot with
    private boolean shootingIsAllowed = true; // Used to disable shooting multiple times during 1 turn
    private int teamquantity;
    private int teamsize;
    //Projectile-Moving-Thread related variables
    private Projectile flyingProjectile = null;
    private Thread moveObjectsThread;
    private Stage stageToClose;
    //Network
    private Server server;
    private Client client;
    private Thread serverThread;
    private Thread clientThread;
    private String map; // TODO do we need this?
    private Chat chat;

    private final static int FIGURE_SPEED = 5;

    public MapWindow(String map, Stage stageToClose, String file, Client client, Thread clientThread, Server server, Thread serverThread) {
        this.map = map;
        this.client = client;
        this.clientThread = clientThread;
        client.registerCurrentNetworkable(this);
        this.server = server;
        this.serverThread = serverThread;
        if(server != null) server.registerCurrentNetworkable(this);

        // TODO code duplication; we have to check what we actually need at the end of the week
        try {
            terrain = new Terrain(TerrainManager.load(map));
        } catch (FileNotFoundException e) {
            // TODO do something sensible here
            e.printStackTrace();
        }

        this.stageToClose = stageToClose;
        JSONObject settings = Settings.getSavedSettings(file);
        this.teamquantity = settings.getInt("numberOfTeams");
        this.teamsize = Integer.parseInt(settings.getString("team-size"));
        teams = new ArrayList<>();
        JSONArray teamsArray = settings.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            ArrayList<Weapon> weapons = new ArrayList<>();
            weapons.add(new Bazooka(settings.getInt("weapon1")));
            weapons.add(new Grenade(settings.getInt("weapon2")));
            weapons.add(new Shotgun(settings.getInt("weapon3")));
            teams.add(new Team(terrain.getRandomSpawnPoints(teamsize), weapons, Color.web(teamsArray.getJSONObject(i).getString("color"))));
        }

        initialize();

        if(server != null) server.sendCommand(getStateForNewClient());
    }

    public MapWindow(JSONObject input, Stage stageToClose, Client client, Thread clientThread) {
        this.client = client;
        this.clientThread = clientThread;
        client.registerCurrentNetworkable(this);

        // TODO implement fromJson (code duplication) -> bring the two json formats into line (weapons are team properties)
        this.terrain = new Terrain(TerrainManager.loadFromString(input.getString("terrain")));

        teams = new ArrayList<>();
        JSONArray teamsArray = input.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            teams.add(new Team(teamsArray.getJSONObject(i)));
        }

        turnCount = input.getInt("turnCount");
        currentTeam = input.getInt("currentTeam");

        this.stageToClose = stageToClose;
        initialize();
    }

    public MapWindow(JSONObject input, Stage stageToClose, String file, Client client, Thread clientThread, Server server, Thread serverThread) {
        this.client = client;
        this.clientThread = clientThread;
        client.registerCurrentNetworkable(this);
        this.server = server;
        this.serverThread = serverThread;
        if(server != null) server.registerCurrentNetworkable(this);

        this.terrain = new Terrain(TerrainManager.loadFromString(input.getString("terrain")));

        this.stageToClose = stageToClose;

        JSONObject settings = Settings.getSavedSettings(file);
        teams = new ArrayList<>();
        JSONArray teamsArray = input.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            teams.add(new Team(teamsArray.getJSONObject(i)));
        }

        turnCount = input.getInt("turnCount");
        currentTeam = input.getInt("currentTeam");

        initialize();

        if(server != null) server.sendCommand(getStateForNewClient());
    }

    /**
     * creates the stage, so that everything is visible
     */
    private void initialize() {
        primaryStage = new Stage();
        primaryStage.setOnCloseRequest(event -> {
            shutdown();

            stageToClose.show();
            primaryStage.close();
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
        teamLabel = new Label("Team" + currentTeam + "s turn. What will " + teams.get(currentTeam).getCurrentFigure().getName() + " do?");
        root.setBottom(teamLabel);

        drawing = new Scene(root, 1600, 300);
        drawing.setOnKeyPressed(
                keyEvent -> {
                    System.out.println("key pressed: " + keyEvent.getCode());
                    switch(keyEvent.getCode()) {
                        case C:
                            System.out.println("toggle chat");
                            chat.setVisible(!chat.isVisible());
                            break;
                        default:
                            client.sendKeyEvent(keyEvent.getCode());
                    }
                }
        );

        chat = new Chat(client);
        chat.setMaxWidth(300);
        chat.setUnobtrusive(true);
        centerView.getChildren().add(chat);
        chat.setVisible(false);

        primaryStage.setTitle("The Playground");
        primaryStage.setScene(drawing);
        primaryStage.show();

        if(server != null) { // only the server should do calculations
            moveObjectsThread = new Thread(() -> { // TODO move this code to own class
                try {
                    long before = System.currentTimeMillis(), now, sleep;
                    while (true) {
                        if (flyingProjectile != null) {
                            try {
                                final Point2D newPos;
                                newPos = terrain.getPositionForDirection(flyingProjectile.getPosition(), flyingProjectile.getVelocity(), flyingProjectile.getHitRegion(), false, false, false, false);
                                Platform.runLater(() -> flyingProjectile.setPosition(new Point2D(newPos.getX(), newPos.getY())));
                                server.sendCommand("PROJECTILE_SET_POSITION " + newPos.getX() + " " + newPos.getY());
                          /*  } catch (CollisionWithTerrainException e) {
                                System.out.println("CollisionWithTerrainException, let's destroy something!"); // TODO
                                server.sendCommand("REMOVE_FLYING_PROJECTILE"); // TODO potential race condition (might still be !=null in next iteration)
                                //endTurn();*/
                            } catch (CollisionException e) {
                                System.out.println("CollisionWithFigureException, let's harm somebody!");
                                Platform.runLater(() -> {
                                    ArrayList<String> commandList = flyingProjectile.handleCollision(terrain, teams, e.getCollidingPosition());
                                    for(String command : commandList){
                                        server.sendCommand(command);
                                    }
                                    //server.sendCommand("REMOVE_FLYING_PROJECTILE"); // TODO potential race condition
                                    endTurn();
                                });
                            }
                        }
                        now = System.currentTimeMillis();
                        sleep = Math.max(0, (1000 / 10) - (now - before)); // 10 fps
                        Thread.sleep(sleep);
                        before = System.currentTimeMillis();
                    }
                } catch (InterruptedException e) {
                    System.out.println("moveObjectsThread shut down");
                }
            });
            moveObjectsThread.start();
        }

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
        output.put("currentTeam", currentTeam);
        output.put("terrain", TerrainManager.toString(terrain.toArrayList()));
        return output;
    }

    /**
     * @param figure a figure object reference
     * @return team index + " " + figure index of the given figure
     */
    private String getFigureId(Figure figure) {  //ToDo Probably not necessary anymore due to the movement of collisionhandling to the weaponclasses
        String id = "";
        for(int i=0; i<teams.size(); i++) {
            for(int j=0; j<teams.get(i).getFigures().size(); j++) {
                if(teams.get(i).getFigures().get(j) == figure) {
                    id = i+" "+j;
                }
            }
        }
        return id;
    }

    @Deprecated
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
                weapons.add(new Bazooka(2));
                weapons.add(new Grenade(2));
                Team team = new Team(terrain.getRandomSpawnPoints(teamsize), weapons, Color.WHITE);
                teams.add(team);
                centerView.getChildren().add(team);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void endTurn() {
        //ToDo Wait until no objectmovements
        turnCount++; // TODO timing issue
        server.sendCommand("SET_TURN_COUNT " + turnCount);


        shootingIsAllowed = true;
        server.sendCommand("DEACTIVATE_FIGURE " + currentTeam);

        int oldCurrentTeam = currentTeam;
        do {
            currentTeam++;
            if (currentTeam == teams.size()) {
                currentTeam = 0;
            }
            if (currentTeam == oldCurrentTeam) {
                server.sendCommand("GAME_OVER " + currentTeam);
                return;
            }
        } while (teams.get(currentTeam).getNumberOfLivingFigures() == 0);

        server.sendCommand("SET_CURRENT_TEAM " + currentTeam);
        server.sendCommand("CURRENT_TEAM_END_ROUND " + currentTeam);
        server.sendCommand("ACTIVATE_FIGURE " + currentTeam);

        String teamLabelText = "Turn: " + turnCount + " It’s Team " + currentTeam + "’s turn! What will " + teams.get(currentTeam).getCurrentFigure().getName() + " do?";
        server.sendCommand("TEAM_LABEL_SET_TEXT " + teamLabelText);
        System.out.println(teamLabelText);
    }

    /**
     * stops all map window threads and saves game state
     */
    private void shutdown() {
        if(moveObjectsThread != null) moveObjectsThread.interrupt();

        GameState.save(this.toJson());
        System.out.println("MapWindow: saved game state");

        clientThread.interrupt();
        if(serverThread != null) serverThread.interrupt();
        System.out.println("MapWindow threads interrupted");
        client.stop();
        if(server != null) server.stop();
        System.out.println("MapWindow client/server (if any) stopped");
    }

    @Override
    public void start(Stage ostage) {
    }

    @Override
    public void handleOnClient(String command) {
        if(command.contains("CHAT ")) {
            chat.processChatCommand(command);
            return;
        }

        String[] cmd = command.split(" ");

        switch(cmd[0]) {
            case "ACTIVATE_FIGURE":
                teams.get(Integer.parseInt(cmd[1])).getCurrentFigure().setActive(true);
                break;
            case "CURRENT_TEAM_END_ROUND":
                teams.get(Integer.parseInt(cmd[1])).endRound();
                teams.get(Integer.parseInt(cmd[1])).getCurrentFigure().setActive(true);
                break;
            case "CURRENT_FIGURE_ANGLE_DOWN":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().angleDown(teams.get(currentTeam).getCurrentFigure().getFacingRight());
                }
                break;
            case "CURRENT_FIGURE_ANGLE_UP":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().angleUp(teams.get(currentTeam).getCurrentFigure().getFacingRight());
                }
                break;
            case "CURRENT_FIGURE_CHOOSE_WEAPON_1":
                if (shootingIsAllowed) {
                    if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                        centerView.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                        centerView.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                    }
                    teams.get(currentTeam).getCurrentFigure().setSelectedItem(teams.get(currentTeam).getWeapon(0));
                    centerView.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                    centerView.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                }
                break;
            case "CURRENT_FIGURE_CHOOSE_WEAPON_2":
                if (shootingIsAllowed) {
                    if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                        centerView.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                        centerView.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                    }

                    teams.get(currentTeam).getCurrentFigure().setSelectedItem(teams.get(currentTeam).getWeapon(1));
                    centerView.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                    centerView.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                }
                break;
            case "CURRENT_FIGURE_CHOOSE_WEAPON_3":
                if(shootingIsAllowed){
                    if (teams.get(currentTeam).getNumberOfWeapons() >= 3) {
                        if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                            centerView.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                            centerView.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                        }
                        teams.get(currentTeam).getCurrentFigure().setSelectedItem(teams.get(currentTeam).getWeapon(2));
                        centerView.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                        centerView.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                    }
                }
                break;
            case "CURRENT_FIGURE_FACE_LEFT":
                teams.get(currentTeam).getCurrentFigure().setFacingRight(false);
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().angleDraw(teams.get(currentTeam).getCurrentFigure().getFacingRight());
                }
                break;
            case "CURRENT_FIGURE_FACE_RIGHT":
                teams.get(currentTeam).getCurrentFigure().setFacingRight(true);
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().angleDraw(teams.get(currentTeam).getCurrentFigure().getFacingRight());
                }
                break;
            case "CURRENT_FIGURE_SET_POSITION":
                Figure f = teams.get(currentTeam).getCurrentFigure();
                f.setPosition(new Point2D(Double.parseDouble(cmd[1]) / 8, Double.parseDouble(cmd[2]) / 8));
                break;
            case "CURRENT_FIGURE_SHOOT":
                try {
                     /* ToDo
                    power = power + 5;
                    Sleep/Wait if more ShootCommands are incoming, count them by incrementing THEN create projecctile
                     */
                    Projectile projectile = teams.get(currentTeam).getCurrentFigure().shoot(power);
                    flyingProjectile = projectile;
                    centerView.getChildren().add(flyingProjectile);
                    shootingIsAllowed = false;
                    //ToDo setRoundTimer down to 5sec

                } catch (NoMunitionException e) {
                    System.out.println("no munition");
                    break;
                }
                centerView.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                centerView.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                teams.get(currentTeam).getCurrentFigure().setSelectedItem(null);
                break;
            case "DEACTIVATE_FIGURE":
                teams.get(Integer.parseInt(cmd[1])).getCurrentFigure().setActive(false);
                break;
            case "GAME_OVER":
                primaryStage.close();
                if(moveObjectsThread != null) moveObjectsThread.interrupt();
                GameOverWindow gameOverWindow = new GameOverWindow();
                gameOverWindow.showWinner(Integer.parseInt(cmd[1]), stageToClose, map, "SETTINGS_FILE.conf", client, clientThread, server, serverThread);
                break;
            case "PROJECTILE_SET_POSITION": // TODO though server did null check, recheck here (problem when connecting later)
                flyingProjectile.setPosition(new Point2D(Double.parseDouble(cmd[1]), Double.parseDouble(cmd[2])));
                break;
            case "REMOVE_FLYING_PROJECTILE":
                centerView.getChildren().remove(flyingProjectile);
                flyingProjectile = null;
                break;
            case "SET_CURRENT_TEAM":
                teams.get(currentTeam).getCurrentFigure().setActive(false);
                currentTeam = Integer.parseInt(cmd[1]);
                break;
            case "SET_HP":
                teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).setHealth(Integer.parseInt(cmd[3]));
            case "SET_TURN_COUNT":
                turnCount = Integer.parseInt(cmd[1]);
                break;
            case "SUDDEN_DEATH":
                teams.get(Integer.parseInt(cmd[1])).suddenDeath();
            case "TEAM_LABEL_SET_TEXT":
                teamLabel.setText(arrayToString(cmd, 1));
                break;
            default:
                System.out.println("handleKeyEventOnClient: no event for key " + command);
        }
    }

    @Override
    public void handleKeyEventOnServer(String keyCode) {
        if (keyCode.startsWith("/kickteam ")) {
            try {
                int teamNumber = Integer.parseInt(extractPart(keyCode, "/kickteam "))-1;
                if(teamNumber >= teams.size()) throw new IndexOutOfBoundsException();
                server.sendCommand("SUDDEN_DEATH " + teamNumber);
                if(currentTeam == teamNumber) {
                    endTurn();
                }
            } catch(NumberFormatException | IndexOutOfBoundsException e) {
                    System.out.println("malformed command " + keyCode);
            }
        }

        Point2D v = null;

        switch(keyCode) {
            case "Space":
                if(teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    server.sendCommand("CURRENT_FIGURE_SHOOT");
                }
                break;
            // these codes always result in optical changes only, so nothing to do on server side
            case "Up":
            case "W":
                server.sendCommand("CURRENT_FIGURE_ANGLE_UP");
                break;
            case "Down":
            case "S":
                server.sendCommand("CURRENT_FIGURE_ANGLE_DOWN");
                break;
            case "Left":
            case "A":
                server.sendCommand("CURRENT_FIGURE_FACE_LEFT");
                if(teams.get(currentTeam).getCurrentFigure().getSelectedItem() == null) {
                    moveCurrentlyActiveFigure(new Point2D(-FIGURE_SPEED, 0));
                }
               break;
            case "Right":
            case "D":
                server.sendCommand("CURRENT_FIGURE_FACE_RIGHT");
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() == null) {
                    moveCurrentlyActiveFigure(new Point2D(FIGURE_SPEED, 0));
                }
                break;
            case "1":
                if(teams.get(currentTeam).getNumberOfWeapons() >= 1) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON_1");
                }
                break;
            case "2":
                if(teams.get(currentTeam).getNumberOfWeapons() >= 2) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON_2");
                }
                break;
            case "3":
                if(teams.get(currentTeam).getNumberOfWeapons() >= 3) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON_3");
                }
                break;
            default:
                System.out.println("handleKeyEventOnServer: no event for key " + keyCode);
        }
    }

    /**
     * moves the currently active figure and reports the position change to the connected clients
     * @param v the velocity vector with which the figure wants to move
     */
    private void moveCurrentlyActiveFigure(Point2D v) {
        Figure f = teams.get(currentTeam).getCurrentFigure();
        Point2D pos = new Point2D(f.getPosition().getX() * 8, f.getPosition().getY() * 8);
        Rectangle2D hitRegion = f.getHitRegion();
        Point2D newPos = null;
        try {
            newPos = terrain.getPositionForDirection(pos, v, hitRegion, true, true, true, true);
        } catch (CollisionException e) {
            System.out.println("CollisionWithTerrainException, stopped movement");
            newPos = e.getLastGoodPosition();
        }/* catch (CollisionWithFigureException e) {
            // figures can walk through each other // TODO really? // Yes, please
            System.out.println("ERROR How did we get here?");
        }*/
        server.sendCommand("CURRENT_FIGURE_SET_POSITION " + newPos.getX() + " " + newPos.getY());
    }

    @Override
    public String getStateForNewClient() {
        return "STATUS MAPWINDOW " + this.toJson().toString();
    }

}
