package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.Chat;
import de.hhu.propra.team61.gui.GameOverWindow;
import de.hhu.propra.team61.io.GameState;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.io.Settings;
import de.hhu.propra.team61.io.TerrainManager;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Networkable;
import de.hhu.propra.team61.network.Server;
import de.hhu.propra.team61.objects.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import static de.hhu.propra.team61.JavaFxUtils.arrayToString;
import static de.hhu.propra.team61.JavaFxUtils.extractPart;

/**
 * Created by kegny on 08.05.14.
 * Edited by DiniiAntares on 15.05.14
 * This class is supposed to draw the Array given by "TerrainManager" rendering the Map visible.
 */
public class MapWindow extends Application implements Networkable {
    private ArrayList<Team> teams;
    private Scene drawing;
    /** contains terrain, labels at the top etc. (ie. everything) */
    private BorderPane rootPane;
    /** contains chat, scrollPane with terrain, teams etc. */
    private StackPane centerPane;
    /** contains terrain, teams, weapons */
    private StackPane fieldPane;
    /** contains fieldPane */
    private ScrollPane scrollPane;
    private Terrain terrain;
    private Label teamLabel;
    private int currentTeam = 0;
    private int turnCount = 0;
    private int levelCounter = 0;
    private Projectile flyingProjectile = null;
    private Thread moveObjectsThread;
    private int teamquantity;
    private int teamsize;
    private Server server;
    private Client client;
    private Thread serverThread;
    private Thread clientThread;
    private String map; // TODO do we need this?
    private Chat chat;
    private boolean pause = false;
    private SceneController sceneController;

    public final static Point2D GRAVITY = new Point2D(0,.01);
    private final static int FPS = 10;

    public MapWindow(String map, String file, Client client, Thread clientThread, Server server, Thread serverThread, SceneController sceneController) {
        this.sceneController = sceneController;
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

        JSONObject settings = Settings.getSavedSettings(file);
        this.teamquantity = settings.getInt("numberOfTeams");
        this.teamsize = Integer.parseInt(settings.getString("team-size"));
        teams = new ArrayList<>();
        JSONArray teamsArray = settings.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            ArrayList<Weapon> weapons = new ArrayList<>();
            weapons.add(new Gun("file:resources/weapons/temp1.png", 50, settings.getInt("weapon1")));
            weapons.add(new Grenade("file:resources/weapons/temp2.png", 40, settings.getInt("weapon2")));
            weapons.add(new Gun("file:resources/weapons/temp3.png", 30, settings.getInt("weapon3")));
            teams.add(new Team(terrain.getRandomSpawnPoints(teamsize), weapons, Color.web(teamsArray.getJSONObject(i).getString("color"))));
        }

        initialize();

        if(server != null) server.sendCommand(getStateForNewClient());
    }

    public MapWindow(JSONObject input, Client client, Thread clientThread, SceneController sceneController) {
        this.sceneController = sceneController;
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

        initialize();
    }

    public MapWindow(JSONObject input, String file, Client client, Thread clientThread, Server server, Thread serverThread, SceneController sceneController) {
        this.sceneController = sceneController;
        this.client = client;
        this.clientThread = clientThread;
        client.registerCurrentNetworkable(this);
        this.server = server;
        this.serverThread = serverThread;
        if(server != null) server.registerCurrentNetworkable(this);

        this.terrain = new Terrain(TerrainManager.loadFromString(input.getString("terrain")));

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
        sceneController.getStage().setOnCloseRequest(event -> {
            shutdown();
            sceneController.switchToMenue();
        });
        rootPane = new BorderPane();
        // contains the terrain with figures
        scrollPane = new ScrollPane();
        centerPane = new StackPane();
        centerPane.setAlignment(Pos.TOP_LEFT);
        fieldPane = new StackPane();
        fieldPane.setAlignment(Pos.TOP_LEFT);
        fieldPane.getChildren().add(terrain);

        // anchor the map to the bottom left corner (ScrollPane cannot do that)
        final AnchorPane anchorPane = new AnchorPane();
        AnchorPane.setBottomAnchor(fieldPane, 0.0);
        AnchorPane.setLeftAnchor(fieldPane, 0.0);
        anchorPane.getChildren().add(fieldPane);

        scrollPane.setId("scrollPane");
        scrollPane.viewportBoundsProperty().addListener((observableValue, oldBounds, newBounds) ->
                        anchorPane.setPrefSize(Math.max(fieldPane.getBoundsInParent().getMaxX(), newBounds.getWidth()), Math.max(fieldPane.getBoundsInParent().getMaxY(), newBounds.getHeight()))
        );
        scrollPane.setContent(anchorPane);
        scrollPane.setPrefSize(1000, 550);
        centerPane.getChildren().add(scrollPane);
        rootPane.setBottom(centerPane);

        for(Team team: teams) {
            fieldPane.getChildren().add(team);
            terrain.addFigures(team.getFigures());
        }
        teamLabel = new Label("Team" + currentTeam + "s turn. What will " + teams.get(currentTeam).getCurrentFigure().getName() + " do?");
        rootPane.setTop(teamLabel);

        drawing = new Scene(rootPane, 1600, 300);
        drawing.getStylesheets().add("file:resources/layout/css/mapwindow.css");
        drawing.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            System.out.println("key pressed: " + keyEvent.getCode());
            if (!chat.isVisible()) { // do not consume keyEvent when chat is active
                switch (keyEvent.getCode()) {
                    case C:
                        System.out.println("toggle chat");
                        chat.setVisible(!chat.isVisible());
                        break;
                    default:
                        client.sendKeyEvent(keyEvent.getCode());
                }
                // we do not want the scrollPane to receive a key event
                keyEvent.consume();
            }
        });

        chat = new Chat(client);
        chat.setMaxWidth(300);
        chat.setUnobtrusive(true);
        centerPane.getChildren().add(chat);
        chat.setVisible(false);

        sceneController.setGameScene(drawing);
        sceneController.switchToMapwindow();

        if(server != null) { // only the server should do calculations
            moveObjectsThread = new Thread(() -> { // TODO move this code to own class
                try {
                    long before = System.currentTimeMillis(), now, sleep;
                    while (true) {
                        if (flyingProjectile != null) {
                            try {
                                final Point2D newPos;
                                newPos = terrain.getPositionForDirection(flyingProjectile.getPosition(), flyingProjectile.getVelocity(), flyingProjectile.getHitRegion(), false, false, false);
                                flyingProjectile.addVelocity(GRAVITY.multiply(flyingProjectile.getMass()));
                                Platform.runLater(() -> flyingProjectile.setPosition(new Point2D(newPos.getX(), newPos.getY())));
                                server.sendCommand("PROJECTILE_SET_POSITION " + newPos.getX() + " " + newPos.getY());
                            } catch (CollisionWithTerrainException e) {
                                System.out.println("CollisionWithTerrainException, let's destroy something!"); // TODO
                                server.sendCommand("REMOVE_FLYING_PROJECTILE"); // TODO potential race condition (might still be !=null in next iteration)
                                endTurn();
                            } catch (CollisionWithFigureException e) {
                                System.out.println("CollisionWithFigureException, let's harm somebody!");
                                Platform.runLater(() -> {
                                    try {
                                        e.getCollisionPartner().sufferDamage(flyingProjectile.getDamage());
                                    } catch (DeathException de) {
                                        if(de.getFigure() == teams.get(currentTeam).getCurrentFigure()) {
                                            endTurn();
                                        }
                                    }
                                    server.sendCommand("SET_HP " + getFigureId(e.getCollisionPartner()) + " " + e.getCollisionPartner().getHealth());
                                    server.sendCommand("REMOVE_FLYING_PROJECTILE"); // TODO potential race condition
                                    endTurn();
                                });
                            }
                        }
                        for(Team team: teams) {
                            for(Figure figure: team.getFigures()) {
                                if(figure.getHealth() > 0) {
                                    final Point2D oldPos = new Point2D(figure.getPosition().getX() * 8, figure.getPosition().getY() * 8);
                                    try {
                                        final Point2D newPos; // TODO code duplication
                                        figure.addVelocity(GRAVITY.multiply(figure.getMass()));
                                        newPos = terrain.getPositionForDirection(oldPos, figure.getVelocity(), figure.getHitRegion(), false, true, false);
                                        if (!oldPos.equals(newPos)) { // do not send a message when position is unchanged
                                            figure.setPosition(new Point2D(newPos.getX() / 8 , newPos.getY() / 8)); // needed to prevent timing issue when calculating new position before client is handled on server
                                            server.sendCommand("FIGURE_SET_POSITION " + getFigureId(figure) + " " + (newPos.getX()) + " " + (newPos.getY()));
                                        }
                                    } catch (CollisionWithTerrainException e) {
                                        if (!e.getLastGoodPosition().equals(oldPos)) {
                                            System.out.println("CollisionWithTerrainException");
                                            figure.setPosition(new Point2D(e.getLastGoodPosition().getX() / 8 , e.getLastGoodPosition().getY() / 8));
                                            server.sendCommand("FIGURE_SET_POSITION " + getFigureId(figure) + " " + (e.getLastGoodPosition().getX()) + " " + (e.getLastGoodPosition().getY()));
                                        }
                                        int oldHp = figure.getHealth();
                                        try {
                                            figure.resetVelocity();
                                        } catch (DeathException de) {
                                            if (de.getFigure() == teams.get(currentTeam).getCurrentFigure()) {
                                                endTurn();
                                            }
                                        }
                                        if (figure.getHealth() != oldHp) { // only send hp update when hp has been changed
                                            server.sendCommand("SET_HP " + getFigureId(figure) + " " + figure.getHealth());
                                        }
                                    } catch (CollisionWithFigureException e) {
                                        System.out.println("WARNING: CollisionWithFigureException should not happen here");
                                    }
                                }
                            }
                        }

                        // sleep thread, and assure constant frame rate
                        now = System.currentTimeMillis();
                        sleep = Math.max(0, (1000 / FPS) - (now - before));
                        Thread.sleep(sleep);
                        before = System.currentTimeMillis();
                    }
                } catch (InterruptedException e) {
                    System.out.println("moveObjectsThread shut down");
                }
            });
            moveObjectsThread.start();
        }
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
    private String getFigureId(Figure figure) {
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
                fieldPane.getChildren().remove(team);
            }
            teams.clear();
            for(int i=0; i<teamquantity; i++) { // TODO hard coded 2 teams, 2 figures
                ArrayList<Weapon> weapons = new ArrayList<>();
                weapons.add(new Gun("file:resources/weapons/temp1.png", 50, 2));
                weapons.add(new Grenade("file:resources/weapons/temp2.png", 40, 2));
                Team team = new Team(terrain.getRandomSpawnPoints(teamsize), weapons, Color.WHITE);
                teams.add(team);
                fieldPane.getChildren().add(team);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getNumberOfLivingTeams() {
        int livingTeams = 0;
        for (Team team : teams) {
            if (team.getNumberOfLivingFigures() > 0){
                livingTeams++;
            }
        }
        return livingTeams;
    }

    public void endTurn() {
        if(turnCount == -42) return; // cheat mode

        turnCount++; // TODO timing issue
        server.sendCommand("SET_TURN_COUNT " + turnCount);

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

        if (getNumberOfLivingTeams() == 0){
            server.sendCommand("GAME_OVER " + -1);
            return;
        }
        if (getNumberOfLivingTeams() < 2){
            server.sendCommand("GAME_OVER " + currentTeam);
            return;
        }

        server.sendCommand("SET_CURRENT_TEAM " + currentTeam);
        server.sendCommand("CURRENT_TEAM_END_ROUND");

        String teamLabelText = "Turn: " + turnCount + " It’s Team " + (currentTeam+1) + "’s turn! What will " + teams.get(currentTeam).getCurrentFigure().getName() + " do?";
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

        if(cmd[0].equals("PAUSE")){
            pause = Boolean.parseBoolean(cmd[1]);
            if(pause) {
                teamLabel.setText("Pause - If(Host){Press P or ESC to continue}"); //ToDo ugly temporary implementation
            } else {
                teamLabel.setText("Continue");
            }
        }

        switch (cmd[0]) {
            case "CURRENT_TEAM_END_ROUND":
                teams.get(currentTeam).endRound();
                break;
            case "CURRENT_FIGURE_ANGLE_DOWN":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().angleDown(teams.get(currentTeam).getCurrentFigure().getFacing_right());
                }
                break;
            case "CURRENT_FIGURE_ANGLE_UP":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().angleUp(teams.get(currentTeam).getCurrentFigure().getFacing_right());
                }
                break;
            case "CURRENT_FIGURE_CHOOSE_WEAPON_1":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                    fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                }
                teams.get(currentTeam).getCurrentFigure().setSelectedItem(teams.get(currentTeam).getWeapon(0));
                fieldPane.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                fieldPane.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                break;
            case "CURRENT_FIGURE_CHOOSE_WEAPON_2":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                    fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                }
                teams.get(currentTeam).getCurrentFigure().setSelectedItem(teams.get(currentTeam).getWeapon(1));
                fieldPane.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                fieldPane.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                break;
            case "CURRENT_FIGURE_CHOOSE_WEAPON_3":
                if (teams.get(currentTeam).getNumberOfWeapons() >= 3) {
                    if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                        fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                        fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                    }
                    teams.get(currentTeam).getCurrentFigure().setSelectedItem(teams.get(currentTeam).getWeapon(2));
                    fieldPane.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                    fieldPane.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                }
                break;
            case "CURRENT_FIGURE_FACE_LEFT":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    teams.get(currentTeam).getCurrentFigure().setFacing_right(false);
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().angleDraw(teams.get(currentTeam).getCurrentFigure().getFacing_right());
                }
                break;
            case "CURRENT_FIGURE_FACE_RIGHT":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    teams.get(currentTeam).getCurrentFigure().setFacing_right(true);
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().angleDraw(teams.get(currentTeam).getCurrentFigure().getFacing_right());
                }
                break;
//            case "Number Sign": // TODO really? this is broken and deprecated
//                cheatMode();
//                break;
            case "CURRENT_FIGURE_SHOOT":
                try {
                    Projectile projectile = teams.get(currentTeam).getCurrentFigure().shoot();
                    flyingProjectile = projectile;
                    fieldPane.getChildren().add(flyingProjectile);
                } catch (NoMunitionException e) {
                    System.out.println("no munition");
                    break;
                }
                fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem().getCrosshair());
                fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                teams.get(currentTeam).getCurrentFigure().setSelectedItem(null);
                break;
            case "FIGURE_SET_POSITION":
                Point2D position = new Point2D(Double.parseDouble(cmd[3]) / 8, Double.parseDouble(cmd[4]) / 8);
                if(server == null) { // server already applied change to prevent timing issue
                    Figure f = teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2]));
                    f.setPosition(position); // TODO alternative setter
                }
                scrollPane.setHvalue(position.getX()*8 / terrain.getWidth());
                scrollPane.setVvalue(position.getY()*8 / terrain.getHeight());
                break;
            case "GAME_OVER":
                if (moveObjectsThread != null) moveObjectsThread.interrupt();
                GameOverWindow gameOverWindow = new GameOverWindow();
                gameOverWindow.showWinner(sceneController, Integer.parseInt(cmd[1]), map, "SETTINGS_FILE.conf", client, clientThread, server, serverThread);
                break;
            case "PROJECTILE_SET_POSITION": // TODO though server did null check, recheck here (problem when connecting later)
                flyingProjectile.setPosition(new Point2D(Double.parseDouble(cmd[1]), Double.parseDouble(cmd[2])));
                break;
            case "REMOVE_FLYING_PROJECTILE":
                fieldPane.getChildren().remove(flyingProjectile);
                flyingProjectile = null;
                break;
            case "SET_CURRENT_TEAM":
                currentTeam = Integer.parseInt(cmd[1]);
                break;
            case "SET_HP":
                teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).setHealth(Integer.parseInt(cmd[3]));
                break;
            case "SET_TURN_COUNT":
                turnCount = Integer.parseInt(cmd[1]);
                break;
            case "SUDDEN_DEATH":
                teams.get(Integer.parseInt(cmd[1])).suddenDeath();
                break;
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
            return;
        } else if(keyCode.startsWith("CHEAT ")) {
            executeCheat(extractPart(keyCode, "CHEAT "));
            return;
        }

        Point2D v = null;

        int team = -1;
        try {
            team = Integer.parseInt(keyCode.split(" ", 2)[0]);
        } catch(NumberFormatException e) {
            System.out.println("handleKeyEventOnServer: NumberFormatException" + e.getMessage());
            return;
        }
        keyCode = keyCode.split(" ", 2)[1];

        // pause is a special case: do not ignore pause command when paused, and also accept the input when it's not team 0's turn
        switch(keyCode) {
            case "Esc":
            case "Pause":
            case "P":
                if (team == 0 || client.isLocalGame()) { // allowing pausing by host (team 0) and when playing local game
                    pause = !pause;
                    server.sendCommand("PAUSE " + pause);
                }
                break;
        }

        if(pause) {
            System.out.println("Game paused, ignoring command " + keyCode);
            return;
        }

        if (team != currentTeam && !client.isLocalGame()) {
            System.out.println("The key event " + keyCode + " of team " + team + " has been discarded. Operation not allowed, currentTeam is " + currentTeam);
            return;
        }

        switch(keyCode) {
            case "Space":
                if(teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    server.sendCommand("CURRENT_FIGURE_SHOOT");
                }
                break;
            // these codes always result in optical changes only, so nothing to do on server side
            case "Up":
            case "W":
                if(teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    server.sendCommand("CURRENT_FIGURE_ANGLE_UP");
                } else {
                    teams.get(currentTeam).getCurrentFigure().jump();
                }
                break;
            case "Down":
            case "S":
                server.sendCommand("CURRENT_FIGURE_ANGLE_DOWN");
                break;
            case "Left":
            case "A":
                if(teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                   server.sendCommand("CURRENT_FIGURE_FACE_LEFT");
                   break;
                } else {
                    v = new Point2D(-Figure.WALK_SPEED, 0);
                }
            case "Right":
            case "D":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    server.sendCommand("CURRENT_FIGURE_FACE_RIGHT");
                } else {
                    if (v == null) v = new Point2D(Figure.WALK_SPEED, 0);
                    Figure f = teams.get(currentTeam).getCurrentFigure();
                    Point2D pos = new Point2D(f.getPosition().getX() * 8, f.getPosition().getY() * 8);
                    Rectangle2D hitRegion = f.getHitRegion();
                    Point2D newPos = null;
                    try {
                        newPos = terrain.getPositionForDirection(pos, v, hitRegion, true, true, true);
                    } catch (CollisionWithTerrainException e) {
                        System.out.println("CollisionWithTerrainException, stopped movement");
                        newPos = e.getLastGoodPosition();
                    } catch (CollisionWithFigureException e) {
                        // figures can walk through each other // TODO really?
                        System.out.println("ERROR How did we get here?");
                    }
                    f.setPosition(new Point2D(newPos.getX() / 8, newPos.getY() / 8)); // needed to prevent timing issue when calculating new position before client is handled on server
                    server.sendCommand("FIGURE_SET_POSITION " + getFigureId(f) + " " + newPos.getX() + " " + newPos.getY());
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

    private void executeCheat(String cmd) {
        switch(cmd) {
            case "1fig": // kills every figure except the first figure of the first team and prevents game over window from being shown
                for(int i=1; i<teams.size(); i++) {
                    teams.get(i).suddenDeath();
                }
                for(int i=1; i<teams.get(0).getFigures().size(); i++) {
                    teams.get(0).getFigures().get(i).setHealth(0);
                }
                turnCount = -42; // prevents endTurn() from showing game over window
                System.out.println("You are now alone.");
                break;
            case "1up": // 100 live for first figure of first team
                teams.get(0).getFigures().get(0).setHealth(100);
                System.out.println("Ate my spinach.");
                break;
            default:
                System.out.println("No cheating, please!");
        }
    }

    public Pane drawBackgroundImage() {
        Pane backgroundPane = new Pane();
        String img = "file:resources/levelback1.png";
        Image image = new Image(img);
        ImageView background = new ImageView();
        background.setImage(image);
        backgroundPane.getChildren().add(background);
        return backgroundPane;
    }

    @Override
    public String getStateForNewClient() {
        return "STATUS MAPWINDOW " + this.toJson().toString();
    }

}
