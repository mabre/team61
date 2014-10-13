package de.hhu.propra.team61.artificialIntelligence;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.objects.CollisionException;
import de.hhu.propra.team61.objects.Crate;
import de.hhu.propra.team61.objects.Figure;
import de.hhu.propra.team61.objects.Terrain;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.json.simple.JSONAware;

import java.util.ArrayList;

/**
 * Base class for artificial intelligences controlling the figures of a team.
 *
 * This class implements an AI which just skippes in every move. Sensible AIs should override at least {@link #getAIType()},
 * and {@link #makeMove()}. Moreover, this class contains some convenience functions useful for different tapes of AIs,
 * like {@link #getEnemiesByDistance()}.
 */
public class ArtificialIntelligence {

    /** the team whose figures are controlled by this AI */
    protected final Team ownTeam;
    /** list containing all teams except for {@link #ownTeam} */
    protected final ArrayList<Team> enemyTeams;
    /** the terrain on which is played (also contains wind information) */
    protected final Terrain terrain;
    /** the crates currently on the terrain */
    protected final ArrayList<Crate> crates;
    /** the settings with which the game has been started (containing time per turn) */
    protected final JSONObject gameSettings;

    /** for keeping track of the "though states" of the AI */
    protected enum AIState {
        NEW_TURN, COLLECTING_CRATE, CRATE_COLLECTED, TARGET_FOUND, TARGET_FACED, TARGET_AIMED, TURN_FINISHED
    }

    /** the current state of the AI */
    protected AIState state;

    /**
     * Creates a new (dummy) AI instance.
     * @param ownTeam the teams whose figures should be controlled by this AI
     * @param teams list of all teams
     * @param terrain reference to the terrain being played on
     * @param crates reference to a list containing the crates on the terrain
     * @param gameSettings json containg the basic game settings
     */
    public ArtificialIntelligence(Team ownTeam, ArrayList<Team> teams, Terrain terrain, ArrayList<Crate> crates, JSONObject gameSettings) {
        this.ownTeam = ownTeam;
        this.enemyTeams = new ArrayList<>();
        this.enemyTeams.addAll(teams);
        this.enemyTeams.remove(ownTeam);
        this.terrain = terrain;
        this.crates = crates;
        this.gameSettings = gameSettings;
        this.state = AIState.NEW_TURN;
    }

    /**
     * Gets the type of this AI object.
     * @see de.hhu.propra.team61.artificialIntelligence.AIType
     * @return type of this AI
     */
    public AIType getAIType() {
        return AIType.DUMMY;
    }

    /**
     * Lets the AI calculate a (partial) move.
     * This method can be called more than once per turn. The AI has finished when an empty list is returned.
     * @return a list of commands to be passed to {@link de.hhu.propra.team61.MapWindow#handleOnServer(String)}
     */
    public ArrayList<String> makeMove() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(ownTeam.getNumber() + " 9");
        commands.add(ownTeam.getNumber() + " Space");
        return commands;
    }

    /**
     * Signals the AI that the current turn is over.
     */
    public void endTurn() {
        state = AIState.NEW_TURN;
    }

    /**
     * Gets a list of all living enemy figures, sorted by distance to the current figure of {@link #ownTeam}.
     * @return List of enemies sorted by distance to current figure
     */
    protected ArrayList<Figure> getEnemiesByDistance() {
        ArrayList<Figure> enemiesByDistance = new ArrayList<>();
        ArrayList<Figure> enemies = new ArrayList<>();

        for(Team t: enemyTeams) {
            for(Figure f: t.getFigures()) {
                if(f.getHealth() > 0) {
                    enemies.add(f);
                }
            }
        }

        while(!enemies.isEmpty()) {
            double smallestDistance = -1;
            Figure closestFigure = enemies.get(0);
            for (Figure f : enemies) {
                double distance = f.getPosition().distance(ownTeam.getCurrentFigure().getPosition());
                if (smallestDistance == -1 || distance < smallestDistance) {
                    smallestDistance = distance;
                    closestFigure = f;
                }
            }
            enemiesByDistance.add(closestFigure);
            enemies.remove(closestFigure);
        }

        return enemiesByDistance;
    }

    /**
     * Determines whether a team member is within {@link de.hhu.propra.team61.objects.Figure#NORMED_OBJECT_SIZE} distance to the given position
     * @param pos position (top left corner) of an object
     * @return true of a friend is nearby
     */
    protected boolean friendIsNearPosition(Point2D pos) {
        for(Figure f: ownTeam.getFigures()) {
            if(pos.distance(f.getPosition().add(Figure.NORMED_OBJECT_SIZE/2, Figure.NORMED_OBJECT_SIZE/2)) < Figure.NORMED_OBJECT_SIZE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculated the center of the object at the given position with a width and height of {@link de.hhu.propra.team61.objects.Figure#NORMED_OBJECT_SIZE}.
     * @param object the position of the top left corner of the object
     * @return the center of the given normed object
     */
    protected Point2D getCenterForNormedObject(Point2D object) {
        return object.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);
    }

    /**
     * Determines whether the figure at the given position is closer than {@link de.hhu.propra.team61.objects.Figure#NORMED_OBJECT_SIZE} px to a wall.
     * @param pos upper left corner of the figure (px)
     * @param left whether to check left or right side
     * @return true if the figure is facing a nearby wall (slopes are no walls)
     */
    protected boolean facingWall(Point2D pos, boolean left) {
        try {
            terrain.getPositionForDirection(pos, new Point2D((left ? -1 : 1) * Figure.NORMED_OBJECT_SIZE, 0), new Rectangle2D(pos.getX(), pos.getY(), Figure.NORMED_OBJECT_SIZE, Figure.NORMED_OBJECT_SIZE), true, true, false, false);
            return false;
        } catch(CollisionException e) {
            return true;
        }
    }

}
