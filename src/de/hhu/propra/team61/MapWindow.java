package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.Chat;
import de.hhu.propra.team61.gui.GameOverWindow;
import de.hhu.propra.team61.gui.SceneController;
import de.hhu.propra.team61.gui.WindIndicator;
import de.hhu.propra.team61.io.GameState;
import de.hhu.propra.team61.io.ItemManager;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.io.Settings;
import de.hhu.propra.team61.io.TerrainManager;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Networkable;
import de.hhu.propra.team61.network.Server;
import de.hhu.propra.team61.objects.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private final static int DAMAGE_BY_POISON = 10;
    private final static int FPS = 10;
    /** vertical speed change of a object with weight 1 caused by gravity in 1s (in our physics, the speed change by gravity is proportional to object mass) */
    public final static Point2D GRAVITY = new Point2D(0, .01);
    private final static int DIGITATION_MIN_HEALTH = 65;
    private final static int DEDIGITATION_HEALTH_THRESHOLD = 25;
    private final static int DIGITATION_MIN_CAUSED_DAMAGE = 30;

    //JavaFX related variables
    private Scene drawing;
    /** contains terrain, labels at the top etc. (ie. everything) */
    private BorderPane rootPane;
    /** contains chat, scrollPane with terrain, teams etc. */
    private StackPane centerPane;
    /** contains terrain, teams, weapons */
    private StackPane fieldPane;
    /** contains fieldPane */
    private ScrollPane scrollPane;
    private Timeline scrollPaneTimeline = null;
    private final static int SCROLL_ANIMATION_DURATION = 1000;
    private final static int SCROLL_ANIMATION_DELAY = 500;
    private Terrain terrain;
    private WindIndicator windIndicator = new WindIndicator();
    private Label teamLabel;
    //Team related variables
    /** dynamic list containing all playing teams (also contains teams which do not have any living figures) */
    private ArrayList<Team> teams;
    private int currentTeam = 0;
    private int turnCount = 0;
    private int levelCounter = 0;
    private int teamquantity;
    private int teamsize;

    private ArrayList<Crate> supplyDrops;

    /** power/energy projectile is shot with */
    private int power = 0;
    /** used to disable shooting multiple times during one turn */
    private boolean shootingIsAllowed = true;
    private boolean pause = false;
    //Projectile-Moving-Thread related variables
    private ArrayList<Projectile> flyingProjectiles = new ArrayList<>();
    private Thread moveObjectsThread;
    //Network
    private Server server;
    private Client client;
    private Thread serverThread;
    private Thread clientThread;

    private String map; // TODO do we need this?
    private Chat chat;
    private SceneController sceneController;

    private int k;
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

        //ToDo: Prepare start inventory of all teams
        for(int i=0; i<teamsArray.length(); i++) {
            ArrayList<Item> inventory = ItemManager.generateInventory(settings.getJSONArray("inventory")); //ToDo add startInventory to settings
            teams.add(new Team(terrain.getRandomSpawnPoints(teamsize), inventory, Color.web(teamsArray.getJSONObject(i).getString("color")), teamsArray.getJSONObject(i).getString("name"), teamsArray.getJSONObject(i).getString("figure"), teamsArray.getJSONObject(i).getJSONArray("figure-names")));
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
        this.terrain = new Terrain(input.getJSONObject("terrain"));

        teams = new ArrayList<>();
        JSONArray teamsArray = input.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            teams.add(new Team(teamsArray.getJSONObject(i)));
        }

        turnCount = input.getInt("turnCount");
        currentTeam = input.getInt("currentTeam");
        terrain.setWind(input.getInt("windForce"));
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

        this.terrain = new Terrain(input.getJSONObject("terrain"));

        JSONObject settings = Settings.getSavedSettings(file);
        teams = new ArrayList<>();
        JSONArray teamsArray = input.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            teams.add(new Team(teamsArray.getJSONObject(i)));
        }

        turnCount = input.getInt("turnCount");
        currentTeam = input.getInt("currentTeam");
        terrain.setWind(input.getInt("windForce"));

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
        scrollPane.setPrefSize(1000, 530);
        centerPane.getChildren().add(scrollPane);
        rootPane.setBottom(centerPane);

        supplyDrops = new ArrayList<>();

        for(Team team: teams) {
            fieldPane.getChildren().add(team);
            terrain.addFigures(team.getFigures());
        }
        teamLabel = new Label("Team " + teams.get(currentTeam).getName() + "'s turn. What will " + teams.get(currentTeam).getCurrentFigure().getName() + " do?");
        teams.get(currentTeam).getCurrentFigure().setActive(true);
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

        if(server != null) terrain.rewind();
        windIndicator.setWindForce(terrain.getWindMagnitude());
        rootPane.setCenter(windIndicator);

        if(server != null) { // only the server should do calculations
            moveObjectsThread = new Thread(() -> { // TODO move this code to own class
                try {
                    long before = System.currentTimeMillis(), now, sleep;
                    while (true) {
                        for(k = 0; k < flyingProjectiles.size(); k++) {
                            Projectile flyingProjectile = flyingProjectiles.get(k);
                            try {
                                final Point2D newPos;
                                newPos = terrain.getPositionForDirection(flyingProjectile.getPosition(), flyingProjectile.getVelocity(), flyingProjectile.getHitRegion(), false, false, false, flyingProjectile.getDrifts());
                                flyingProjectile.addVelocity(GRAVITY.multiply(flyingProjectile.getMass()));
                                flyingProjectile.setPosition(new Point2D(newPos.getX(), newPos.getY()));
                                scrollTo(newPos.getX(), newPos.getY(), 0, 0, false);
                                server.sendCommand("PROJECTILE_SET_POSITION " + k + " " + newPos.getX() + " " + newPos.getY());
                            } catch (CollisionException e) {
                                System.out.println("CollisionException, let's do this!");
                                final Projectile collidingProjectile = flyingProjectile;
                                flyingProjectile = null; // we remove it here to prevent timing issues
                                Platform.runLater(() -> {
//                                    } catch (DeathException de) { // TODO that change was somewhat important I think (or not?) ... (see handleCollision in Weapon)
//                                        if(de.getFigure() == teams.get(currentTeam).getCurrentFigure()) {
//                                            endTurn();
//                                        }
//                                    }
                                    //Get series of commands to send to the clients from
                                    //Collisionhandling done by the weapon causing this exception
                                    ArrayList<String> commandList = collidingProjectile.handleCollision(terrain, teams, e.getCollidingPosition());
                                    fieldPane.getChildren().remove(collidingProjectile);
                                    if(commandList.contains("REMOVE_FLYING_PROJECTILE")){ commandList.set(0,"REMOVE_FLYING_PROJECTILE "+k); }
                                    for(String command : commandList){ server.sendCommand(command); } //Send commands+
                                });
                            }
                        }

                        for(int i = 0; i < supplyDrops.size(); i++){
                            Crate supply = supplyDrops.get(i);
                            supply.resetVelocity();
                            final Point2D oldPos = new Point2D(supply.getPosition().getX(), supply.getPosition().getY());
                            try {
                                final Point2D newPos; // TODO code duplication
                                newPos = terrain.getPositionForDirection(oldPos, supply.getVelocity(), supply.getHitRegion(), false, true, false, false); //ToDo change last false to true? I am doing that later
                                if (!oldPos.equals(newPos)) { // do not send a message when position is unchanged
                                    supply.setPosition(new Point2D(newPos.getX(), newPos.getY())); // needed to prevent timing issue when calculating new position before client is handled on server
                                    server.sendCommand("SUPPLY_SET_POSITION " + i + " " + (newPos.getX()) + " " + (newPos.getY()));
                                }
                            } catch (CollisionException e) {
                                if (!e.getLastGoodPosition().equals(oldPos)) {
                                    supply.setPosition(new Point2D(e.getLastGoodPosition().getX(), e.getLastGoodPosition().getY()));
                                    server.sendCommand("SUPPLY_SET_POSITION " + i + " " + (e.getLastGoodPosition().getX()) + " " + (e.getLastGoodPosition().getY()));
                                }
                                supply.nullifyVelocity();
                            }
                        }

                        for(int t = 0; t < teams.size(); t++) {
                            Team team = teams.get(t);
                            for(int f = 0; f < team.getFigures().size(); f++) {
                                Figure figure = team.getFigures().get(f);
                                if(figure.getHealth() > 0) {
                                    //Check if figure collides with an Crate TODO: Ask if I should move that somewhere else
                                    for(int i = 0; i < supplyDrops.size(); i++){
                                        if(figure.getHitRegion().intersects(supplyDrops.get(i).getHitRegion())){
                                            server.sendCommand("SUPPLY_PICKED_UP" + " " + t + " " + supplyDrops.get(i).getContent());
                                            server.sendCommand("REMOVE_SUPPLY " + i);
                                        }
                                    }

                                    final boolean scrollToFigure = (figure == teams.get(currentTeam).getCurrentFigure());
                                    final Point2D oldPos = new Point2D(figure.getPosition().getX(), figure.getPosition().getY());
                                    try {
                                        final Point2D newPos; // TODO code duplication
                                        figure.addVelocity(GRAVITY.multiply(figure.getMass()));
                                        newPos = terrain.getPositionForDirection(oldPos, figure.getVelocity(), figure.getHitRegion(), false, true, false, true);
                                        if (!oldPos.equals(newPos)) { // do not send a message when position is unchanged
                                            figure.setPosition(new Point2D(newPos.getX(), newPos.getY())); // needed to prevent timing issue when calculating new position before client is handled on server
                                            server.sendCommand("FIGURE_SET_POSITION " + getFigureId(figure) + " " + (newPos.getX()) + " " + (newPos.getY()) + " " + scrollToFigure);
                                        }
                                    } catch (CollisionException e) {
                                        if (!e.getLastGoodPosition().equals(oldPos)) {
                                            System.out.println("CollisionWithTerrainException");
                                            figure.setPosition(new Point2D(e.getLastGoodPosition().getX(), e.getLastGoodPosition().getY()));
                                            server.sendCommand("FIGURE_SET_POSITION " + getFigureId(figure) + " " + (e.getLastGoodPosition().getX()) + " " + (e.getLastGoodPosition().getY()) + " " + scrollToFigure);
                                        }
                                        int oldHp = figure.getHealth();
                                        try {
                                            figure.resetVelocity();
                                        } catch (DeathException de) {
                                            if (de.getFigure() == teams.get(currentTeam).getCurrentFigure()) {
                                                server.sendCommand("END_TURN");
                                            }
                                        }
                                        if (figure.getHealth() != oldHp) { // only send hp update when hp has been changed
                                            server.sendCommand("SET_HP " + getFigureId(figure) + " " + figure.getHealth());
                                        }
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
        output.put("terrain", terrain.toJson());
        output.put("windForce", terrain.getWindMagnitude());
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
        if(turnCount == -42) { // cheat mode
            shootingIsAllowed = true;
            return;
        }

        //ToDo Wait until no objectmovements
        while(flyingProjectiles.size() > 0){} //ToDo makeover

        teams.get(currentTeam).getCurrentFigure().addCausedHpDamage(collectRecentlyCausedDamage());

        turnCount++; // TODO timing issue
        server.sendCommand("SET_TURN_COUNT " + turnCount);

        server.sendCommand("DEACTIVATE_FIGURE " + currentTeam);

        terrain.rewind();
        server.sendCommand("WIND_FORCE " + terrain.getWindMagnitude());

        // Let all living poisoned Figures suffer DAMAGE_BY_POISON damage;
        if(turnCount % teams.size() == 0) { //if(Round finished) //Round := all living Teams made a turn
            for (Team t : teams) {
                for (Figure f : t.getFigures()) {
                    if(f.getHealth() > 0) { //Avoid reviving the poisoned dead
                        if (f.getIsPoisoned()) { f.setHealth(Math.max(1, f.getHealth() - DAMAGE_BY_POISON)); }
                    }
                }
            }
        }

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

        if(turnCount == teams.size() * teams.get(0).getFigures().size() * 2) {
            doDigitations();
        } else if(turnCount > teams.size() * teams.get(0).getFigures().size() * 2) {
            undoDigitations();
        }

        // TODO give this an true probability, set by Customize? //Remove own supplyDrops Variable?
        if(true){
            Crate drop = new Crate(terrain.toArrayList().get(0).size()-1);
            server.sendCommand("DROP_SUPPLY"+" "+drop.getPosition().getX()+" "+drop.getContent());
        }
        
        server.sendCommand("SET_CURRENT_TEAM " + currentTeam);
        teams.get(currentTeam).endRound();
        server.sendCommand("CURRENT_TEAM_END_ROUND " + currentTeam);
        server.sendCommand("ACTIVATE_FIGURE " + currentTeam);

        String teamLabelText = "Turn " + turnCount + ": It’s Team " + teams.get(currentTeam).getName() + "’s turn! What will " + teams.get(currentTeam).getCurrentFigure().getName() + " do?";
        server.sendCommand("TEAM_LABEL_SET_TEXT " + teamLabelText);
        System.out.println(teamLabelText);
    }

    private int collectRecentlyCausedDamage() {
        int recentlyCausedDamage = 0;
        for(Team team: teams) {
            for(Figure figure: team.getFigures()) {
                recentlyCausedDamage += figure.popRecentlySufferedDamage();
            }
        }
        return recentlyCausedDamage;
    }

    private void doDigitations() {
        for(Team team: teams) {
            for(Figure figure: team.getFigures()) {
                if(figure.getHealth() >= DIGITATION_MIN_HEALTH && figure.getCausedHpDamage() >= DIGITATION_MIN_CAUSED_DAMAGE) {
                    figure.digitate();
                    server.sendCommand("DIGITATE " + getFigureId(figure));
                }
            }
        }
    }

    private void undoDigitations() {
        for(Team team: teams) {
            for(Figure figure: team.getFigures()) {
                if(figure.getHealth() < DEDIGITATION_HEALTH_THRESHOLD) {
                    figure.dedigitate();
                    server.sendCommand("DEDIGITATE " + getFigureId(figure));
                }
            }
        }
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

    /**
     * changes the scroll pane scroll position so that the given object is visible in the center
     * @param x the x coordinate of the object
     * @param y the y coordinate of the object
     * @param width the width of the object
     * @param height the height if the object
     */
    private void scrollTo(double x, double y, double width, double height, boolean animate) {
        final double paneWidth = scrollPane.getWidth();
        final double paneHeight = scrollPane.getHeight();

        final double contentWidth = terrain.getWidth();
        final double contentHeight = terrain.getHeight();

        final double newHvalue = (x - paneWidth / 2 + width / 2) / (contentWidth - paneWidth);
        final double newVvalue = (y - paneHeight / 2 + height / 2) / (contentHeight - paneHeight);

        if(scrollPaneTimeline != null) scrollPaneTimeline.stop(); // stop animation when scrolling to new position

        Platform.runLater(() -> {
            if (animate) {
                scrollPaneTimeline = new Timeline();

                final KeyValue kvH = new KeyValue(scrollPane.hvalueProperty(), newHvalue);
                final KeyFrame kfH = new KeyFrame(Duration.millis(SCROLL_ANIMATION_DURATION), kvH);

                final KeyValue kvV = new KeyValue(scrollPane.vvalueProperty(), newVvalue);
                final KeyFrame kfV = new KeyFrame(Duration.millis(SCROLL_ANIMATION_DURATION), kvV);

                scrollPaneTimeline.getKeyFrames().addAll(kfH, kfV);
                scrollPaneTimeline.setDelay(new Duration(SCROLL_ANIMATION_DELAY));
                scrollPaneTimeline.play();
            } else {
                scrollPane.setHvalue(newHvalue);
                scrollPane.setVvalue(newVvalue);
            }
        });
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
            case "ACTIVATE_FIGURE":
                teams.get(Integer.parseInt(cmd[1])).getCurrentFigure().setActive(true);
                Point2D activePos = teams.get(Integer.parseInt(cmd[1])).getCurrentFigure().getPosition();
                scrollTo(activePos.getX(), activePos.getY(), Figure.NORMED_OBJECT_SIZE, Figure.NORMED_OBJECT_SIZE, true);
                break;
            case "END_TURN":
                endTurn();
                break;
            case "CURRENT_TEAM_END_ROUND":
                if(server == null) { // already done on server
                    teams.get(Integer.parseInt(cmd[1])).endRound();
                }
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
            case "CURRENT_FIGURE_CHOOSE_WEAPON":
                if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                }
                teams.get(currentTeam).getCurrentFigure().setSelectedItem(teams.get(currentTeam).getItem(Integer.parseInt(cmd[1]) - 1));
                fieldPane.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
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
            case "CURRENT_FIGURE_SHOOT":
                try {
                    Projectile projectile = teams.get(currentTeam).getCurrentFigure().shoot();
                    if(projectile != null){ //Only weapons need projectiles
                        flyingProjectiles.add(projectile);
                        fieldPane.getChildren().add(projectile);
                        shootingIsAllowed = false;
                    } else { //Treatment for "true" Items
                        fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                    }
                } catch (NoMunitionException e) {
                    System.out.println("no munition");
                    break;
                }
                fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                teams.get(currentTeam).getCurrentFigure().setSelectedItem(null);
                break;
            case "DEDIGITATE":
                if(server == null) {
                    teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).dedigitate();
                }
                break;
            case "DIGITATE":
                if(server == null) {
                    teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).digitate();
                }
                break;
            case "FIGURE_SET_POSITION":
                Point2D position = new Point2D(Double.parseDouble(cmd[3]), Double.parseDouble(cmd[4]));
                if(server == null) { // server already applied change to prevent timing issue
                    Figure f = teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2]));
                    f.setPosition(position); // TODO alternative setter
                }
                if(cmd.length > 5 && Boolean.parseBoolean(cmd[5])) { // do not scroll when moving an inactive figure
                    scrollTo(position.getX(), position.getY(), Figure.NORMED_OBJECT_SIZE, Figure.NORMED_OBJECT_SIZE, false);
                }
                break;
            case "FIGURE_ADD_VELOCITY":
                Point2D vector = new Point2D(Double.parseDouble(cmd[3]), Double.parseDouble(cmd[4]));
                Figure f = teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2]));
                f.addVelocity(vector);
                break;
            case "REPLACE_BLOCK":
                if(cmd[3].charAt(0) == '#'){cmd[3] = " ";} //Decode # as destruction, ' ' is impossible due to Client/Server architecture
                terrain.replaceBlock(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), cmd[3].charAt(0));
                break;
            case "DEACTIVATE_FIGURE":
                shootingIsAllowed = true;
                teams.get(Integer.parseInt(cmd[1])).getCurrentFigure().setActive(false);
                break;

            case "DROP_SUPPLY":
                supplyDrops.add(new Crate(terrain.toArrayList().get(0).size(),cmd[2]));
                fieldPane.getChildren().add(supplyDrops.get(supplyDrops.size()-1));
                fieldPane.getChildren().add(supplyDrops.get(supplyDrops.size()-1).getContentDisplay());
                break;
            case "REMOVE_SUPPLY":
                fieldPane.getChildren().remove(supplyDrops.get(Integer.parseInt(cmd[1])));
                supplyDrops.remove(supplyDrops.get(Integer.parseInt(cmd[1])));
                break;
            case "SUPPLY_SET_POSITION":
                position = new Point2D(Double.parseDouble(cmd[2]), Double.parseDouble(cmd[3]));
                supplyDrops.get(Integer.parseInt(cmd[1])).setPosition(position);
                break;
            case "SUPPLY_PICKED_UP":
                teams.get(Integer.parseInt(cmd[1])).getItem(cmd[2]).refill();
                break;
            case "GAME_OVER":
                if (moveObjectsThread != null) moveObjectsThread.interrupt();
                GameOverWindow gameOverWindow = new GameOverWindow();
                gameOverWindow.showWinner(sceneController, Integer.parseInt(cmd[1]), teams.get(Integer.parseInt(cmd[1])).getName(), map, "SETTINGS_FILE.conf", client, clientThread, server, serverThread);
                break;
            case "PROJECTILE_SET_POSITION": // TODO though server did null check, recheck here (problem when connecting later)
                if(server==null) { // TODO code duplication should be avoided
                    final double x = Double.parseDouble(cmd[2]);
                    final double y = Double.parseDouble(cmd[3]);
                    flyingProjectiles.get(Integer.parseInt(cmd[1])).setPosition(new Point2D(x, y));
                    scrollTo(x, y, 0, 0, false);
                }
                break;
            case "ADD_FLYING_PROJECTILE":
          //      flyingProjectiles.add(projectile);
          //      fieldPane.getChildren().add(projectile);
                break;
            case "REMOVE_FLYING_PROJECTILE":
                if(flyingProjectiles.size() >= Integer.parseInt(cmd[1])) {
                    fieldPane.getChildren().remove(flyingProjectiles.get(Integer.parseInt(cmd[1])-1));
                    flyingProjectiles.remove(Integer.parseInt(cmd[1])-1);
                }
                break;
            case "SET_CURRENT_TEAM":
                teams.get(currentTeam).getCurrentFigure().setActive(false);
                currentTeam = Integer.parseInt(cmd[1]);
                break;
            case "SET_HP":
                teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).setHealth(Integer.parseInt(cmd[3]));
                break;
            case "CONDITION":
                switch(cmd[1]){
                    case "POISON":
                        teams.get(Integer.parseInt(cmd[2])).getFigures().get(Integer.parseInt(cmd[3])).setIsPoisoned(Boolean.parseBoolean(cmd[4]));
                        break;
                    case "PARALYZE":
                        teams.get(Integer.parseInt(cmd[2])).getFigures().get(Integer.parseInt(cmd[3])).setIsParalyzed(Boolean.parseBoolean(cmd[4]));
                        break;
                    case "STUCK":
                        teams.get(Integer.parseInt(cmd[2])).getFigures().get(Integer.parseInt(cmd[3])).setIsStuck(Boolean.parseBoolean(cmd[4]));
                        break;
                }
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
            case "WIND_FORCE":
                windIndicator.setWindForce(Double.parseDouble(cmd[1]));
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
            executeCheat(extractPart(keyCode, "CHEAT ").split(" "));
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
                server.sendCommand("CURRENT_FIGURE_FACE_LEFT");
                moveCurrentlyActiveFigure(new Point2D(-Figure.WALK_SPEED, 0));
                break;
            case "Right":
            case "D":
                server.sendCommand("CURRENT_FIGURE_FACE_RIGHT");
                moveCurrentlyActiveFigure(new Point2D(Figure.WALK_SPEED, 0));
                break;
            case "1":
                if(shootingIsAllowed) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON 1");
                }
                break;
            case "2":
                if(shootingIsAllowed) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON 2");
                }
                break;
            case "3":
                if(shootingIsAllowed) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON 3");
                }
                break;
            case "4":
                if(shootingIsAllowed) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON 4");
                }
                break;
            case "5":
                if(shootingIsAllowed) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON 5");
                }
                break;
            case "6":
                if(shootingIsAllowed) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON 6");
                }
                break;
            case "7":
                if(shootingIsAllowed) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON 7");
                }
                break;
            case "8":
                if(shootingIsAllowed) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON 8");
                }
                break;
            case "9":
                if(shootingIsAllowed) {
                    server.sendCommand("CURRENT_FIGURE_CHOOSE_WEAPON 9");
                }
                break;
            default:
                System.out.println("handleKeyEventOnServer: no event for key " + keyCode);
        }
    }

    private void executeCheat(String[] cmd) {
        switch (cmd[0]) {
            case "1fig": // kills every figure except the first figure of the first team and prevents game over window from being shown
                for (int i = 1; i < teams.size(); i++) {
                    teams.get(i).suddenDeath();
                }
                for (int i = 1; i < teams.get(0).getFigures().size(); i++) {
                    teams.get(0).getFigures().get(i).setHealth(0);
                }
                turnCount = -42; // prevents endTurn() from showing game over window
                System.out.println("You are now alone.");
                break;
            case "1up": // 100 live for first figure of first team
                teams.get(0).getFigures().get(0).setHealth(100);
                System.out.println("Ate my spinach.");
                break;
            case "dedigitate": // calls undoDigitations() method
                undoDigitations();
                System.out.println("Returning to Baby I");
                break;
            case "digitate": // calls doDigitations() method
                doDigitations();
                System.out.println("Digitation.");
                break;
            case "forcedig": // forces digitation of all figures
                for(Team team: teams) {
                    for (Figure figure : team.getFigures()) {
                        figure.digitate();
                    }
                }
                System.out.println("Mass-Digitation.");
                break;
            case "rewind": // sets wind to given value
                terrain.setWind(Double.parseDouble(cmd[1]));
                windIndicator.setWindForce(terrain.getWindMagnitude());
                System.out.println("It’s windy.");
            default:
                client.sendChatMessage("««« Haw-haw! This user failed to cheat … »»» " + arrayToString(cmd, 0));
                System.out.println("No cheating, please!");
        }
    }

    /**
     * moves the currently active figure and reports the position change to the connected clients
     * @param v the velocity vector with which the figure wants to move
     */
    private void moveCurrentlyActiveFigure(Point2D v) {
        Figure f = teams.get(currentTeam).getCurrentFigure();
        Point2D pos = new Point2D(f.getPosition().getX(), f.getPosition().getY());
        Rectangle2D hitRegion = f.getHitRegion();
        Point2D newPos = null;
        try {
            newPos = terrain.getPositionForDirection(pos, v, hitRegion, true, true, true, true);
        } catch (CollisionException e) {
            System.out.println("CollisionException, stopped movement");
            newPos = e.getLastGoodPosition();
        }
        f.setPosition(new Point2D(newPos.getX(), newPos.getY())); // needed to prevent timing issue when calculating new position before client is handled on server
        server.sendCommand("FIGURE_SET_POSITION " + getFigureId(f) + " " + newPos.getX() + " " + newPos.getY() + " true");
    }

    public ImageView drawBackgroundImage() {
        String img = "file:resources/board.png";
        Image image = new Image(img);
        ImageView background = new ImageView();
        background.setImage(image);
        return background;
    }
    
    @Override
    public String getStateForNewClient() {
        return "STATUS MAPWINDOW " + this.toJson().toString();
    }

}
