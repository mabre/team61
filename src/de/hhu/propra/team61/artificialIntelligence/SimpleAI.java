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
    private static int USE_DIGIWISE_THRESHOLD = 60;
    private static double USE_DIGIWISE_PROBABILITY = .2;

    private Figure currentFigure; // TODO IMPORTANT doc
    Point2D currentFigurePosition;

    Figure closestEnemy;
    Point2D closestEnemyPosition;

    double angleToEnemy;
    boolean angleDown;
    double distanceToEnemy;
    int repetition = 0;

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
     * Updates {@link #currentFigure}, {@link #currentFigurePosition}, and sets {@link #closestEnemy}, {@link #closestEnemyPosition}, and {@link #distanceToEnemy} to the n-th nearest enemy figure next to {@link #currentFigure}.
     * If n is less than the number of enemies, the nearest enemy is chosen. If no enemy is found, {@link #closestEnemy}
     * is set to null.
     * @param n n-th closest enemy is chosen (counting starts from 0)
     */
    private void chooseEnemy(int n) {
        currentFigure = ownTeam.getCurrentFigure();
        currentFigurePosition = currentFigure.getPosition();
        currentFigurePosition = currentFigurePosition.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);

        ArrayList<Figure> enemiesByDistance = getEnemiesByDistance();
        if(n < enemiesByDistance.size()) {
            closestEnemy = getEnemiesByDistance().get(n);
        } else if(enemiesByDistance.size() == 0) {
            closestEnemy = null;
            return;
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
        // use medipack when having low hp, being the last figure of the team and can be killed with a bazooka, or when being poisoned
        // do not use a medipack when standing on water (unless being the last figure)
        if (ownTeam.getItem(8 - 1).getMunition() > 0 &&
                (!terrain.standingOnLiquid(currentFigurePosition) || ownTeam.getNumberOfLivingFigures() == 1)) {
            if (currentFigure.getHealth() < USE_MEDIPACK_THRESHOLD ||
                    (ownTeam.getNumberOfLivingFigures() == 1 && currentFigure.getHealth() <= 50) ||
                    currentFigure.getIsPoisoned()) {
                commands.add(ownTeam.getNumber() + " 8");
                commands.add(ownTeam.getNumber() + " Space");
            }
        }
        // use digiwise randomly when having enough hp, or being the last figue of the team
        if (ownTeam.getItem(7 - 1).getMunition() > 0 && !currentFigure.isDigitated()) {
            if ((currentFigure.getHealth() > USE_DIGIWISE_THRESHOLD && Math.random() < USE_DIGIWISE_PROBABILITY) ||
                    ownTeam.getNumberOfLivingFigures() == 1) {
                commands.add(ownTeam.getNumber() + " 7");
                commands.add(ownTeam.getNumber() + " Space");
            }
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

        // TODO IMPORTANT collect nearby crates

        // try hitting one of the next 8 enemies, fall back to 1st one if no hit is possible
        boolean foundEnemy = false;
        boolean hittingFriend = false;
        boolean firstHittingFriend = false;
        for (int i = 0; i < 8 && !foundEnemy; i++) {
            chooseEnemy(i);

            if (closestEnemy == null) break;

            try {
                hittingFriend = false;
                Point2D crosshairOffset = new Point2D((closestEnemyPosition.getX() < currentFigurePosition.getX() ? -1 : 1) * Math.cos(Math.toRadians(angleToEnemy)) * Figure.NORMED_OBJECT_SIZE,
                        (closestEnemyPosition.getY() < currentFigurePosition.getY() ? -1 : 1) * Math.sin(Math.toRadians(angleToEnemy)) * Figure.NORMED_OBJECT_SIZE); // TODO also check slightly higher
                System.out.println(crosshairOffset);
                System.out.println("standing at" + currentFigurePosition + ", aiming at " + closestEnemy.getName() + " " + closestEnemyPosition);
                System.out.println("angle " + angleToEnemy + ", steps " + (int) (angleToEnemy / Weapon.ANGLE_STEP));
                terrain.getPositionForDirection(currentFigurePosition.add(crosshairOffset), closestEnemyPosition.subtract(currentFigurePosition), new Rectangle2D(currentFigurePosition.getX() + crosshairOffset.getX(), currentFigurePosition.getY() + crosshairOffset.getY(), 1, 1), false, false, false, false);
                System.err.println((i + 1) + "st enemy good?");
                foundEnemy = true;
            } catch (CollisionException e) {
                if (!e.getCollisionPartnerClass().equals("figure")) {
                    System.out.println((i + 1) + "st enemy not good");
                } else {
                    System.out.println((i + 1) + "st enemy is good (hit)"); // TODO IMPORTANT do not call a hit of a friend good
                    if (friendIsNearPosition(e.getLastGoodPosition())) {
                        foundEnemy = false;
                        hittingFriend = true;
                        if (i == 0) firstHittingFriend = true;
                        System.out.println("no, hitting friend");
                    } else {
                        foundEnemy = true;
                    }
                }
            }
        }

        if (!foundEnemy && repetition < Math.max(gameSettings.getInt("turnTimer", 20)*5, 20*5)) {
            System.out.println("No enemy within range found.");
            repetition++;

            chooseEnemy(0);
            boolean goingLeft = closestEnemyPosition.getX() < currentFigurePosition.getX();
            System.out.println("going left? " + goingLeft);
            Point2D newGoToPos = currentFigurePosition.subtract(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2); // TODO IMPORTANT rename or change var
            Point2D goToPos = null;
            for (int j = 0; j < 2 && newGoToPos != null && !terrain.standingOnLiquid(newGoToPos) && terrain.standingOnGround(newGoToPos)
                    && !facingWall(newGoToPos, goingLeft) && (newGoToPos.distance(closestEnemyPosition) > 150 || firstHittingFriend); j++) {
                goToPos = newGoToPos;
                try {
                    newGoToPos = terrain.getPositionForDirection(goToPos, new Point2D((goingLeft ? -1 : 1) * Figure.WALK_SPEED, 0), new Rectangle2D(goToPos.getX(), goToPos.getY(), Figure.NORMED_OBJECT_SIZE, Figure.NORMED_OBJECT_SIZE), true, true, true, true);
                    commands.add(ownTeam.getNumber() + (goingLeft ? " Left" : " Right"));
                } catch (CollisionException e) {
                    newGoToPos = null;
                }
            }
            if(commands.size() > 0) return; // do not return if we didn't do anything
        }

        useItem(commands);

        if(hittingFriend) {
            closestEnemy = null;
            angleToEnemy = 0;
            commands.add(ownTeam.getNumber() + " 9");
            state = AIState.TARGET_FACED;
            return;
        }

        state = AIState.TARGET_FOUND;
        repetition = 0;

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
            double grenadeProbability = ((closestEnemy.getHealth() <= 40 && closestEnemy.getHealth() >= 20) ? .8 : .4);
            weaponNumber = (Math.random() < grenadeProbability ? 2 : 3); // grenade or shotgun
        } else if(distanceToEnemy > 140 && distanceToEnemy < 200 && !(angleToEnemy > 45 && !angleDown) // do not aim up (probably won't work)
                && Math.random() < .5 && ownTeam.getItem(6-1).getMunition() > 0) {
            weaponNumber = 6; // banana bomb
        } else if(distanceToEnemy > 250 && distanceToEnemy <= 400) {
            weaponNumber = (Math.random() < .5 ? 3 : 4); // shotgun or rifle
        } else if(distanceToEnemy > 400) {
            weaponNumber = (Math.random() < .1 ? 3 : 4); // shotgun or rifle
        } else {
            double bazookaProbability = ((closestEnemy.getHealth() <= 50 && closestEnemy.getHealth() >= 20) ? .8 : .4);
            weaponNumber = (Math.random() < bazookaProbability ? 1 : 3); // bazooka or shotgun
        }

        if(ownTeam.getItem(weaponNumber-1).getMunition() <= 0) {
            weaponNumber = 3; // has infinite munition
        }

        commands.add(ownTeam.getNumber() + " " + weaponNumber);

        // aim
        System.out.println("standing at" + currentFigurePosition + ", aiming at " + closestEnemy.getName() + " " + closestEnemyPosition);
        System.out.println("angle " + angleToEnemy + ", steps " + (int)(angleToEnemy/Weapon.ANGLE_STEP));

        // for "heavy" weapons, compensate for gravity
        if (weaponNumber == 1) {
            commands.add(ownTeam.getNumber() + " Up");
            if (distanceToEnemy > 210) {
                commands.add(ownTeam.getNumber() + " Up");
            }
        } else if (weaponNumber == 2) {
            commands.add(ownTeam.getNumber() + " Up");
            commands.add(ownTeam.getNumber() + " Up");
        } else if (weaponNumber == 6) {
            commands.add(ownTeam.getNumber() + " Up");
            commands.add(ownTeam.getNumber() + " Up");
            commands.add(ownTeam.getNumber() + " Up");
            commands.add(ownTeam.getNumber() + " Up");
            commands.add(ownTeam.getNumber() + " Up");
        }
    }

    private boolean facingWall(Point2D pos, boolean left) {
        try {
            terrain.getPositionForDirection(pos, new Point2D((left ? -1 : 1) * Figure.NORMED_OBJECT_SIZE, 0), new Rectangle2D(pos.getX(), pos.getY(), Figure.NORMED_OBJECT_SIZE, Figure.NORMED_OBJECT_SIZE), true, true, false, false);
            return false;
        } catch(CollisionException e) {
            return true;
        }
    }

}
