package de.hhu.propra.team61.artificialIntelligence;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.objects.*;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;

/**
 * Created by markus on 29.08.14. // TODO IMPORTANT
 */
public class SimpleAI extends ArtificialIntelligence {

    private Figure currentFigure; // TODO IMPORTANT doc
    Point2D currentFigurePosition;

    Figure closestEnemy;
    Point2D closestEnemyPosition;

    double angleToEnemy;
    boolean angleDown;

    public SimpleAI(Team ownTeam, ArrayList<Team> teams, Terrain terrain, ArrayList<Crate> crates, JSONObject gameSettings) {
        super(ownTeam, teams, terrain, crates, gameSettings);
    }

    public ArrayList<String> makeMove() {
        ArrayList<String> commands = new ArrayList<>();

        switch(state) {
            case NEW_TURN:
            case TARGET_FOUND:
                prepareAim(commands);
                break;
            case TARGET_FACED:
                aim(commands);
                break;
            case TARGET_AIMED:
                commands.add(ownTeam.getNumber() + " Space");
                state = AIState.TURN_FINISHED;
                break;
            case TURN_FINISHED:
                System.err.println("We are finished.");
                return commands;
            default:
                System.err.println("??");
                return commands;
        }

        if(commands.size() == 0) {
            commands.add("NOOP");
        }

        return commands;
    }

    private void chooseEnemy(int n) {
        currentFigure = ownTeam.getCurrentFigure();
        currentFigurePosition = currentFigure.getPosition();
        currentFigurePosition = currentFigurePosition.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);

        ArrayList<Figure> enemiesByDistance = getEnemiesByDistance();
        if(n < enemiesByDistance.size()) {
            closestEnemy = getEnemiesByDistance().get(n); // TODO IMPORTANT index check
        } else if(enemiesByDistance.size() == 0) {
            System.err.println("TODO IMPORTANT What now?");
        } else {
            closestEnemy = getEnemiesByDistance().get(0);
        }
        closestEnemyPosition = closestEnemy.getPosition();
        closestEnemyPosition = closestEnemyPosition.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);

        angleToEnemy = Math.toDegrees(Math.acos((closestEnemyPosition.getY()-currentFigurePosition.getY()) / currentFigurePosition.distance(closestEnemyPosition)));
        angleToEnemy = Math.abs(90 - angleToEnemy); // we want to have the angle to the horizontal
        angleDown = (closestEnemyPosition.getY() > currentFigurePosition.getY());
    }

    private void aim(ArrayList<String> commands) {
        for(int i = (int)(angleToEnemy/Weapon.ANGLE_STEP); i > 0; i--) {
            if(angleDown) {
                commands.add(ownTeam.getNumber() + " Down");
            } else {
                commands.add(ownTeam.getNumber() + " Up");
            }
        }
        state = AIState.TARGET_AIMED;
    }

    private void prepareAim(ArrayList<String> commands) {
        currentFigure = ownTeam.getCurrentFigure();
        currentFigurePosition = currentFigure.getPosition();
        currentFigurePosition = currentFigurePosition.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);

        chooseEnemy(0);

        try {
            Point2D crosshairOffset = new Point2D((closestEnemyPosition.getX() < currentFigurePosition.getX() ? -1 : 1)*Math.cos(angleToEnemy) * Figure.NORMED_OBJECT_SIZE,
                    (closestEnemyPosition.getY() < currentFigurePosition.getY() ? -1 : 1)*Figure.NORMED_OBJECT_SIZE * Math.sin(angleToEnemy));
            System.out.println(crosshairOffset);
            terrain.getPositionForDirection(currentFigurePosition.add(crosshairOffset), closestEnemyPosition.subtract(currentFigurePosition), new Rectangle2D(currentFigurePosition.getX()+crosshairOffset.getX(), currentFigurePosition.getY()+crosshairOffset.getY(), 1, 1), false, false, false, false);
            System.err.println("first enemy good?"); //correct start position/end position for calculations (NORMED_OBJECT_SIZE)
        } catch (CollisionException e) {
            if(!e.getCollisionPartnerClass().equals("figure")) {
                System.out.println("standing at" + currentFigurePosition + ", aiming at " + closestEnemy.getName() + " " + closestEnemyPosition);
                System.out.println("angle " + angleToEnemy + ", steps " + (int)(angleToEnemy/Weapon.ANGLE_STEP));
                System.out.println("first enemy not good");

                chooseEnemy(1);
            } else {
                System.err.println("first enemy is good (hit)");
            }
        }

        state = AIState.TARGET_FOUND;

        // turn to enemy
        if(closestEnemyPosition.getX() < currentFigurePosition.getX()) {
            commands.add(ownTeam.getNumber() + " Right"); // so final position is (nearly [friction, wind]) equal to original position
            commands.add(ownTeam.getNumber() + " Left");
        } else {
            commands.add(ownTeam.getNumber() + " Left");
            commands.add(ownTeam.getNumber() + " Right");
        }

        state = AIState.TARGET_FACED;

        // choose weapon
        commands.add(ownTeam.getNumber() + " " + ((int)(Math.random()*4)+1));

        // aim
        System.out.println("standing at" + currentFigurePosition + ", aiming at " + closestEnemy.getName() + " " + closestEnemyPosition);
        System.out.println("angle " + angleToEnemy + ", steps " + (int)(angleToEnemy/Weapon.ANGLE_STEP));
    }

}
