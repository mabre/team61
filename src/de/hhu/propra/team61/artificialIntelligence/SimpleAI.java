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

    private static int USE_MEDIPACK_THRESHOLD = 40;

    private Figure currentFigure; // TODO IMPORTANT doc
    Point2D currentFigurePosition;

    Figure closestEnemy;
    Point2D closestEnemyPosition;

    double angleToEnemy;
    boolean angleDown;
    double distanceToEnemy;

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

    /**
     * Sets {@link #currentFigure}, {@link #currentFigurePosition}, and {@link #distanceToEnemy} to the n-th nearest enemy figure next to {@link #currentFigure}.
     * If n is less than the number of enemies, the nearest enemy is chosen.
     * @param n
     */
    private void chooseEnemy(int n) {
        currentFigure = ownTeam.getCurrentFigure();
        currentFigurePosition = currentFigure.getPosition();
        currentFigurePosition = currentFigurePosition.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);

        ArrayList<Figure> enemiesByDistance = getEnemiesByDistance();
        if(n < enemiesByDistance.size()) {
            closestEnemy = getEnemiesByDistance().get(n);
        } else if(enemiesByDistance.size() == 0) {
            System.err.println("TODO IMPORTANT What now?");
        } else {
            closestEnemy = getEnemiesByDistance().get(0);
        }
        closestEnemyPosition = closestEnemy.getPosition();
        closestEnemyPosition = closestEnemyPosition.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);

        distanceToEnemy = Math.sqrt(Math.pow(closestEnemyPosition.getX()-currentFigurePosition.getX(), 2) + Math.pow(closestEnemyPosition.getY()-currentFigurePosition.getY(), 2));

        angleToEnemy = Math.toDegrees(Math.acos((closestEnemyPosition.getY() - currentFigurePosition.getY()) / currentFigurePosition.distance(closestEnemyPosition)));
        angleToEnemy = Math.abs(90 - angleToEnemy); // we want to have the angle to the horizontal
        angleDown = (closestEnemyPosition.getY() > currentFigurePosition.getY());
    }

    private void useItem(ArrayList<String> commands) {
        if(currentFigure.getHealth() < USE_MEDIPACK_THRESHOLD && ownTeam.getItem(8-1).getMunition() > 0) {
            commands.add(ownTeam.getNumber() + " 8");
            commands.add(ownTeam.getNumber() + " Space");
        }
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

        // try hitting one of the next 8 enemies, fall back to 1st one if no hit is possible
        boolean foundEnemy = false;
        for(int i=0; i<8 && !foundEnemy; i++) {
            chooseEnemy(i);

            try {
                Point2D crosshairOffset = new Point2D((closestEnemyPosition.getX() < currentFigurePosition.getX() ? -1 : 1) * Math.cos(angleToEnemy) * Figure.NORMED_OBJECT_SIZE,
                        (closestEnemyPosition.getY() < currentFigurePosition.getY() ? -1 : 1) * Figure.NORMED_OBJECT_SIZE * Math.sin(angleToEnemy)); // TODO IMPORTANT also check skightly higher
                System.out.println(crosshairOffset);
                terrain.getPositionForDirection(currentFigurePosition.add(crosshairOffset), closestEnemyPosition.subtract(currentFigurePosition), new Rectangle2D(currentFigurePosition.getX() + crosshairOffset.getX(), currentFigurePosition.getY() + crosshairOffset.getY(), 1, 1), false, false, false, false);
                System.err.println((i+1) + "st enemy good?");
                foundEnemy = true;
            } catch (CollisionException e) {
                if (!e.getCollisionPartnerClass().equals("figure")) {
                    System.out.println("standing at" + currentFigurePosition + ", aiming at " + closestEnemy.getName() + " " + closestEnemyPosition);
                    System.out.println("angle " + angleToEnemy + ", steps " + (int) (angleToEnemy / Weapon.ANGLE_STEP));
                    System.out.println((i+1) + "st enemy not good");
                } else {
                    System.err.println((i+1) + "st enemy is good (hit)"); // TODO do not call a hit of a friend good
                    foundEnemy = true;
                }
            }
        }

        useItem(commands);

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
        int weaponNumber;

        if(distanceToEnemy < 100) {
            weaponNumber = (Math.random() < .5 ? 2 : 3); // grenade or shotgun
        } else if(distanceToEnemy > 250) {
            weaponNumber = (Math.random() < .5 ? 3 : 4); // shotgun or rifle
        } else {
            weaponNumber = (Math.random() < .5 ? 1 : 3); // bazooka or shotgun
        }

        if(ownTeam.getItem(weaponNumber-1).getMunition() <= 0) {
            weaponNumber = 3; // has infinite munition
        }

        commands.add(ownTeam.getNumber() + " " + weaponNumber); // shotgun or rifle

        // aim
        System.out.println("standing at" + currentFigurePosition + ", aiming at " + closestEnemy.getName() + " " + closestEnemyPosition);
        System.out.println("angle " + angleToEnemy + ", steps " + (int)(angleToEnemy/Weapon.ANGLE_STEP));

        // for "heavy" weapons, compensate for gravity
        if (weaponNumber == 1) {
            commands.add(ownTeam.getNumber() + " Up");
        } else if (weaponNumber == 2) {
            commands.add(ownTeam.getNumber() + " Up");
            commands.add(ownTeam.getNumber() + " Up");
        }
    }

}
