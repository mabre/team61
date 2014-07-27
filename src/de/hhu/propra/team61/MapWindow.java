package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.*;
import de.hhu.propra.team61.io.GameState;
import de.hhu.propra.team61.io.Settings;
import de.hhu.propra.team61.io.TerrainManager;
import de.hhu.propra.team61.io.VorbisPlayer;
import de.hhu.propra.team61.io.ItemManager;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.network.Client;
import de.hhu.propra.team61.network.Networkable;
import de.hhu.propra.team61.network.Server;
import de.hhu.propra.team61.objects.*;
import de.hhu.propra.team61.objects.itemtypes.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import java.io.FileNotFoundException;


import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static de.hhu.propra.team61.JavaFxUtils.arrayToString;
import static de.hhu.propra.team61.JavaFxUtils.extractPart;

/**
 * An instance of this class is a window displaying a level with figures, wind, game comments etc.
 */
public class MapWindow extends Application implements Networkable {
    private final static int DAMAGE_BY_POISON = 10;
    /** Chance of a crate spawning (Compared to {@link Math#random()}); TODO give this an true probability, set by Customize */
    private final static double SUPPLY_DROP_PROBABILITY = 0.25;
    private final static int FPS = 10;
    /** vertical speed change of a object with weight 1 caused by gravity in 1s (in our physics, the speed change by gravity is proportional to object mass) */
    public final static Point2D GRAVITY = new Point2D(0, .01);
    private final static int DIGITATION_MIN_HEALTH = 65;
    private final static int DEGITATION_HEALTH_THRESHOLD = 25;
    private final static int DIGITATION_MIN_CAUSED_DAMAGE = 30;
    public static final int HIGH_DAMAGE_THRESHOLD = 50;
    public static final int RAMPAGE_THRESHOLD = 75;
    public static final double NO_HIT_COMMENT_PROBABILITY = .5;
    /** number of turns until sudden death is started, as set by the user */
    private final int TURNS_TILL_SUDDEN_DEATH = Settings.getSavedInt("turnsTillSuddenDeath", 30);
    /** number of turns the boss needs to destroy the whole map */
    private final static int SUDDEN_DEATH_TURNS = 20;
    private final static int MILLISECONDS_PER_TURN = Settings.getSavedInt("secondsPerTurn", 30)*1000; // TODO IMPORTANT doc
    private static final int MILLISECONDS_BETWEEN_TURNS = 1000;
    /** names the boss can have (chosen randomly) */
    private final static String[] BOSS_NAMES = {"Marʔoz", "ʔock’mar", "Ånsgar", "Apfel"}; // similarity to Vel’Koz, Kog’Maw, a town in Norway, and an evil fruit is purely coincidental

    //JavaFX related variables
    private Scene drawing;
    /** contains gamePane and pause/help-overlay */
    private StackPane rootPane = new StackPane();
    /** contains pause/help-overlay */
    private BorderPane pausePane = new BorderPane();
    /** contains terrain, labels at the top etc. (ie. everything visible after initialization) */
    private BorderPane gamePane;
    /** contains teamLabel, windIndicator */
    private BorderPane topLine = new BorderPane();
    /** contains chat, scrollPane with terrain, teams etc. */
    private StackPane centerPane;
    /** contains terrain, teams, weapons */
    private StackPane fieldPane;
    /** contains fieldPane */
    private ScrollPane scrollPane;
    private Timeline scrollPaneTimeline = null;
    private final static int SCROLL_ANIMATION_DURATION = 1000;
    private final static int SCROLL_ANIMATION_DELAY = 500;
    private boolean autoScroll = true;
    private boolean projectileFocused = false;
    private Terrain terrain;
    private WindIndicator windIndicator = new WindIndicator();
    private Label teamLabel;
    private ScrollingLabel ingameLabel = new ScrollingLabel();
    /** the ImageView containing the image that is shown in the overlay */
    private ImageView pauseHelpImageView = new ImageView();
    /** shown in the overlay when the game is paused */
    private final static Image pauseImage = new Image("file:resources/layout/pause.png");
    /** shown in the overlay when the game is not paused */
    private final static Image helpImage = new Image("file:resources/layout/help.png");
    //Team related variables
    /** dynamic list containing all playing teams (also contains teams which do not have any living figures) */
    private ArrayList<Team> teams;
    private int currentTeam = 0;
    /** number of turns played, ie. during first turn, the value is 0 */
    private int turnCount = 0;
    private int teamquantity;
    private int teamsize;
    /** time left for the current turn in ms */
    private final AtomicInteger turnTimer = new AtomicInteger(-MILLISECONDS_BETWEEN_TURNS); // TODO IMPORTANT json
    private Thread turnTimerThread;
    private Label turnTimerLabel = new Label();

    /** dynamic list containing all drops on the screen */
    private ArrayList<Crate> supplyDrops = new ArrayList<>();
    /** power/energy projectile is shot with */
    private int power = 0; //ToDo implement or kick
    /** used to disable shooting multiple times during one turn */
    private boolean shootingIsAllowed = true;
    private boolean pause = false;
    private boolean help = false;
    //Projectile-Moving-Thread related variables
    private ArrayList<Projectile> flyingProjectiles = new ArrayList<>();
    private Thread moveObjectsThread;
    //Network
    private Server server;
    private Client client;
    private Thread serverThread;
    private Thread clientThread;

    private Figure boss = null;
    private boolean bossSpawnedLeft;

    private int floodLevel = -1;

    private String map; // TODO do we need this?
    private Chat chat;
    private SceneController sceneController;
    private int k;

    /**
     * Creates a new map window.
     * @param map the filename of the level to be loaded
     * @param file file containing the settings for the game to be started
     * @param client a client object
     * @param clientThread the thread executing the client
     * @param server a server object, might be null
     * @param serverThread the thread executing the server, might be null
     * @param sceneController
     */
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

        JSONObject settings = Settings.getSavedJson(file);
        this.teamquantity = settings.getInt("numberOfTeams");
        this.teamsize = settings.getInt("teamSize");
        teams = new ArrayList<>();
        JSONArray teamsArray = settings.getJSONArray("teams");
        JSONArray cratesArray = settings.getJSONArray("crates");

        if(teamsArray.length() * teamsize > terrain.getNumberOfAvailableSpawnPoints()) {
            System.err.println("ERROR: Not enough spawn points: " + teamsArray.length() + " teams with " + teamsize + " figures requested, but only " + terrain.getNumberOfAvailableSpawnPoints() + " spawn points");
            Dialogs.create()
                    .owner(sceneController.getStage())
                    .masthead("Not enough starting points available.")
                    .message("Try adding spawn points using the level editor.")
                    .style(DialogStyle.UNDECORATED)
                    .lightweight()
                    .showInformation();
            shutdown();
            sceneController.switchToMenu();
            return;
        }

        //ToDo: Prepare start inventory of all teams
        for(int i=0; i<teamsArray.length(); i++) {
            ArrayList<Item> inventory = ItemManager.generateInventory(settings.getJSONArray("inventory")); //ToDo add startInventory to settings
            teams.add(new Team(terrain.getRandomSpawnPoints(teamsize), inventory, Color.web(teamsArray.getJSONObject(i).getString("color")), teamsArray.getJSONObject(i).getString("name"), teamsArray.getJSONObject(i).getString("figure"), teamsArray.getJSONObject(i).getJSONArray("figure-names")));
        }

        for(int i=0; i<cratesArray.length(); i++) {
            supplyDrops.add(new Crate(cratesArray.getJSONObject(i)));
        }

        initialize();

        if(server != null) server.send(getStateForNewClient());
    }

    /**
     * Creates a new map window.
     * @param input a json object representing the state of the map window
     * @param client a client object
     * @param clientThread the thread executing the client
     * @param sceneController
     * @see #toJson()
     */
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

        supplyDrops = new ArrayList<>();
        JSONArray cratesArray = input.getJSONArray("crates");
        for(int i=0; i<cratesArray.length(); i++) {
            supplyDrops.add(new Crate(cratesArray.getJSONObject(i)));
        }

        turnCount = input.getInt("turnCount");
        turnTimer.set(input.getInt("turnCount", MILLISECONDS_BETWEEN_TURNS));
        currentTeam = input.getInt("currentTeam");
        terrain.setWind(input.getDouble("windForce"));
        initialize();
    }

    /**
     * Creates a new map window.
     * @param input a json object representing the state of the map window
     * @param client a client object
     * @param clientThread the thread executing the client
     * @param server a server object, might be null
     * @param serverThread the thread executing the server, might be null
     * @param sceneController
     * @see #toJson()
     */
    public MapWindow(JSONObject input, String file, Client client, Thread clientThread, Server server, Thread serverThread, SceneController sceneController) {
        this.sceneController = sceneController;
        this.client = client;
        this.clientThread = clientThread;
        client.registerCurrentNetworkable(this);
        this.server = server;
        this.serverThread = serverThread;
        if(server != null) server.registerCurrentNetworkable(this);

        this.terrain = new Terrain(input.getJSONObject("terrain"));

        JSONObject settings = Settings.getSavedJson(file); //ToDo Obsolete or roundtimer etc in there?

        teams = new ArrayList<>();
        JSONArray teamsArray = input.getJSONArray("teams");
        for(int i=0; i<teamsArray.length(); i++) {
            teams.add(new Team(teamsArray.getJSONObject(i)));
        }

        supplyDrops = new ArrayList<>();
        JSONArray cratesArray = input.getJSONArray("crates");
        for(int i=0; i<cratesArray.length(); i++) {
            supplyDrops.add(new Crate(cratesArray.getJSONObject(i)));
        }

        turnCount = input.getInt("turnCount");
        turnTimer.set(input.getInt("turnCount", MILLISECONDS_BETWEEN_TURNS));
        currentTeam = input.getInt("currentTeam");
        terrain.setWind(input.getDouble("windForce"));

        initialize();

        if(server != null) server.send(getStateForNewClient());
    }

    /**
     * creates the stage, so that everything is visible
     */
    private void initialize() {
        Rifle.setTerrainHeight(terrain.getTerrainHeight());
        Rifle.setTerrainWidth(terrain.getTerrainWidth());

        sceneController.getStage().setOnCloseRequest(event -> {
            shutdown();
            sceneController.switchToMenu();
        });
        gamePane = new BorderPane();
        // contains the terrain with figures
        scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
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
        scrollPane.viewportBoundsProperty().addListener((observableValue, oldBounds, newBounds) -> {
            // this condition (partially) fixes the wired behaviour of the scroll pane when using the rifle
            // though there is a workaround is Rifle.angleDraw, in some rare cases (which?) the problem still exists,
            // eg. in High.lvl, when standing on the green triangle after the material resistance test area and aiming around
            if((newBounds.getMinX() >= 0 && newBounds.getMinY() >= 0 && newBounds.getMaxY() < terrain.getTerrainHeight() + 10) ||
                    oldBounds.getHeight() == 0) {
                anchorPane.setPrefSize(Math.max(fieldPane.getBoundsInParent().getMaxX(), newBounds.getWidth()), Math.max(fieldPane.getBoundsInParent().getMaxY(), newBounds.getHeight()));
            }
        });
        scrollPane.setContent(anchorPane);
        scrollPane.setPrefSize(1000, 530);
        centerPane.getChildren().add(scrollPane);
        gamePane.setBottom(centerPane);

        for(Team team: teams) {
            fieldPane.getChildren().add(team);
            terrain.addFigures(team.getFigures());
        }
        for(Crate c: supplyDrops) {
            fieldPane.getChildren().add(c);
        }
        teamLabel = new Label("Turn " + (turnCount + 1) + ": It’s Team " + teams.get(currentTeam).getName() + "’s turn! What will " + teams.get(currentTeam).getCurrentFigure().getName() + " do?");
        teams.get(currentTeam).getCurrentFigure().setActive(true);
        topLine.setLeft(teamLabel);
        gamePane.setTop(topLine);
        gamePane.setCenter(ingameLabel);
        createPausePane();
        rootPane.getChildren().addAll(drawBackgroundImage(), gamePane, pausePane);
        drawing = new Scene(rootPane, 1600, 300);
        drawing.getStylesheets().add("file:resources/layout/css/mapwindow.css");
        drawing.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            System.out.println("key pressed: " + keyEvent.getCode());
            if (!chat.isVisible()) { // do not consume keyEvent when chat is active
                switch (keyEvent.getCode()) {
                    case C:
                        System.out.println("toggle chat");
                        chat.setVisible(!chat.isVisible());
                        VorbisPlayer.play("resources/audio/SFX/chatBlop.ogg", false);
                        break;
                    case Z:
                        if(autoScroll) {
                            autoScroll = false;
                        } else {
                            autoScroll = true;
                            if(!projectileFocused) {
                                Point2D figPos = teams.get(currentTeam).getCurrentFigure().getPosition();
                                scrollTo(figPos.getX(), figPos.getY(), Figure.NORMED_OBJECT_SIZE, Figure.NORMED_OBJECT_SIZE, false);
                            }
                        }
                        System.out.println("camera autoscroll: " + autoScroll);
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
        topLine.setRight(windIndicator);

        topLine.setCenter(turnTimerLabel);

        VorbisPlayer.readVolumeSetting();
        VorbisPlayer.play(terrain.getBackgroundMusic(), true);
        if(!terrain.getBackgroundMusicName().isEmpty()) {
            Platform.runLater(() -> // using runLater assures that the comment is not visible before the terrain is visible
                setGameComment("♫ " + terrain.getBackgroundMusicName() + " ♫", false)
            );
        }

        turnTimerThread = new Thread(this::turnTimerFunction);
        turnTimerThread.start();

        if(server != null) { // only the server should do calculations
            moveObjectsThread = new Thread(() -> { // TODO move this code to own class / functionn
                try {
                    long before = System.currentTimeMillis(), now, sleep;
                    while (true) {
                        if (!pause) {
                            for (k = 0; k < flyingProjectiles.size(); k++) {
                                Projectile flyingProjectile = flyingProjectiles.get(k);
                                try {
                                    final Point2D newPos;
                                    newPos = terrain.getPositionForDirection(flyingProjectile.getPosition(), flyingProjectile.getVelocity(), flyingProjectile.getHitRegion(), false, false, false, flyingProjectile.getDrifts());
                                    flyingProjectile.addVelocity(GRAVITY.multiply(flyingProjectile.getMass()));
                                    flyingProjectile.setPosition(new Point2D(newPos.getX(), newPos.getY()));
                                    if (autoScroll && projectileFocused)
                                        scrollTo(newPos.getX(), newPos.getY(), 0, 0, false);
                                    server.send("PROJECTILE_SET_POSITION " + k + " " + newPos.getX() + " " + newPos.getY());
                                } catch (CollisionException e) {
                                    System.out.println("CollisionException, let's do this!");
                                    final Projectile collidingProjectile = flyingProjectile;
                                    flyingProjectiles.remove(flyingProjectile); // we remove it here to prevent timing issue
                                    Platform.runLater(() -> {
                                        fieldPane.getChildren().remove(flyingProjectile);
                                    });
                                    //Get series of commands to send to the clients from
                                    //Collisionhandling done by the weapon causing this exception
                                    ArrayList<String> commandList = collidingProjectile.handleCollision(terrain, teams, e.getCollidingPosition());
                                    if (commandList.contains("REMOVE_FLYING_PROJECTILE")) {
                                        commandList.set(0, "REMOVE_FLYING_PROJECTILE " + k);
                                    }
                                    for (String command : commandList) {
                                        server.send(command);
                                    } //Send commands+
                                    if (!commandList.get(commandList.size()-1).contains("ADD_FLYING_PROJECTILES") && flyingProjectiles.size() == 0) {
                                        endTurn();
                                    }
                                }
                            }

                            for (int i = 0; i < supplyDrops.size(); i++) {
                                Crate supply = supplyDrops.get(i);
                                supply.resetVelocity();
                                final Point2D oldPos = new Point2D(supply.getPosition().getX(), supply.getPosition().getY());
                                try {
                                    final Point2D newPos; // TODO code duplication
                                    newPos = terrain.getPositionForDirection(oldPos, supply.getVelocity(), supply.getHitRegion(), false, true, false, false); //ToDo change last false to true? I am doing that later
                                    if (!oldPos.equals(newPos)) { // do not send a message when position is unchanged
                                        supply.setPosition(new Point2D(newPos.getX(), newPos.getY())); // needed to prevent timing issue when calculating new position before client is handled on server
                                        server.send("SUPPLY_SET_POSITION " + i + " " + (newPos.getX()) + " " + (newPos.getY()));
                                    }
                                } catch (CollisionException e) {
                                    if (!e.getLastGoodPosition().equals(oldPos)) {
                                        supply.setPosition(new Point2D(e.getLastGoodPosition().getX(), e.getLastGoodPosition().getY()));
                                        server.send("SUPPLY_SET_POSITION " + i + " " + (e.getLastGoodPosition().getX()) + " " + (e.getLastGoodPosition().getY()));
                                    }
                                    supply.nullifyVelocity();
                                }
                            }

                            for (int t = 0; t < teams.size(); t++) {
                                Team team = teams.get(t);
                                for (int f = 0; f < team.getFigures().size(); f++) {
                                    Figure figure = team.getFigures().get(f);
                                    if (figure.getHealth() > 0) {
                                        //Check if figure collides with an Crate TODO: Ask if I should move that somewhere else
                                        for (int i = 0; i < supplyDrops.size(); i++) {
                                            if (figure.getHitRegion().intersects(supplyDrops.get(i).getHitRegion())) {
                                                server.send("SUPPLY_PICKED_UP" + " " + t + " " + supplyDrops.get(i).getContent());
                                                server.send("REMOVE_SUPPLY " + i);
                                            }
                                        }

                                        final boolean scrollToFigure = (figure == teams.get(currentTeam).getCurrentFigure());
                                        final Point2D oldPos = new Point2D(figure.getPosition().getX(), figure.getPosition().getY());
                                        try {
                                            final Point2D newPos; // TODO code duplication
                                            figure.addVelocity(GRAVITY.multiply(figure.getMass()));
                                            newPos = terrain.getPositionForDirection(oldPos, figure.getVelocity(), figure.getHitRegion(), false, true, false, true);
                                            if (oldPos.distance(newPos) > .01) { // do not send a message when position is unchanged
                                                figure.setPosition(new Point2D(newPos.getX(), newPos.getY())); // needed to prevent timing issue when calculating new position before client is handled on server
                                                server.send("FIGURE_SET_POSITION " + t + " " + f + " " + (newPos.getX()) + " " + (newPos.getY()) + " " + scrollToFigure);
                                            }
                                        } catch (CollisionException e) {
                                            if (e.getLastGoodPosition().distance(oldPos) > .01) {
                                                System.out.println("CollisionWithTerrainException");
                                                figure.setPosition(e.getLastGoodPosition());
                                                server.send("FIGURE_SET_POSITION " + t + " " + f + " " + (e.getLastGoodPosition().getX()) + " " + (e.getLastGoodPosition().getY()) + " " + scrollToFigure);
                                            }
                                            int oldHp = figure.getHealth();
                                            int oldShield = figure.getShield();
                                            try {
                                                figure.resetVelocity();
                                                if (terrain.standingOnLiquid(figure.getPosition())) {
                                                    System.out.println(figure.getName() + " standing on liquid");
                                                    figure.sufferDamage(figure.getDamageByLiquid());
                                                }
                                            } catch (DeathException de) {
                                                // if the current figure dies by jumping, switch the team, unless this figures just shot
                                                // (in this case, the projectile collision will trigger endTurn()) (#82)
                                                if (de.getFigure() == teams.get(currentTeam).getCurrentFigure() && flyingProjectiles.size()==0) {
                                                    endTurn();
                                                }
                                            }
                                            if (figure.getHealth() != oldHp) { // only send hp update when hp has been changed
                                                Server.send("SET_HP " + getFigureId(figure) + " " + figure.getHealth());
                                                Server.send("PLAY_SFX fallDamage");
                                            }
                                            if (figure.getShield() != oldShield) {
                                                Server.send("SET_SHIELD " + getFigureId(figure) + " " + figure.getShield());
                                            }
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

    private void turnTimerFunction() {
        final int INTERVAL = 100;
        try {
            long before = System.currentTimeMillis(), now, sleep;
            while (true) {
                if(!pause) {
                    synchronized(turnTimer) {
                        if(turnTimer.get() > 0) {
                            if (turnTimer.addAndGet(-INTERVAL) == 0 && flyingProjectiles.size() == 0) {
                                if(server != null) {
                                    Platform.runLater(() -> {
                                        currentFigureChooseWeapon(9); // TODO hard-coded weapon 9 to Skip @Kegny
                                        handleOnClient("CURRENT_FIGURE_SHOOT"); // TODO IMPORTANT network
                                    });
                                }
                            }
                        } else if(turnTimer.get() < 0) {
                            if(turnTimer.addAndGet(INTERVAL) == 0) {
                                turnTimer.set(MILLISECONDS_PER_TURN); // TODO IMPORTANT investigate case where figure dies by shock wave
                                if(server != null) Server.send("SET_TURN_TIMER " + turnTimer.get());
                            }
                        }
                        updateTurnTimerLabelText();
                    }
                }
                // sleep thread, and assure constant frame rate
                now = System.currentTimeMillis();
                sleep = Math.max(0, (INTERVAL) - (now - before));
                Thread.sleep(sleep);
                before = System.currentTimeMillis();
            }
        } catch (InterruptedException e) {
            System.out.println("turnTimerThread shut down");
        }
    }

    private void updateTurnTimerLabelText() {
        final int value = turnTimer.get();
        Platform.runLater(() -> turnTimerLabel.setText(String.format("%.1f", value/1000.0)));
//        System.out.println(value + "ms");
    }

    /**
     * Creates the overlay for in-game help and when the game is paused. Pane is invisible at all other times.
     */
    private void createPausePane() {
        pausePane.setVisible(false);
        CustomGrid pauseGrid = new CustomGrid();
        pauseGrid.add(pauseHelpImageView, 0, 0, 2, 1);
        pauseGrid.setHalignment(pauseHelpImageView, HPos.CENTER);
        Button cont = new Button("Continue game");
        cont.setOnAction(e -> {
            if (pause) {
                pause = !pause;
                server.send("PAUSE " + pause);
            } else {
                help = !help;
                pausePane.setVisible(help);
            }
        });

        pauseGrid.add(cont, 0, 1);
        pauseGrid.setHalignment(cont, HPos.CENTER);
        Button exit = new Button("Save and end game");
        exit.setOnAction(e -> {
             sceneController.switchToMenu();
             shutdown();
        });
        pauseGrid.add(exit, 1, 1);
        pauseGrid.setHalignment(exit, HPos.CENTER);
        Text shortcuts = new Text("Shortcuts:");
        pauseGrid.add(shortcuts, 0, 4);
        Text shortcutsList = new Text(
                "←/A\t\twalk left\n" +
                "→/D\t\twalk right\n" +
                "↑/W\t\tjump, move crosshair up\n" +
                "↓/S\t\tmove crosshair down\n" +
                //"E - open inventory\n" + TODO
                "1 to 9\tchoose item\n" +
                "0\t\tdeselect item\n" +
                "Space\tshoot\n" +
                "C\t\topen chat\n" +
                "Z\t\tdisable/enable auto-scrolling\n" +
                "P/Esc/F1\tpause game, show this help");
        pauseGrid.add(shortcutsList, 0, 5, 2, 8);
        pausePane.setId("pausePane");
        pausePane.setCenter(pauseGrid);
    }

    private Pane drawBackgroundImage() {
        String image = "file:resources/levels/"+terrain.getBackgroundImage();
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle("-fx-background-image: url('" + image + "')");
        return backgroundPane;
    }

    /**
     * Gets the whole state of the map window as json.
     * @return the whole state of the window as JSONObject
     */
    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        JSONArray teamsArray = new JSONArray();
        for(Team t: teams) {
            teamsArray.put(t.toJson());
        }
        JSONArray cratesArray = new JSONArray();
        for(Crate c : supplyDrops){
            cratesArray.put(c.toJson());
        }
        output.put("teams", teamsArray);
        output.put("crates", cratesArray);
        output.put("turnCount", turnCount);
        output.put("turnTimer", turnTimer.get());
        output.put("currentTeam", currentTeam);
        output.put("terrain", terrain.toJson());
        output.put("windForce", terrain.getWindMagnitude());
        // TODO include sudden death status
        return output;
    }

    /**
     * Gets a unique id of the given figure.
     * The idea is the number of the team, followed by a space, followed by the number of the figure (counting starts
     * from 0).
     * @param figure a reference to the figure whose team and number are sought
     * @return team index + " " + figure index of the given figure
     * @throws java.lang.IllegalArgumentException thrown when the given figure is not found
     */
    private String getFigureId(Figure figure) throws IllegalArgumentException {
        for(int i=0; i<teams.size(); i++) {
            for(int j=0; j<teams.get(i).getFigures().size(); j++) {
                if(teams.get(i).getFigures().get(j) == figure) {
                    return i+" "+j;
                }
            }
        }
        throw new IllegalArgumentException("Could not find figure" + figure);
    }

    /**
     * Gets the number of the team of the given figure.
     * @param figure a reference to the figure whose team is sought
     * @return the index of the team of the figure
     * @throws java.lang.IllegalArgumentException thrown when the given figure is not found
     */
    private int getTeamOfFigure(Figure figure) throws IllegalArgumentException {
        for(int i=0; i<teams.size(); i++) {
            for(int j=0; j<teams.get(i).getFigures().size(); j++) {
                if(teams.get(i).getFigures().get(j) == figure) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("Could not find team of figure" + figure);
    }

    /**
     * Calculates the number of living teams.
     * @return number of teams with living figures
     */
    public int getNumberOfLivingTeams() {
        int livingTeams = 0;
        for (Team team : teams) {
            if (team.getNumberOfLivingFigures() > 0){
                livingTeams++;
            }
        }
        return livingTeams;
    }

    /**
     * increases turnCount, deactivates last active figure (ie. removes highlight), triggers sudden death,
     *   calculates next team, activates next figure, and updates label with current team etc.
     * only to be called on server
     */
    public void endTurn() {
        if(turnCount == -42) { // cheat mode
            shootingIsAllowed = true;
            synchronized(turnTimer) {
                turnTimer.set(MILLISECONDS_PER_TURN);
                if(server != null) Server.send("SET_TURN_TIMER " + turnTimer.get());
                updateTurnTimerLabelText();
            }
            return;
        }

        if(flyingProjectiles.size() > 0) {
            System.err.println("Hey, there are " + flyingProjectiles.size() + " projectiles, we shouldn't be here!");
        }

        synchronized(turnTimer) {
            if (turnTimer.get() < 0) {
                System.err.println("turnTimer " + turnTimer.get() + ", stopping endTurn()");
            }
        }

        int causedDamageInTurn = collectRecentlyCausedDamage();

        if(teams.get(currentTeam).getCurrentFigure().isOnRampage()) {
            teams.get(currentTeam).getCurrentFigure().endRampage(causedDamageInTurn);
            Server.send("END_RAMPAGE " + getFigureId(teams.get(currentTeam).getCurrentFigure()) + " " + causedDamageInTurn);
        }

        if (causedDamageInTurn >= HIGH_DAMAGE_THRESHOLD) {
            server.send("PLAY_SFX highDamage");
            if(causedDamageInTurn >= RAMPAGE_THRESHOLD) {
                teams.get(currentTeam).getCurrentFigure().startRampage();
                Server.send("START_RAMPAGE " + getFigureId(teams.get(currentTeam).getCurrentFigure()));
                server.send("SET_GAME_COMMENT 0 "+teams.get(currentTeam).getCurrentFigure().getName()+" is on a rampage.");
            }
        } else if (causedDamageInTurn == 0 && Math.random() < NO_HIT_COMMENT_PROBABILITY) {
            server.send("SET_GAME_COMMENT 0 " + generateNoHitComment(teams.get(currentTeam).getCurrentFigure().getName())); // TODO class for generating random comments.
        }
        teams.get(currentTeam).getCurrentFigure().addCausedHpDamage(causedDamageInTurn);
        turnCount++; // TODO timing issue
        server.send("SET_TURN_COUNT " + turnCount);

        server.send("DEACTIVATE_FIGURE " + currentTeam);

        terrain.rewind();
        server.send("WIND_FORCE " + terrain.getWindMagnitude());

        if(turnCount >= TURNS_TILL_SUDDEN_DEATH && boss == null && floodLevel == -1) {
            switch ((int)(Math.random()*2)) {
                case 0:
                    spawnBoss();
                    break;
                default:
                    System.out.println("flood warning");
                    server.send("SET_GAME_COMMENT 0 Weather forecast: Flood warning.");
                    floodLevel = 0;
                    break;
            }
        } else if(boss != null) {
            moveBoss();
            server.send("SD BOSS MOVE");
        } else if(floodLevel != -1) {
            server.sendCommands(terrain.increaseFlood(++floodLevel));
        }

        // Let all living poisoned Figures suffer DAMAGE_BY_POISON damage;
        if(turnCount % teams.size() == 0) { //if(Round finished) //Round := all living Teams made a turn (not exactly true here - when a team died, this is wrong, but not that important here)
            for (Team t : teams) {
                for (Figure f : t.getFigures()) {
                    if(f.getHealth() > 0) { //Avoid reviving the poisoned dead
                        if (f.getIsPoisoned()) {
                            f.setHealth(Math.max(1, f.getHealth() - DAMAGE_BY_POISON)); // note that we ignore the shield here
                            server.send("SET_HP " + getFigureId(f) + " " + f.getHealth());
                        }
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
                server.send("GAME_OVER " + currentTeam);
                return;
            }
        } while (teams.get(currentTeam).getNumberOfLivingFigures() == 0);

        if (getNumberOfLivingTeams() == 0){
            server.send("GAME_OVER -1");
            return;
        }
        if (getNumberOfLivingTeams() < 2){
            server.send("GAME_OVER " + currentTeam);
            return;
        }

        if(turnCount == teams.size() * teams.get(0).getFigures().size() * 2) {
            doDigitations();
            server.send("SET_GAME_COMMENT 0 Digitate, my brave hearts!");
            server.send("PLAY_SFX digitation");
        }
        undoDigitations(); // do it also when turnCount condition is not met (when a digiwise was used, we also have to undo that digitation)

        if(Math.random() < SUPPLY_DROP_PROBABILITY) {
            Crate drop = new Crate(terrain.getTerrainWidth()/Terrain.BLOCK_SIZE-2); // -1 for getting max index, -1 for crate width
            server.send("DROP_SUPPLY" + " " + drop.getPosition().getX() + " " + drop.getContent()); // actually, on client side, the crate spawns at an renadom position, but we overwrite it later
            server.send("SET_GAME_COMMENT 0 Nice, a present. That’s like a corollary, it’s for free.");
        }

        server.send("SET_CURRENT_TEAM " + currentTeam);
        teams.get(currentTeam).endRound();
        server.send("CURRENT_TEAM_END_ROUND " + currentTeam);
        server.send("ACTIVATE_FIGURE " + currentTeam);

        if(teams.get(currentTeam).getCurrentFigure().getHealth() <= 10) {
            server.send("PLAY_SFX heartbeat");
        }

        String teamLabelText = "Turn " + (turnCount + 1) + ": It’s Team " + teams.get(currentTeam).getName() + "’s turn! What will " + teams.get(currentTeam).getCurrentFigure().getName() + " do?";
        server.send("TEAM_LABEL_SET_TEXT " + teamLabelText);
        System.out.println(teamLabelText);

        synchronized(turnTimer) {
            turnTimer.set(-MILLISECONDS_BETWEEN_TURNS);
            Server.send("SET_TURN_TIMER " + turnTimer.get());
            updateTurnTimerLabelText();
        }
    }

    private String generateNoHitComment(String name) {
        switch ((int)(Math.random()*2)) {
            case 0:
                return "Is " + name + " drunk?";
            default:
                return name + " seems to be a partyfist.";
        }
    }

    /**
     * Sums up the hp damage caused since last time calling this function.
     * Does not include the damage caused at the currently active figure, put its recently suffered damage is poped anyway.
     * @return hp damage caused since last time calling this function
     * @see de.hhu.propra.team61.objects.Figure#popRecentlySufferedDamage()
     */
    private int collectRecentlyCausedDamage() {
        int recentlyCausedDamage = 0;
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            ArrayList<Figure> figures = team.getFigures();
            for (Figure figure : figures) {
                if (i == currentTeam && team.getCurrentFigure() == figure) {
                    figure.popRecentlySufferedDamage();
                } else {
                    recentlyCausedDamage += figure.popRecentlySufferedDamage();
                }
            }
        }
        return recentlyCausedDamage;
    }

    private void doDigitations() {
        for(Team team: teams) {
            for(Figure figure: team.getFigures()) {
                if(figure.getHealth() >= DIGITATION_MIN_HEALTH && figure.getCausedHpDamage() >= DIGITATION_MIN_CAUSED_DAMAGE) {
                    figure.digitate();
                    server.send("DIGITATE " + getFigureId(figure));
                }
            }
        }
    }

    private void undoDigitations() {
        for (Team team : teams) {
            for (Figure figure : team.getFigures()) {
                if (figure.getHealth() < DEGITATION_HEALTH_THRESHOLD) {
                    figure.degitate();
                    server.send("DEGITATE " + getFigureId(figure));
                }
            }
        }
    }

    private void spawnBoss() {
        String bossName = BOSS_NAMES[(int)(Math.random() * BOSS_NAMES.length)];
        bossSpawnedLeft = (Math.random() > .5);
        initBoss(bossName);
        server.send("SD BOSS SPAWN " + bossName + " " + bossSpawnedLeft);
    }

    private void initBoss(String name) {
        System.out.println(name + " appeared");
        setGameComment("Watch out guys! " + name + " has appeared!", false);
        boss = new Figure(name, "boss", 1000000, 1000000, false, false, false); // TODO short-hand constructor
        boss.setPosition(new Point2D(bossSpawnedLeft ? 0 : terrain.getTerrainWidth() - Figure.NORMED_OBJECT_SIZE, 0));
        Platform.runLater(() -> fieldPane.getChildren().add(boss));
    }

    private void moveBoss() {
        final int moveBy = terrain.getTerrainWidth()/ SUDDEN_DEATH_TURNS;
        if(bossSpawnedLeft) {
            boss.setPosition(boss.getPosition().add(moveBy, 0));
        } else {
            boss.setPosition(boss.getPosition().subtract(moveBy, 0));
        }
        terrain.destroyColumns(boss.getPosition(), bossSpawnedLeft, moveBy);
    }

    /**
     * stops all map window threads and saves game state
     */
    private void shutdown() {
        VorbisPlayer.stop();
        ingameLabel.stopAllTimers();

        if(moveObjectsThread != null) moveObjectsThread.interrupt();
        if(turnTimerThread != null) turnTimerThread.interrupt();

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
                pauseHelpImageView.setImage(pauseImage);
                pausePane.setVisible(true);
            } else {
                pausePane.setVisible(false);
            }
        }
        
        switch (cmd[0]) {
            case "ACTIVATE_FIGURE":
                teams.get(Integer.parseInt(cmd[1])).getCurrentFigure().setActive(true);
                Point2D activePos = teams.get(Integer.parseInt(cmd[1])).getCurrentFigure().getPosition();
                projectileFocused = false;
                if(autoScroll && !projectileFocused) scrollTo(activePos.getX(), activePos.getY(), Figure.NORMED_OBJECT_SIZE, Figure.NORMED_OBJECT_SIZE, true);
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
                if(server == null) {
                    currentFigureChooseWeapon(Integer.parseInt(cmd[1]));
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
            case "CURRENT_FIGURE_SHOOT":
                try {
                    Projectile projectile = teams.get(currentTeam).getCurrentFigure().shoot();
                    if(projectile != null){ //Only weapons need projectiles
                        flyingProjectiles.add(projectile);
                        fieldPane.getChildren().add(projectile);
                        shootingIsAllowed = false;
                        projectileFocused = true;
                        //ToDo setRoundTimer down to 5sec
                    } else { //Treatment for "true" Items
                        fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                    }
                } catch (NoMunitionException e) {
                    System.out.println("no munition");
                    setGameComment("No munition.", true);
                    VorbisPlayer.play("resources/audio/SFX/reload.ogg", false);
                }
                fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
                teams.get(currentTeam).getCurrentFigure().setSelectedItem(null);
                break;
            case "DEGITATE":
                if(server == null) {
                    teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).degitate();
                }
                break;
            case "DIGITATE":
                if(server == null) {
                    teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).digitate();
                }
                break;
            case "END_RAMPAGE":
                if(server == null) {
                    teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).endRampage(Integer.parseInt(cmd[3]));
                }
                break;
            case "FIGURE_SET_POSITION":
                Point2D position = new Point2D(Double.parseDouble(cmd[3]), Double.parseDouble(cmd[4]));
                Figure f = teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2]));
                if(server == null) { // server already applied change to prevent timing issue
                    f.setPosition(position); // TODO alternative setter
                }
                if(autoScroll && (!projectileFocused || client.isLocalGame() || getTeamOfFigure(f) == client.getAssociatedTeam())) {
                    projectileFocused = false; // when moving own figure, stop focusing projectile
                }
                if(cmd.length > 5 && Boolean.parseBoolean(cmd[5])) { // do not scroll when moving an inactive figure
                    if(autoScroll && !projectileFocused) scrollTo(position.getX(), position.getY(), Figure.NORMED_OBJECT_SIZE, Figure.NORMED_OBJECT_SIZE, false);
                }
                break;
            case "PLAY_SFX":
                VorbisPlayer.play("resources/audio/SFX/" + cmd[1] + ".ogg", false);
                break;
            case "FIGURE_ADD_VELOCITY":
                Point2D vector = new Point2D(Double.parseDouble(cmd[3]), Double.parseDouble(cmd[4]));
                Figure fig = teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2]));
                fig.addVelocity(vector);
                break;
            case "REPLACE_BLOCK":
                //if(server == null) { // hack for destruction calculation
                    if (cmd[3].charAt(0) == '#') {
                        cmd[3] = " "; //Decode # as destruction, ' ' is impossible due to Client/Server architecture
                    }
                    terrain.replaceBlock(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), cmd[3].charAt(0));
                //}
                break;
            case "DEACTIVATE_FIGURE":
                shootingIsAllowed = true;
                teams.get(Integer.parseInt(cmd[1])).getCurrentFigure().setActive(false);
                break;
            case "DROP_SUPPLY":
                supplyDrops.add(new Crate(terrain.getTerrainWidth() / Terrain.BLOCK_SIZE - 2, cmd[2]));
                fieldPane.getChildren().add(supplyDrops.get(supplyDrops.size() - 1));
                break;
            case "REMOVE_SUPPLY":
                fieldPane.getChildren().remove(supplyDrops.get(Integer.parseInt(cmd[1])));
                supplyDrops.remove(supplyDrops.get(Integer.parseInt(cmd[1])));
                break;
            case "START_RAMPAGE":
                if(server == null) {
                    teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).startRampage();
                }
                break;
            case "SUPPLY_SET_POSITION":
                position = new Point2D(Double.parseDouble(cmd[2]), Double.parseDouble(cmd[3]));
                supplyDrops.get(Integer.parseInt(cmd[1])).setPosition(position);
                break;
            case "SUPPLY_PICKED_UP":
                teams.get(Integer.parseInt(cmd[1])).getItem(cmd[2]).refill();
                if(client.getAssociatedTeam() == Integer.parseInt(cmd[1]) || client.isLocalGame()) { // only current team should see what has been picked up // TODO what happends if a crate falls on a figure?
                    if(cmd[2].equals("Shotgun")) {
                        setGameComment("Useless, a new " + cmd[2] + ".", false);
                    } else {
                        setGameComment("Cool, a new " + cmd[2] + "!", false);
                    }
                }
                break;
            case "GAME_OVER":
                if (moveObjectsThread != null) moveObjectsThread.interrupt();
                if (turnTimerThread != null) turnTimerThread.interrupt();
                VorbisPlayer.stop();
                ingameLabel.stopAllTimers();
                String winnerName = (cmd[1].equals("-1") ? "NaN" : teams.get(Integer.parseInt(cmd[1])).getName()); // -1 = draw
                GameOverWindow gameOverWindow = new GameOverWindow(sceneController, Integer.parseInt(cmd[1]), winnerName, map, "SETTINGS_FILE.conf", client, clientThread, server, serverThread);
                break;
            case "PROJECTILE_SET_POSITION": // TODO though server did null check, recheck here (problem when connecting later)
                if(server==null) { // TODO code duplication should be avoided
                    final double x = Double.parseDouble(cmd[2]);
                    final double y = Double.parseDouble(cmd[3]);
                    flyingProjectiles.get(Integer.parseInt(cmd[1])).setPosition(new Point2D(x, y));
                    if(autoScroll && projectileFocused) scrollTo(x, y, 0, 0, false);
                }
                break;
            case "ADD_FLYING_PROJECTILES":
                JSONArray input = new JSONArray(extractPart(command, "ADD_FLYING_PROJECTILES "));
                for(int i = 0; i < input.length(); i++){
                    Projectile projectile = new Projectile(input.getJSONObject(i));
                    flyingProjectiles.add(projectile);
                    fieldPane.getChildren().add(projectile);
                }
                break;
            case "REMOVE_FLYING_PROJECTILE":
                if(server == null) {
                    if (flyingProjectiles.size() - 1 >= Integer.parseInt(cmd[1])) {
                        fieldPane.getChildren().remove(flyingProjectiles.get(Integer.parseInt(cmd[1])));
                        flyingProjectiles.remove(Integer.parseInt(cmd[1]));
                    }
                }
                break;
            case "SD":
                if(server == null) {
                    switch (cmd[1]) {
                        case "BOSS":
                            if (cmd[2].equals("MOVE")) {
                                moveBoss();
                            } else if (cmd[2].equals("SPAWN")) {
                                bossSpawnedLeft = Boolean.parseBoolean(cmd[4]);
                                initBoss(cmd[3]);
                            }
                    }
                }
                break;
            case "SET_CURRENT_TEAM":
                teams.get(currentTeam).getCurrentFigure().setActive(false);
                currentTeam = Integer.parseInt(cmd[1]);
                break;
            case "SET_HP":
                if(server == null) {
                    teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).setHealth(Integer.parseInt(cmd[3]));
                }
                break;
            case "SET_SHIELD":
                if(server == null) {
                    teams.get(Integer.parseInt(cmd[1])).getFigures().get(Integer.parseInt(cmd[2])).setShield(Integer.parseInt(cmd[3]));
                }
                break;
            case "CONDITION":
                switch(cmd[1]){
                    case "POISON":
                        VorbisPlayer.play("resources/audio/SFX/poisoned.ogg", false);
                        teams.get(Integer.parseInt(cmd[2])).getFigures().get(Integer.parseInt(cmd[3])).setIsPoisoned(Boolean.parseBoolean(cmd[4]));
                        break;
                    case "PARALYZE":
                        teams.get(Integer.parseInt(cmd[2])).getFigures().get(Integer.parseInt(cmd[3])).setIsParalyzed(Boolean.parseBoolean(cmd[4]));
                        break;
                    case "STUCK":
                        teams.get(Integer.parseInt(cmd[2])).getFigures().get(Integer.parseInt(cmd[3])).setIsStuck(Boolean.parseBoolean(cmd[4]));
                        break;
                    default:
                        System.err.println("handleKeyEventOnClient: no event for key " + command);
                }
                break;
            case "SET_GAME_COMMENT":
                boolean lowPrio = (cmd[1].equals("1"));
                setGameComment(command.substring(18), lowPrio);
                break;
            case "SET_TURN_COUNT":
                turnCount = Integer.parseInt(cmd[1]);
                break;
            case "SET_TURN_TIMER":
                turnTimer.set(Integer.parseInt(cmd[1]));
                break;
            case "SUDDEN_DEATH":
                int teamToKill = Integer.parseInt(cmd[1]);
                teams.get(teamToKill).suddenDeath();
                break;
            case "TEAM_LABEL_SET_TEXT":
                teamLabel.setText(arrayToString(cmd, 1));
                break;
            case "WIND_FORCE":
                windIndicator.setWindForce(Double.parseDouble(cmd[1]));
                break;
            default:
                System.err.println("handleKeyEventOnClient: no event for key " + command);
        }
    }

    private void currentFigureChooseWeapon(int weapon) {
        if (teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
            fieldPane.getChildren().remove(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
        }
        if(weapon != 0) { // 0 is defined as no weapon/deselect weapon
            teams.get(currentTeam).getCurrentFigure().setSelectedItem(teams.get(currentTeam).getItem(weapon - 1));
            fieldPane.getChildren().add(teams.get(currentTeam).getCurrentFigure().getSelectedItem());
        } else {
            teams.get(currentTeam).getCurrentFigure().setSelectedItem(null);
        }
    }

    @Override
    public void handleOnServer(String command) {
        if (command.startsWith("/kickteam ")) {
            String teamToKick = extractPart(command, "/kickteam ");
            try {
                int teamNumber = Integer.parseInt(teamToKick)-1; // user starts counting from 1, we count from 0
                if(teamNumber >= teams.size()) throw new IndexOutOfBoundsException();
                server.send("SUDDEN_DEATH " + teamNumber);
                if(currentTeam == teamNumber) {
                    endTurn();
                }
            } catch(NumberFormatException e) {
                // no team number given, try team name
                for (int i = 0; i < teams.size(); i++) {
                    if (teams.get(i).getName().equals(teamToKick)) {
                        server.send("SUDDEN_DEATH " + i);
                        break;
                    }
                }
            } catch(IndexOutOfBoundsException e) {
                    System.out.println("malformed command " + command);
            }
            return;
        } else if(command.startsWith("CHEAT ")) {
            executeCheat(extractPart(command, "CHEAT ").split(" "));
            return;
        }

        Point2D v = null;

        int team = -1;
        try {
            team = Integer.parseInt(command.split(" ", 2)[0]);
        } catch(NumberFormatException e) {
            System.out.println("handleOnServer: NumberFormatException" + e.getMessage());
            return;
        }
        command = command.split(" ", 2)[1];

        // pause is a special case: do not ignore pause command when paused, and also accept the input when it's not team 0's turn
        switch(command) {
            case "Esc":
            case "Pause":
            case "P":
            case "F1":
                if (team == 0 || client.isLocalGame()) { // allowing pausing by host (team 0) and when playing local game
                    pause = !pause;
                    server.send("PAUSE " + pause);
                } else {
                    help = !help;
                    pauseHelpImageView.setImage(helpImage);
                    pausePane.setVisible(help);
                }
                break;
        }

        if(pause) {
            System.out.println("Game paused, ignoring command " + command);
            return;
        }

        if(turnTimer.get() <= 0) {
            System.out.println("turnTimer at " + turnTimer.get() + ", ignoring command" + command);
            return;
        }

        if (team != currentTeam && !client.isLocalGame()) {
            System.out.println("The key event " + command + " of team " + team + " has been discarded. Operation not allowed, currentTeam is " + currentTeam);
            return;
        }

        switch(command) {
            case "Space":
                if(teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    server.send("CURRENT_FIGURE_SHOOT");
                }
                break;
            // these codes always result in optical changes only, so nothing to do on server side
            case "Up":
            case "W":
                if(teams.get(currentTeam).getCurrentFigure().getSelectedItem() != null) {
                    server.send("CURRENT_FIGURE_ANGLE_UP");
                } else {
                    teams.get(currentTeam).getCurrentFigure().jump();
                }
                break;
            case "Down":
            case "S":
                server.send("CURRENT_FIGURE_ANGLE_DOWN");
                break;
            case "Left":
            case "A":
                server.send("CURRENT_FIGURE_FACE_LEFT");
                moveCurrentlyActiveFigure(new Point2D(-Figure.WALK_SPEED, 0));
                break;
            case "Right":
            case "D":
                server.send("CURRENT_FIGURE_FACE_RIGHT");
                moveCurrentlyActiveFigure(new Point2D(Figure.WALK_SPEED, 0));
                break;
            case "1":
            case "Numpad 1":
                if(shootingIsAllowed) {
                    server.send("CURRENT_FIGURE_CHOOSE_WEAPON 1");
                    currentFigureChooseWeapon(1);
                    server.send("SET_GAME_COMMENT 1 Bazooka ("+teams.get(currentTeam).getCurrentFigure().getSelectedItem().getFormattedMunition()+"): " + Bazooka.DESCRIPTION, currentTeam);
                    server.send("PLAY_SFX bazooka");
                }
                break;
            case "2":
            case "Numpad 2":
                if(shootingIsAllowed) {
                    server.send("CURRENT_FIGURE_CHOOSE_WEAPON 2");
                    currentFigureChooseWeapon(2);
                    server.send("SET_GAME_COMMENT 1 Grenade ("+teams.get(currentTeam).getCurrentFigure().getSelectedItem().getFormattedMunition()+"): " + Grenade.DESCRIPTION, currentTeam);
                    server.send("PLAY_SFX grenade");
                }
                break;
            case "3":
            case "Numpad 3":
                if(shootingIsAllowed) {
                    server.send("CURRENT_FIGURE_CHOOSE_WEAPON 3");
                    currentFigureChooseWeapon(3);
                    server.send("SET_GAME_COMMENT 1 Shotgun (∞): " + Shotgun.DESCRIPTION, currentTeam);
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().refill();
                    server.send("PLAY_SFX shotgun");
                }
                break;
            case "4":
            case "Numpad 4":
                if(shootingIsAllowed) {
                    server.send("CURRENT_FIGURE_CHOOSE_WEAPON 4");
                    currentFigureChooseWeapon(4);
                    server.send("SET_GAME_COMMENT 1 Rifle ("+teams.get(currentTeam).getCurrentFigure().getSelectedItem().getFormattedMunition()+"): " + Rifle.DESCRIPTION, currentTeam);
                    server.send("PLAY_SFX rifle");
                }
                break;
            case "5":
            case "Numpad 5":
                if(shootingIsAllowed) {
                    server.send("CURRENT_FIGURE_CHOOSE_WEAPON 5");
                    currentFigureChooseWeapon(5);
                    server.send("SET_GAME_COMMENT 1 Poisoned Arrow ("+teams.get(currentTeam).getCurrentFigure().getSelectedItem().getFormattedMunition()+"): " + PoisonedArrow.DESCRIPTION, currentTeam);
                    server.send("PLAY_SFX poisonArrow");
                }
                break;
            case "6":
            case "Numpad 6":
                if(shootingIsAllowed) {
                    server.send("CURRENT_FIGURE_CHOOSE_WEAPON 6");
                    currentFigureChooseWeapon(6);
                    server.send("SET_GAME_COMMENT 1 Bananabomb ("+teams.get(currentTeam).getCurrentFigure().getSelectedItem().getFormattedMunition()+"): " + Bananabomb.DESCRIPTION, currentTeam);
                    server.send("PLAY_SFX banana");
                }
                break;
            case "7":
            case "Numpad 7":
                if(shootingIsAllowed) {
                    server.send("CURRENT_FIGURE_CHOOSE_WEAPON 7");
                    currentFigureChooseWeapon(7);
                    server.send("SET_GAME_COMMENT 1 Digiwise ("+teams.get(currentTeam).getCurrentFigure().getSelectedItem().getFormattedMunition()+"): " + Digiwise.DESCRIPTION, currentTeam);
                    server.send("PLAY_SFX digiwise");
                }
                break;
            case "8":
            case "Numpad 8":
                if(shootingIsAllowed) {
                    server.send("CURRENT_FIGURE_CHOOSE_WEAPON 8");
                    currentFigureChooseWeapon(8);
                    server.send("SET_GAME_COMMENT 1 Medipack ("+teams.get(currentTeam).getCurrentFigure().getSelectedItem().getFormattedMunition()+"): " + Medipack.DESCRIPTION, currentTeam);
                    server.send("PLAY_SFX medipack");
                }
                break;
            case "9":
            case "Numpad 9":
                if(shootingIsAllowed) {
                    server.send("CURRENT_FIGURE_CHOOSE_WEAPON 9");
                    currentFigureChooseWeapon(9);
                    server.send("SET_GAME_COMMENT 1 Skip (∞): " + Skip.DESCRIPTION, currentTeam);
                    teams.get(currentTeam).getCurrentFigure().getSelectedItem().refill();
                    server.send("PLAY_SFX skipTurn");
                }
                break;
            case "0": //Deselect Item
            case "Numpad 0":
                server.send("CURRENT_FIGURE_CHOOSE_WEAPON 0");
                currentFigureChooseWeapon(0);
                break;
            default:
                System.out.println("handleOnServer: no event for key " + command);
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
                server.send("SET_GAME_COMMENT 0 You are now alone.");
                System.out.println("You are now alone.");
                break;
            case "1up": // 100 live for first figure of first team
                teams.get(0).getFigures().get(0).setHealth(100);
                server.send("SET_GAME_COMMENT 0 Ate my spinach.");
                System.out.println("Ate my spinach.");
                break;
            case "up+": // 1000 live for all living figures
                for(Team t: teams) {
                    for(Figure f: t.getFigures()) {
                        if(f.getHealth() > 0) {
                            f.setHealth(1000);
                        }
                    }
                }
                server.send("SET_GAME_COMMENT 0 Spinach bomb.");
                System.out.println("Spinach bomb.");
                break;
            case "1up+": // 1000 live for first figure of first team
                teams.get(0).getFigures().get(0).setHealth(1000);
                System.out.println("Ate too much spinach.");
                break;
            case "degitate": // calls undoDigitations() method
                undoDigitations();
                server.send("SET_GAME_COMMENT 0 Returning to Baby I");
                System.out.println("Returning to Baby I");
                break;
            case "digitate": // calls doDigitations() method
                doDigitations();
                server.send("SET_GAME_COMMENT 0 Digitation.");
                System.out.println("Digitation.");
                break;
            case "forcedig": // forces digitation of all figures
                for(Team team: teams) {
                    for (Figure figure : team.getFigures()) {
                        figure.digitate();
                    }
                }
                server.send("SET_GAME_COMMENT 0 Mass-Digitation");
                server.send("PLAY_SFX digitation");
                System.out.println("Mass-Digitation.");
                break;
            case "rewind": // sets wind to given value
                terrain.setWind(Double.parseDouble(cmd[1]));
                windIndicator.setWindForce(terrain.getWindMagnitude());
                server.send("SET_GAME_COMMENT 0 It's windy.");
                System.out.println("It’s windy.");
                break;
            case "gameover": // ends the game by showing game over window
                Platform.runLater(() -> handleOnClient("GAME_OVER -1"));
                break;
            case "sd": // starts/continues sudden death
                Platform.runLater(() -> {
                    switch (cmd[1]) {
                        case "boss":
                            if (cmd.length > 2) {
                                bossSpawnedLeft = Boolean.parseBoolean(cmd[2]);
                                initBoss("cheat");
                            } else {
                                if (boss == null) {
                                    spawnBoss();
                                } else {
                                    moveBoss();
                                }
                            }
                            break;
                        case "flood":
                            if (cmd.length > 2) {
                                floodLevel = Integer.parseInt(cmd[2]);
                            } else {
                                if (floodLevel != -1) {
                                    terrain.increaseFlood(++floodLevel);
                                } else {
                                    floodLevel = 0;
                                }
                            }
                            break;
                    }
                });
                System.out.println("Premature Death");
                break;
            case "time+": // increases turnTimer by given value (s), or by 20 s if no value is given
                turnTimer.addAndGet(cmd.length > 1 ? Integer.parseInt(cmd[1])*1000 : 20000);
                server.send("SET_GAME_COMMENT 0 It's about time.");
                System.out.println("It’s about time.");
                break;
            default:
                client.sendChatMessage("««« Haw-haw! This user failed to cheat … »»» " + arrayToString(cmd, 0));
                server.send("SET_GAME_COMMENT 0 No cheating, please!");
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
        server.send("FIGURE_SET_POSITION " + getFigureId(f) + " " + newPos.getX() + " " + newPos.getY() + " true");
    }

    @Override
    public String getStateForNewClient() {
        return "STATUS MAPWINDOW " + this.toJson().toString();
    }

    /**
     * Shows the given label in right upper corner.
     * @param content is the text shown in the Label.
     * @param lowPrio if a comment has low priority, is is immediately overwritten when another line is added
     */
    void setGameComment(String content, boolean lowPrio){
        Platform.runLater(() -> {
            ingameLabel.addLine(content, lowPrio);
        });
    }
}
