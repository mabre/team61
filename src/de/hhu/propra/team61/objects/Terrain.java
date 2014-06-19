package de.hhu.propra.team61.objects;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

import static de.hhu.propra.team61.objects.Figure.*;

/**
 * A GridPane representing a terrain.
 * The class has methods for getting spawn points, walkability checks and destroying terrain
 * Created by markus on 17.05.14.
 */
public class Terrain extends GridPane {
    private static final boolean DEBUG = false;
    private static final boolean GRID_ENABLED = false;

    private final static String imgPath = "file:resources/";
    private final static int BLOCK_SIZE = 8;
    private final static Image EARTH_IMAGE = new Image(imgPath + "earth.png");
    private final static Image ICE_IMAGE = new Image(imgPath + "ice.png");
    private final static Image LAVE_IMAGE = new Image(imgPath + "lava.png");
    private final static Image SKY_IMAGE = new Image(imgPath + "sky.png");
    private final static Image SLANT_LE_IMAGE = new Image(imgPath + "slant_ground_le.png");
    private final static Image SLANT_RI_IMAGE = new Image(imgPath + "slant_ground_ri.png");
    private final static Image STONES_IMAGE = new Image(imgPath + "stones.png");
    private final static Image WATER_IMAGE = new Image(imgPath + "water.png");

    //Technical Blocks/Special Cases
    private final double RESISTANCE_OF_SKY = 15;
    private final double RESISTANCE_OF_FLUIDS = 99999999;
    //Blocks
    private final double RESISTANCE_OF_EARTH = 25;
    private final double RESISTANCE_OF_SAND  = 20;
    private final double RESISTANCE_OF_SNOW  = 20;
    private final double RESISTANCE_OF_STONE = 35;
    private final double RESISTANCE_OF_ICE = 30;
    //Modifiers
    private final double MODIFIER_FOR_SLANTS = 0.30;

    //ArrayLists
    private ArrayList<ArrayList<Character>> terrain;
    private ArrayList<Point2D> spawnPoints;
    private ArrayList<Figure> figures;

    private Point2D wind = new Point2D(0,0);
    private final static double MAX_WIND_SPEED_NORMAL = Figure.WALK_SPEED*.8;
    private final static double MAX_WIND_SPEED_HARD = Figure.WALK_SPEED*1.2;

    /**
     * @param terrain 2-D-ArrayList containing the terrain to be displayed
     */
    public Terrain(ArrayList<ArrayList<Character>> terrain) {
        load(terrain);
        figures = new ArrayList<>();
    }

    public void load(ArrayList<ArrayList<Character>> terrain) {
        getChildren().clear();

        this.terrain = terrain;

        spawnPoints = new ArrayList<Point2D>();

        //Draw Terrain
        setAlignment(Pos.TOP_LEFT);
        setGridLinesVisible(GRID_ENABLED);

        String img;

        for (int i = 0; i < terrain.size(); i++) {
            for (int j = 0; j < terrain.get(i).size(); j++) {
                char terraintype = terrain.get(i).get(j);
                switch (terraintype) {
                    case ' ':
                        add(new ImageView(SKY_IMAGE), j, i);
                        break;
                    case 'S':
                        add(new ImageView(STONES_IMAGE), j, i);
                        break;
                    case 'E':
                        add(new ImageView(EARTH_IMAGE), j, i);
                        break;
                    case 'I':
                        add(new ImageView(ICE_IMAGE), j, i);
                        break;
                    case '/':
                        add(new ImageView(SLANT_RI_IMAGE), j, i);
                        break;
                    case '\\':
                        add(new ImageView(SLANT_LE_IMAGE), j, i);
                        break;
                    case 'W':
                        add(new ImageView(WATER_IMAGE), j, i);
                        break;
                    case 'L':
                        add(new ImageView(LAVE_IMAGE), j, i);
                        break;
                    case 'P': // special case: spawn point, add to list and draw sky
                        spawnPoints.add(new Point2D(j * BLOCK_SIZE, i * BLOCK_SIZE));
                        terrain.get(i).set(j, ' ');
                    default:
                        add(new ImageView(SKY_IMAGE), j, i);
                }
                //terrainGrid.setConstraints(content,j,i);
            }
        }
    }

    /**
     * @return the 2-D-ArrayList representing the loaded terrain
     */
    public ArrayList<ArrayList<Character>> toArrayList() {
        return terrain;
    }

    /**
     * get a spawn point and remove it from the list of available spawn points
     *
     * @return a random spawn point, or null if there are no more spawn points
     */
    public Point2D getRandomSpawnPoint() {
        if (spawnPoints.isEmpty()) {
            return null;
        }
        int index = (int) (Math.random() * spawnPoints.size());
        Point2D spawnPoint = spawnPoints.get(index);
        spawnPoints.remove(index);
        System.out.println("TERRAIN: returning spawn point #" + index + " " + spawnPoint + " (" + spawnPoints.size() + " left)");
        return spawnPoint;
    }

    /**
     * @param n
     * @return n random spawn points
     * @see #getRandomSpawnPoint()
     */
    public ArrayList<Point2D> getRandomSpawnPoints(int n) {
        ArrayList<Point2D> spawnPoints = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Point2D sp = getRandomSpawnPoint();
            if (sp != null) spawnPoints.add(sp);
        }
        return spawnPoints;
    }

    /**
     * @param hitRegion of a figure or whatever
     * @param x         column of the field
     * @param y         row of the field
     * @return true, when hitRegion intersects with the field
     */
    private boolean intersects(Rectangle2D hitRegion, int x, int y) {
        char c;
        try {
            c = terrain.get(y).get(x);
        } catch (IndexOutOfBoundsException e) {
            // this means we "collided" with the end of the terrain, pretend that it is stone
            c = 'S';
        }

        switch (c) {
            case ' ':
                return false;
            case '/':
                Point2D p;
                for (int i = 0; i < BLOCK_SIZE; i++) { //ToDo Replace with BLOCK_SIZE ?
                    int px = x * BLOCK_SIZE + i;
                    int py = y * BLOCK_SIZE + BLOCK_SIZE - i;
                    p = new Point2D(px, py);
                    if(hitRegion.contains(p)) {
                        debugLog("diagonal / intersection at " + px + "x" + py + "px");
                        return true;
                    }
                }
                return false;
            case '\\':
                for(int i=0; i<BLOCK_SIZE; i++) {
                    int px = x*BLOCK_SIZE+i;
                    int py = y*BLOCK_SIZE+1+i;
                    p = new Point2D(px, py);
                    if(hitRegion.contains(p)) {
                        debugLog("diagonal / intersection at " + px + "x" + py + "px");
                        return true;
                    }
                }
                return false;
            default:
                Rectangle2D rec = new Rectangle2D(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                return hitRegion.intersects(rec);
        }
    }

    /**
     * creates new random wind // TODO option?
     */
    public void rewind() {
        double maxWindSpeed = MAX_WIND_SPEED_NORMAL; // TODO add option
        double windSpeed = Math.random() * maxWindSpeed - maxWindSpeed / 2;
        if (Math.random() > .5) windSpeed *= 2; // make higher speed less probable
        wind = new Point2D(windSpeed, 0);
        System.out.println("new wind: " + windSpeed);
    }

    /**
     * @return the magnitude of the current wind vector, the sign indicates wind direction
     */
    public double getWindMagnitude() {
        return wind.magnitude()*Math.signum(wind.getX());
    }

    /**
     * Sets the wind to the given value
     * @param wind wind speed in x direction
     */
    public void setWind(double wind) {
        this.wind = new Point2D(wind, 0);
    }

    private static void debugLog(String msg) {
        if(DEBUG) System.out.println(msg);
    }

    /**
     * adds direction to oldPosition, but assures that we do not walk/fly through terrain or other figures
     * When canWalkAlongDiagonals is true, the movement continues at slopes in diagonal direction (used for figures);
     * otherwise, the movement is stopped (used for projectiles)
     *
     * @param oldPosition           old position of the object
     * @param direction             direction vector of the object
     * @param hitRegion             a rectangle describing the area where the object can collide with terrain etc.
     * @param canWalkAlongDiagonals when true, the object is moved along diagonal walls
     * @param canWalkThroughFigures when true, the object is able to walk through figures (no CollisionWithFigureException will be thrown) TODO therefore, have a wrapper function which does not throw this exception
     * @param snapToPx when true, the positions returned are rounded to whole px
     * @return new position of the object
     * @throws CollisionException thrown when hitting terrain or a figure
     */
    public Point2D getPositionForDirection(Point2D oldPosition, Point2D direction, Rectangle2D hitRegion, boolean canWalkAlongDiagonals, boolean canWalkThroughFigures, boolean snapToPx) throws CollisionException {
        Point2D newPosition = new Point2D(oldPosition.getX(), oldPosition.getY());
        if(!isInWindbreak(oldPosition, direction)) direction = direction.add(wind);
        Point2D normalizedDirection = direction.normalize();

        debugLog("start position: " + oldPosition);
        debugLog("normalized velocity: " + normalizedDirection);

        final int runs = (int) direction.magnitude();

        for (int i = 0; i < runs; i++) {
            // move position by 1px
            newPosition = newPosition.add(normalizedDirection);

            // calculate moved hitRegion
            hitRegion = new Rectangle2D(hitRegion.getMinX() + normalizedDirection.getX(), hitRegion.getMinY() + normalizedDirection.getY(), hitRegion.getWidth(), hitRegion.getHeight());

            debugLog("checking new position for collision: " + newPosition + " (" + (i + 1) + "/" + runs + ")" + " " + hitRegion);

            // check if hitRegion intersects with non-walkable terrain
            boolean triedDiagonal = false;
            int tries = 0;
            Point2D diagonalDirection = new Point2D(0, 0);
            do { // while(triedDiagonal && ++tries<2)
                triedDiagonal = false;

                // calculate indices of fields which are touched by hitRegion
                int minY = (int) Math.floor(hitRegion.getMinY() / BLOCK_SIZE);
                int maxY = (int) Math.ceil(hitRegion.getMaxY() / BLOCK_SIZE);
                int minX = (int) Math.floor(hitRegion.getMinX() / BLOCK_SIZE);
                int maxX = (int) Math.ceil(hitRegion.getMaxX() / BLOCK_SIZE);

                for (int y = minY; y <= maxY && !triedDiagonal; y++) { // TODO recheck necessity of <=
                    for (int x = minX; x <= maxX && !triedDiagonal; x++) {
                        //debugLog(hitRegion + " " + terrain.get(y).get(x) + " field: " + rec);
                        boolean intersects = intersects(hitRegion, x, y);
                        Figure intersectingFigure = null;
                        if (!canWalkThroughFigures && !intersects) {
                            for (Figure figure : figures) {
                                if (hitRegion.intersects(figure.getHitRegion())) {
                                    intersects = true;
                                    intersectingFigure = figure;
                                }
                            }
                        }
                        if (intersects) {
                            try {
                                debugLog("intersection at " + x + " " + y + " with " + terrain.get(y).get(x));
                                if (intersectingFigure != null)
                                    debugLog("intersecting with " + intersectingFigure.getName() + " at " + intersectingFigure.getPosition());
                            } catch (IndexOutOfBoundsException e) {
                                debugLog("intersection at " + x + " " + y + " out of bounds");
                            }
                            if (canWalkAlongDiagonals && tries == 0 && intersectingFigure == null) {
                                diagonalDirection = new Point2D(Math.signum(normalizedDirection.getX())/12, -1.5);
                                Point2D positionOnSlope = newPosition.subtract(normalizedDirection).add(diagonalDirection);
                                hitRegion = new Rectangle2D(hitRegion.getMinX() - normalizedDirection.getX() + diagonalDirection.getX(), hitRegion.getMinY() - normalizedDirection.getY() + diagonalDirection.getY(), hitRegion.getWidth(), hitRegion.getHeight());
                                newPosition = positionOnSlope;
                                triedDiagonal = true;
                                debugLog("trying to walk diagonal along " + diagonalDirection + " to " + newPosition + " " + hitRegion);
                            } else {
                                Point2D collidingPosition = newPosition;
                                if (diagonalDirection.magnitude() == 0) { // did not go diagonal
                                    newPosition = newPosition.subtract(normalizedDirection);
                                } else {
                                    newPosition = newPosition.subtract(diagonalDirection);
                                }
                                if (snapToPx) {
                                    newPosition = new Point2D(Math.floor(newPosition.getX()), Math.ceil(newPosition.getY())); // TODO code duplication
                                }
                                if(intersectingFigure == null) {
                                    throw new CollisionException("terrain", collidingPosition, newPosition);
                                } else {
                                    throw new CollisionException("figure",collidingPosition, newPosition);
                                }
                            }
                        }
                    }
                } // for each field
            } while (triedDiagonal && ++tries < 2);
        } // for each run

        if (snapToPx) {
            newPosition = new Point2D(Math.floor(newPosition.getX()), Math.ceil(newPosition.getY())); // TODO code duplication
        }
        return newPosition;
    }

    /**
     * @param position the position to be checked
     * @param direction we do not apply wind to vertical movements near the ground (makes jumping easier)
     * @return true when the given position is not influenced by wind (ie. is right behind a piece of terrain)
     */
    private boolean isInWindbreak(Point2D position, Point2D direction) {
        if(wind.getX() == 0) return true;

        boolean movingVertical = (direction.getX()==0);

        int minY, maxY, minX, maxX;
        minY = (int) Math.floor(position.getY() / BLOCK_SIZE - 1);
        maxY = (int) Math.floor((position.getY() + Figure.NORMED_OBJECT_SIZE) / BLOCK_SIZE - 1);
        if(movingVertical) maxY += (int) Math.floor(Figure.JUMP_SPEED / BLOCK_SIZE);
        if(wind.getX() > 0) {
            minX = (int) Math.floor(position.getX() / BLOCK_SIZE - 1);
            maxX = (int) Math.floor(position.getX() / BLOCK_SIZE);
        } else {
            minX = (int) Math.floor((position.getX() + Figure.NORMED_OBJECT_SIZE) / BLOCK_SIZE);
            maxX = (int) Math.floor((position.getX() + Figure.NORMED_OBJECT_SIZE) / BLOCK_SIZE + 1);
        }

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                try {
                    if (terrain.get(y).get(x) != ' ') {
                        return true;
                    }
                } catch (IndexOutOfBoundsException e) {
                    return true;
                }
            }
        }

        System.out.println("not covered");
        return false;
    }

    public void addFigures(ArrayList<Figure> figures) {
        this.figures.addAll(figures);
    }

    private double getResistance(int x, int y) {
        char block = terrain.get(y).get(x);
        switch (block) {
            case ' ': return RESISTANCE_OF_SKY;
            case 'W':
            case 'L': return RESISTANCE_OF_FLUIDS;
            case '/':
            case '\\'://Slants are depending on blocks below
                      if(terrain.size() > y + 1){ return getResistance(x,y + 1) * MODIFIER_FOR_SLANTS; }
                      else{ return RESISTANCE_OF_SKY; } // Return an at least somewhat useful information
            case 'S': return RESISTANCE_OF_STONE;
            case 'E': return RESISTANCE_OF_EARTH;
            case 'I': return RESISTANCE_OF_ICE;
            case 'A': //ToDo change that
                return RESISTANCE_OF_SNOW;
            case 'B': //ToDo change that
                return RESISTANCE_OF_SAND;
            default: return RESISTANCE_OF_SKY;
        }
    }

    public void replaceBlock(int blockX, int blockY, char replacement){
        terrain.set(blockY,terrain.get(blockY)).set(blockX,replacement);
    }

    /**
     * This function actually calculates the destroyed blocks recursively.
     * First the Explosion expands to all directions depending on left explosionPower leaving out
     * already destroyed blocks.
     *
     * @param commands ArrayList<String> of Commands to be executed on clients
     * @param blockX Used to move through terrain, which is a grid
     * @param blockY Used to move through terrain, which is a grid
     * @param explosionPower value to determine (int)if block (blockX,blockY) is destroyed
     */
    private void explode(ArrayList<String> commands, int blockX, int blockY, int explosionPower){
        final char destroyed = '#';
        char replacement = destroyed;

        if (explosionPower > 0 && terrain.get(blockY).get(blockX) != '#') { //else abort recursion
            double resistanceOfBlock = getResistance(blockX,blockY);

            //Calc behaviour for current Block
            if (explosionPower >= resistanceOfBlock) { // Enough destructive force

                //Print Debugging-MSG to console:
                //System.out.println("Explosion of: \"" + terrain.get(blockY).get(blockX) + "\" (" + blockX + " " + blockY + ")" + "Resistance: " + resistanceOfBlock + "; " + "Explosionpower: " + explosionPower);


                explosionPower -= resistanceOfBlock; //Reduce explosionPower
                replaceBlock(blockX,blockY,replacement); //Mark as destroyed

                // Recursively continue destruction for all directions unless OutOfBounds
                if (blockY < terrain.size()){  explode(commands, blockX, blockY + 1, explosionPower); }

                if (blockX > 0) { explode(commands, blockX - 1, blockY, explosionPower); }
                if (blockX < terrain.get(blockY).size()) {  explode(commands, blockX + 1, blockY, explosionPower); }

                if (blockY > 0) { explode(commands, blockX, blockY-1,explosionPower); }

                // Add destruction of actual Block to commandlist
                commands.add("REPLACE_BLOCK " + blockX + " " + blockY + " " + replacement);// ' ' is impossible due to the Client/Server-MSG-System

            } else {
                resistanceOfBlock = getResistance(blockX,blockY); // Check if partially enough destructive Force
                if(explosionPower > resistanceOfBlock * MODIFIER_FOR_SLANTS && resistanceOfBlock != RESISTANCE_OF_SKY){ // BUT do not create slants out of air

                    //Print Debugging-MSG to console:
                    //System.out.println("now a Slant: \"" + terrain.get(blockY).get(blockX) + "\" (" + blockX + " " + blockY + ")" + "Resistance: " + resistanceOfBlock + "; " + "Explosionpower: " + explosionPower);


                    if(blockX > 0 && blockX < terrain.get(blockY).size()){
                        if(terrain.get(blockY).get(blockX-1) != '#' && terrain.get(blockY).get(blockX-1) != ' '){
                            commands.add("REPLACE_BLOCK " + blockX + " " + blockY + " " + '\\');
                        } else {
                            if(terrain.get(blockY).get(blockX+1) != '#' && terrain.get(blockY).get(blockX+1) != ' '){
                                commands.add("REPLACE_BLOCK " + blockX + " " + blockY + " " + '/');
                            }
                        }
                    }
                }
            }
        }//recursion
    }//explode()

    /**
     *
     * @param impactPoint
     * @param explosionPower
     */
    public ArrayList<String> handleExplosion(Point2D impactPoint, int explosionPower) {
        // Get Block, which is center of explosion, from Point2D
        int blockX = (int)impactPoint.getX() / BLOCK_SIZE;
        int blockY = (int)impactPoint.getY() / BLOCK_SIZE;
        
        ArrayList<String> commands = new ArrayList<String>();
        explode(commands,blockX,blockY,explosionPower); //Recursive Function, actual handling in here, adds commands to the arraylist
        commands.add("RELOAD_TERRAIN"); //Tell Clients to update Map for visibility;

        return commands;
    }
}
