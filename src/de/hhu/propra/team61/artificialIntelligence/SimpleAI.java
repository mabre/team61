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

    public SimpleAI(Team ownTeam, ArrayList<Team> teams, Terrain terrain, ArrayList<Crate> crates, JSONObject gameSettings) {
        super(ownTeam, teams, terrain, crates, gameSettings);
    }

    public ArrayList<String> makeMove() {
        ArrayList<String> commands = new ArrayList<>();

        Figure currentFigure = ownTeam.getCurrentFigure();
        Point2D currentFigurePosition = currentFigure.getPosition();
        currentFigurePosition = currentFigurePosition.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);

        Figure closestEnemy = getEnemiesByDistance().get(0); // TODO IMPORTANT index check
        Point2D closestEnemyPosition = closestEnemy.getPosition();
        closestEnemyPosition = closestEnemyPosition.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);

        double angleToEnemy = Math.toDegrees(Math.acos((closestEnemyPosition.getY()-currentFigurePosition.getY()) / currentFigurePosition.distance(closestEnemyPosition)));
        angleToEnemy = Math.abs(90 - angleToEnemy); // we want to have the angle to the horizontal
        boolean angleDown = (closestEnemyPosition.getY() > currentFigurePosition.getY());

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

                closestEnemy = getEnemiesByDistance().get(1); // TODO IMPORTANT index check; code duplication
                closestEnemyPosition = closestEnemy.getPosition();
                closestEnemyPosition = closestEnemyPosition.add(Figure.NORMED_OBJECT_SIZE / 2, Figure.NORMED_OBJECT_SIZE / 2);

                angleToEnemy = Math.toDegrees(Math.acos((closestEnemyPosition.getY()-currentFigurePosition.getY()) / currentFigurePosition.distance(closestEnemyPosition)));
                angleToEnemy = Math.abs(90 - angleToEnemy); // we want to have the angle to the horizontal
                angleDown = (closestEnemyPosition.getY() > currentFigurePosition.getY());
            } else {
                System.err.println("first enemy is good (hit)");
            }
        }

        // turn to enemy
        if(closestEnemyPosition.getX() < currentFigurePosition.getX()) {
            commands.add(ownTeam.getNumber() + " Left");
        } else {
            commands.add(ownTeam.getNumber() + " Right");
        }

        // choose weapon
        commands.add(ownTeam.getNumber() + " " + ((int)(Math.random()*4)+1));

        // aim
        System.out.println("standing at" + currentFigurePosition + ", aiming at " + closestEnemy.getName() + " " + closestEnemyPosition);
        System.out.println("angle " + angleToEnemy + ", steps " + (int)(angleToEnemy/Weapon.ANGLE_STEP));
        for(int i = (int)(angleToEnemy/Weapon.ANGLE_STEP); i > 0; i--) {
            if(angleDown) {
                commands.add(ownTeam.getNumber() + " Down");
            } else {
                commands.add(ownTeam.getNumber() + " Up");
            }
        }

        // shoot
        commands.add(ownTeam.getNumber() + " Space");
        return commands;
    }

    private ArrayList<Figure> getEnemiesByDistance() {
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

}
