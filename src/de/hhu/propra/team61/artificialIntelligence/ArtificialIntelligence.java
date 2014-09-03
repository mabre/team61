package de.hhu.propra.team61.artificialIntelligence;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.objects.Crate;
import de.hhu.propra.team61.objects.Figure;
import de.hhu.propra.team61.objects.Terrain;
import javafx.geometry.Point2D;

import java.util.ArrayList;

/**
 * Created by markus on 29.08.14. // TODO IMPORTANT doc
 */
public class ArtificialIntelligence {

    protected final Team ownTeam;
    protected final ArrayList<Team> enemyTeams;
    protected final Terrain terrain;
    protected final ArrayList<Crate> crates;
    protected final JSONObject gameSettings;

    protected enum AIState {
        NEW_TURN, TARGET_FOUND, TARGET_FACED, TARGET_AIMED, TURN_FINISHED
    }

    protected AIState state;

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

    public ArrayList<String> makeMove() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(ownTeam.getNumber() + " 9");
        commands.add(ownTeam.getNumber() + " Space");
        return commands;
    }

    public void endTurn() {
        state = AIState.NEW_TURN;
    }

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

    boolean friendIsNearPosition(Point2D pos) {
        for(Figure f: ownTeam.getFigures()) {
            if(pos.distance(f.getPosition().add(Figure.NORMED_OBJECT_SIZE/2, Figure.NORMED_OBJECT_SIZE/2)) < Figure.NORMED_OBJECT_SIZE) {
                return true;
            }
        }
        return false;
    }

}
