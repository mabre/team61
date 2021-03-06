package de.hhu.propra.team61.artificialIntelligence;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.objects.*;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;

/**
 * Implementation of an AI with difficulty "simple".
 * See {@link #prepareAim(java.util.ArrayList)} for a basic description of the strategy.
 */
public class SimpleAI extends ArtificialIntelligence {

    /** medipack is used when hp drops below this value */
    private static int USE_MEDIPACK_THRESHOLD = 40;
    /** digiwise is used when hp drops below this value, with a probability of {@link #USE_DIGIWISE_PROBABILITY} */
    private static int USE_DIGIWISE_THRESHOLD = 60;
    /** probability for digiwise usage, see also {@link #USE_DIGIWISE_THRESHOLD} */
    private static double USE_DIGIWISE_PROBABILITY = .2;
    /** width of a projectile, used for terrain collision calculations */
    private static int PROJECTILE_WIDTH = 4;

    /** reference to the being moved in the current turn */
    private Figure currentFigure;
    /** position of the center of {@link #currentFigure} */
    Point2D currentFigurePosition;

    /** reference to the crate closest to {@link #currentFigure} */
    Crate closestCrate;
    /** position of the center of {@link #closestCrate} */
    Point2D closestCratePosition;

    /** reference to the enemy figure the AI wants to hit (not necessarily the closest figure) */
    Figure closestEnemy;
    /** position of the center of {@link #closestEnemy} */
    Point2D closestEnemyPosition;

    /** angle between {@link #currentFigure} and {@link #closestEnemy} in degrees to the horizontal [0, 90] */
    double angleToEnemy;
    /** whether {@link #angleToEnemy} is below or above horizontal */
    boolean angleDown;
    /** distance to {@link #closestEnemy} in px */
    double distanceToEnemy;
    /** counter for how many steps have been made during the current turn */
    int repetition = 0;

    public SimpleAI(Team ownTeam, ArrayList<Team> teams, Terrain terrain, ArrayList<Crate> crates, JSONObject gameSettings) {
        super(ownTeam, teams, terrain, crates, gameSettings);
    }

    /**
     * Let the AI calculate the next (partial) move.
     * @return List of commands, empty if turn is finished
     */
    @Override
    public ArrayList<String> makeMove() {
        ArrayList<String> commands = new ArrayList<>();

        switch(state) {
            case NEW_TURN:
                findCrate();
            case COLLECTING_CRATE:
                collectCrate(commands);
                break;
            case CRATE_COLLECTED:
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
            commands.add(ownTeam.getNumber() + " NOOP");
        }

        return commands;
    }

    @Override
    public AIType getAIType() {
        return AIType.SIMPLE;
    }

    /**
     * Updates {@link #currentFigure} and {@link #currentFigurePosition}.
     */
    private void updateCurrentFigure() {
        currentFigure = ownTeam.getCurrentFigure();
        currentFigurePosition = currentFigure.getPosition();
        currentFigurePosition = getCenterForNormedObject(currentFigurePosition);
    }

    /**
     * Updates {@link #currentFigure}, {@link #currentFigurePosition}, and sets {@link #closestEnemy}, {@link #closestEnemyPosition}, and {@link #distanceToEnemy} to the n-th nearest enemy figure next to {@link #currentFigure}.
     * If n is less than the number of enemies, the nearest enemy is chosen. If no enemy is found, {@link #closestEnemy}
     * is set to null, and {@link #closestEnemyPosition} is not changed.
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

    /**
     * Uses a medipack and/or digiwise when certain conditions are met.
     * @param commands reference to the list of commands returned to the game loop
     */
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
        // use digiwise randomly when having enough hp, or being the last figure of the team
        if (ownTeam.getItem(7 - 1).getMunition() > 0 && !currentFigure.isDigitated()) {
            if ((currentFigure.getHealth() > USE_DIGIWISE_THRESHOLD && Math.random() < USE_DIGIWISE_PROBABILITY) ||
                    ownTeam.getNumberOfLivingFigures() == 1) {
                commands.add(ownTeam.getNumber() + " 7");
                commands.add(ownTeam.getNumber() + " Space");
            }
        }
    }

    /**
     * Adds commands for moving the crosshair up/down, according to {@link #angleToEnemy}.
     * The command for choosing a weapon must already be sent, the initial position should be horizontal, the figure
     * must be looking into the right direction.
     * @param commands reference to the list of commands returned to the game loop
     */
    private void aim(ArrayList<String> commands) {
        for(int i = (int)Math.round(angleToEnemy/Weapon.ANGLE_STEP); i > 0; i--) {
            if(angleDown) {
                commands.add(ownTeam.getNumber() + " Down");
            } else {
                commands.add(ownTeam.getNumber() + " Up");
            }
        }
        state = AIState.TARGET_AIMED;
    }

    /**
     * Calls {@link #updateCurrentFigure()}, updates {@link #closestCrate} and {@link #closestCratePosition}, and sets {@link #state} to {@link de.hhu.propra.team61.artificialIntelligence.ArtificialIntelligence.AIState#CRATE_COLLECTED}.
     * Only crates within small distance to the current figures are found. If no suitable crate is found, {@link #closestCrate}
     * is set to {@code null}.
     */
    private void findCrate() {
        updateCurrentFigure();

        for(Crate crate: crates) {
            Point2D cratePosition = getCenterForNormedObject(crate.getPosition());
            if(cratePosition.distance(currentFigurePosition) < Figure.NORMED_OBJECT_SIZE*1.6 &&
                    Math.acos((cratePosition.getY() - currentFigurePosition.getY()) / currentFigurePosition.distance(cratePosition)) > 1.3 && // do not risk falling down or the like
                    !terrain.standingOnLiquid(crate.getPosition())) {
                closestCrate = crate;
                closestCratePosition = cratePosition;
                state = AIState.COLLECTING_CRATE;
                return;
            }
        }

        closestCrate = null;
        state = AIState.CRATE_COLLECTED;
    }

    /**
     * Adds commands for moving to the closest crate, if any.
     * If there is no closest crate, are the crate has been hit, {@link #state} is set to {@link de.hhu.propra.team61.artificialIntelligence.ArtificialIntelligence.AIState#CRATE_COLLECTED}.
     * Otherwise, one movement command is added.
     * @param commands reference to the list of commands returned to the game loop
     * @see #findCrate()
     */
    private void collectCrate(ArrayList<String> commands) {
        if(closestCrate == null) {
            state = AIState.CRATE_COLLECTED;
            return;
        }

        updateCurrentFigure();

        if(Math.abs(closestCratePosition.getX() - currentFigurePosition.getX()) < Figure.NORMED_OBJECT_SIZE*.8) {
            state = AIState.CRATE_COLLECTED;
        } else if(closestCratePosition.getX() < currentFigurePosition.getX()) {
            commands.add(ownTeam.getNumber() + " Left");
        } else {
            commands.add(ownTeam.getNumber() + " Right");
        }
    }

    /**
     * Does all the calculation for hitting an enemy.
     * <ol>
     *     <li>call {@link #updateCurrentFigure()}</li>
     *     <li>check if the 8 closest enemies can be hit from the current position</li>
     *     <li>if no hit is possible, move to an enemy if there is enought time (and return from function without changing {@link #state})</li>
     *     <li>call {@link #useItem(java.util.ArrayList)}</li>
     *     <li>make the figure look in the right direction</li>
     *     <li>choose an approriate weapon (considering distance and wind)</li>
     *     <li>add corrections to the crosshair angle depending on weapon mass</li>
     *     <li>set {@link #state} to {@link de.hhu.propra.team61.artificialIntelligence.ArtificialIntelligence.AIState#TARGET_FACED}</li>
     * </ol>
     * @param commands reference to the list of commands returned to the game loop
     */
    private void prepareAim(ArrayList<String> commands) {
        updateCurrentFigure();

        // try hitting one of the next 8 enemies, fall back to 1st one if no hit is possible
        boolean foundEnemy = false;
        boolean hittingFriend = false;
        boolean firstHittingFriend = false;
        boolean hittingNearbyTerrain = false;
        for (int i = 0; i < 8 && !foundEnemy; i++) {
            chooseEnemy(i);

            if (closestEnemy == null) break;

            try {
                hittingFriend = false;
                Point2D crosshairOffset = getCrosshairOffset(); // TODO also check slightly higher
                System.out.println(crosshairOffset);
                System.out.println("standing at" + currentFigurePosition + ", aiming at " + closestEnemy.getName() + " " + closestEnemyPosition);
                System.out.println("angle " + angleToEnemy + ", steps " + Math.round(angleToEnemy / Weapon.ANGLE_STEP));
                terrain.getPositionForDirection(currentFigurePosition.add(crosshairOffset), closestEnemyPosition.subtract(currentFigurePosition), new Rectangle2D(currentFigurePosition.getX() + crosshairOffset.getX(), currentFigurePosition.getY() + crosshairOffset.getY(), PROJECTILE_WIDTH, PROJECTILE_WIDTH), false, false, false, false);
                System.err.println((i + 1) + "st enemy good?");
                foundEnemy = true;
                hittingNearbyTerrain = false;
            } catch (CollisionException e) {
                if (!e.getCollisionPartnerClass().equals("figure")) {
                    System.out.println((i + 1) + "st enemy not good");
                    hittingNearbyTerrain = (e.getLastGoodPosition().distance(currentFigurePosition) < Figure.NORMED_OBJECT_SIZE*2);
                } else {
                    System.out.println((i + 1) + "st enemy is good (hit)");
                    hittingNearbyTerrain = false;
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
            Point2D newGoToPos = currentFigurePosition.subtract(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);
            Point2D goToPos = null;
            for (int j = 0; j < 2 && newGoToPos != null && !terrain.standingOnLiquid(newGoToPos) && terrain.standingOnGround(newGoToPos)
                    && !facingWall(newGoToPos, goingLeft) && (newGoToPos.distance(closestEnemyPosition) > 150 || firstHittingFriend || hittingNearbyTerrain); j++) {
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

        if(hittingFriend || closestEnemy == null) {
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
        boolean shootingHigherIsGood = false;
        if(!hittingNearbyTerrain) {
            if (distanceToEnemy < 100) {
                double grenadeProbability = ((closestEnemy.getHealth() <= 40 && closestEnemy.getHealth() >= 20) ? .8 : .4);
                weaponNumber = (Math.random() < grenadeProbability ? 2 : 3); // grenade or shotgun
            } else if (distanceToEnemy > 140 && distanceToEnemy < 200 && !(angleToEnemy > 45 && !angleDown) // do not aim up (probably won't work)
                    && Math.random() < .5 && ownTeam.getItem(6 - 1).getMunition() > 0
                    && (terrain.getWindMagnitude() < 2.5 || terrain.getWindMagnitude() * (closestEnemyPosition.getX() - currentFigurePosition.getX()) >= 0)) { // no bananbomb into strong wind
                weaponNumber = 6; // banana bomb
            } else if (distanceToEnemy > 250 && distanceToEnemy <= 400) {
                weaponNumber = (Math.random() < .5 ? 3 : 4); // shotgun or rifle
            } else if (distanceToEnemy > 400) {
                weaponNumber = (Math.random() < .1 ? 3 : 4); // shotgun or rifle
            } else {
                double bazookaProbability = ((closestEnemy.getHealth() <= 50 && closestEnemy.getHealth() >= 20) ? .8 : .4);
                weaponNumber = (Math.random() < bazookaProbability ? 1 : 3); // bazooka or shotgun
            }
        } else {
            // hitting nearby terrain; try throwing grenade or bananabomb
            try { // TODO not working reliably (have seen it working when enemy on right)
                Point2D crosshairOffset = getCrosshairOffset();
                terrain.getPositionForDirection(currentFigurePosition.add(crosshairOffset).subtract(0,Figure.NORMED_OBJECT_SIZE), closestEnemyPosition.subtract(currentFigurePosition).subtract(0, Figure.NORMED_OBJECT_SIZE * 2), new Rectangle2D(currentFigurePosition.getX() + crosshairOffset.getX(), currentFigurePosition.getY() + crosshairOffset.getY(), PROJECTILE_WIDTH, PROJECTILE_WIDTH), false, false, false, false);
                shootingHigherIsGood = true;
            } catch (CollisionException e) {
                if (e.getCollisionPartnerClass().equals("figure")) {
                    shootingHigherIsGood = true;
                }
            }
            if(shootingHigherIsGood) {
                if (distanceToEnemy <= 120) {
                    weaponNumber = 2;
                } else if (distanceToEnemy > 120 && distanceToEnemy < 200 && !(angleToEnemy > 45 && !angleDown) // do not aim up (probably won't work)
                        && ownTeam.getItem(6 - 1).getMunition() > 0
                        && (terrain.getWindMagnitude() < 2.5 || terrain.getWindMagnitude() * (closestEnemyPosition.getX() - currentFigurePosition.getX()) >= 0)) { // no bananbomb into strong wind
                    weaponNumber = 6; // banana bomb
                } else {
                    weaponNumber = 9;
                }
            } else {
                weaponNumber = 9;
            }
        }

        if(weaponNumber == 9 || closestEnemy == null) {
            closestEnemy = null;
            angleToEnemy = 0;
            commands.add(ownTeam.getNumber() + " 9");
            state = AIState.TARGET_FACED;
            return;
        }

        if(ownTeam.getItem(weaponNumber-1).getMunition() <= 0) {
            weaponNumber = 3; // has infinite munition
        }

        commands.add(ownTeam.getNumber() + " " + weaponNumber);

        // aim
        System.out.println("standing at" + currentFigurePosition + ", aiming at " + closestEnemy.getName() + " " + closestEnemyPosition);
        System.out.println("angle " + angleToEnemy + ", steps " + Math.round(angleToEnemy/Weapon.ANGLE_STEP));

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
        if (shootingHigherIsGood) {
            commands.add(ownTeam.getNumber() + " Up");
        }
    }

    /**
     * Calculates the vector between {@link #currentFigure} and the projectile spawn point when directly aiming at {@link #closestEnemy}.
     * @return vector between current figure's center and projectile spawn point when crosshair points at chosen enemy
     */
    private Point2D getCrosshairOffset() {
        return new Point2D((closestEnemyPosition.getX() < currentFigurePosition.getX() ? -1 : 1) * (Math.cos(Math.toRadians(angleToEnemy)) * Figure.NORMED_OBJECT_SIZE - PROJECTILE_WIDTH/2),
                (closestEnemyPosition.getY() < currentFigurePosition.getY() ? -1 : 1) * (Math.sin(Math.toRadians(angleToEnemy)) * Figure.NORMED_OBJECT_SIZE - PROJECTILE_WIDTH/2));
    }

}
