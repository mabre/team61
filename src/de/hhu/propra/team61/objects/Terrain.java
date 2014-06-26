package de.hhu.propra.team61.objects; // TODO create own subpackage (after merging, otherwise resolving conflicts might become hard)

import de.hhu.propra.team61.animation.SpriteAnimation;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.util.ArrayList;

/**
 * A GridPane representing a terrain.
 * <p>
 * Use {@link #Terrain(JSONObject)} to load a new terrain. A valid json string representing a terrain can look like this:
 * <pre>
 * {@code
 * {
 *   "background": "cave.png",
 *   "music": "like_cave.ogg",
 *   "terrain": [
 *     "                                        ",
 *     "    P      P       P      P      P      ",
 *     "                                        ",
 *     "SSSSSSSSSSSSEE\\WWWWWWWWWWWWWWW/EESSSSSSS",
 *   ]
 * }
 * }
 * </pre>
 * Only the {@code terrain} property is mandatory. If you also want to render spawn points (eg. in an editor), you can
 * use the alternative {@link #Terrain(JSONObject, boolean)} constructor.
 * <p>
 * This class provides methods for manipulating terrain, getting spawn points, and collision handling.
 */
public class Terrain extends GridPane {
    /** set to {@code true} to get very verbose debug output from the collision handling methods */
    private static final boolean DEBUG = false;
    /** set to {@code true} to true to see the terrain grid */
    private static final boolean GRID_ENABLED = false;
    /** the size of a block within the terrain; you usually do NOT want to use this outside this class (notable exception: level editor) */
    final static int BLOCK_SIZE = 8;

    /** hold the image for the destroy animation of the sudden death type "boss" */
    private static Image RIFT_IMAGE = new Image("file:resources/animations/boss_rift.png");

    //ArrayLists
    /** a list containing the terrain blocks which are rendered, [row][column], counting starts from [0][0] in the top left corner */
    private ArrayList<ArrayList<TerrainBlock>> terrain;
    /** a list of unused spawn points */
    private ArrayList<Point2D> spawnPoints;
    /** a list of figures with which objects can collide */
    private ArrayList<Figure> figures;

    /** a vector representing wind force and direction */
    private Point2D wind = new Point2D(0,0);
    /** maximum wind speed for difficulty "easy" */
    private final static double MAX_WIND_SPEED_EASY = Figure.WALK_SPEED*.6;
    /** maximum wind speed for difficulty "normal" */
    private final static double MAX_WIND_SPEED_NORMAL = Figure.WALK_SPEED*.8;
    /** maximum wind speed for difficulty "hard" */
    private final static double MAX_WIND_SPEED_HARD = Figure.WALK_SPEED*1.2;

    /**
     * Creates a new terrain from the given JSONObject.
     * @param terrain a JSONObject containing the level whose terrain shall be displayed
     */
    public Terrain(JSONObject terrain) {
        this(terrain, false);
    }

    /**
     * Creates a new terrain from the given JSONObject, and allows rendering the spawn points.
     * @param terrain a JSONObject containing the level whose terrain shall be displayed
     * @param renderSpawnPoints if true, the spawn points are rendered (used by level editor)
     */
    public Terrain(JSONObject terrain, boolean renderSpawnPoints) {
        load(terrain, renderSpawnPoints);
        figures = new ArrayList<>();
    }

    /**
     * Loads the given terrain into the {@link #terrain} list.
     * @param terrainObject a JSONObject representing a level
     * @param renderSpawnPoints if true, the spawn points are rendered (used by level editor)
     */
    private void load(JSONObject terrainObject, boolean renderSpawnPoints) {
        getChildren().clear();
        JSONArray terrainAsJSON = terrainObject.getJSONArray("terrain");
        this.terrain = new ArrayList<>();
        spawnPoints = new ArrayList<>();
        for (int row = 0; row < terrainAsJSON.length(); row++) {
            this.terrain.add(new ArrayList<>());
            final int length = terrainAsJSON.getString(row).length();
            for (int column = 0; column < length; column++) {
                char c = terrainAsJSON.getString(row).charAt(column);
                TerrainBlock block = new TerrainBlock(c, column, row);
                if(row != 0) {
                    block.setTopNeighbour(terrain.get(row-1).get(column));
                    terrain.get(row-1).get(column).setBottomNeighbour(block);
                }
                if(column != 0) {
                    block.setLeftNeighbour(terrain.get(row).get(column-1));
                    terrain.get(row).get(column-1).setRightNeighbour(block);
                }
                this.terrain.get(row).add(column, block);
                this.add(block, column, row);
                if(c == 'P') { // special case: spawn point, add to list
                    spawnPoints.add(new Point2D(column * BLOCK_SIZE, row * BLOCK_SIZE));
                    if(renderSpawnPoints) block.setType('P'); // renders the spawn point
                }
            }
        }
        setAlignment(Pos.TOP_LEFT);
        setGridLinesVisible(GRID_ENABLED);
    }

    /**
     * Gets a 2D ArrayList of characters which represents the currently loaded terrain [row][column], including any destruction.
     * @return a 2D ArrayList representing the loaded terrain
     */
    public ArrayList<ArrayList<Character>> toArrayList() {
        ArrayList<ArrayList<Character>> rows = new ArrayList<>();
        for(ArrayList<TerrainBlock> row: terrain) {
            ArrayList<Character> columns = new ArrayList<>();
            for(TerrainBlock column: row) {
                columns.add(column.getType());
            }
            rows.add(columns);
        }
        return rows;
    }

    /**
     * Get a spawn point and remove it from the list of available spawn points.
     * @return a random spawn point, or null if there are no more spawn points
     * @see #getRandomSpawnPoints(int)
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
     * Get up to n spawn points and remove them from the list of available spawn points.
     * @param n number of spawn points to be returned (might be less)
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
     * creates new random wind
     * <p>
     * The maximum wind force depends on the settings chosen by the user. (TODO actually not) Lower wind forces are more
     * probable.
     * </p>
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
     * Sets the wind to the given value.
     * @param wind wind speed in x direction
     */
    public void setWind(double wind) {
        this.wind = new Point2D(wind, 0);
    }

    /**
     * Prints a message to stdout if (@link DEBUG} is set to {@code true}.
     * @param msg the message to be printed
     */
    static void debugLog(String msg) {
        if(DEBUG) System.out.println(msg);
    }

    /**
     * Use this function to calculate the final position of a collision-aware movement.
     * <p>
     * This function adds the given direction to the given old (ie. current) position, but assures that the object to be
     * moved does not walk/fly through terrain or other figures. When {@code canWalkAlongDiagonals} is {@code true}, the
     * movement continues at slopes in diagonal direction (used for figures); otherwise, the movement is stopped (used
     * for projectiles).
     * <p>
     * example:<br>
     * Figure f at {@code (0,4)} wants to go to {@code (2,4)}, it can walk up slopes, go through other figures, and the movement is
     * influenced by the wind:<br>
     * {@code getPositionForDirection(f.getPosition(), new Point2D(2,0), f.getCollisionArea(), true, true, true)}<br>
     *
     * @param oldPosition old position of the object
     * @param direction direction vector of the object
     * @param hitRegion a rectangle describing the area where the object can collide with terrain etc.
     * @param canWalkAlongDiagonals when true, the object is moved along diagonal walls
     * @param canWalkThroughFigures when true, the object is able to walk through figures
     * @param snapToPx when true, the position returned is rounded to whole px (needed by figures to really land on ground)
     * @param influencedByWind if false, the wind is not considered when calculating the new position
     * @return new position of the object
     * @throws CollisionException thrown when hitting terrain or a figure
     * @see CollisionException
     */
    public Point2D getPositionForDirection(final Point2D oldPosition, final Point2D direction, Rectangle2D hitRegion, boolean canWalkAlongDiagonals, boolean canWalkThroughFigures, boolean snapToPx, boolean influencedByWind) throws CollisionException {
        Point2D newPosition = new Point2D(oldPosition.getX(), oldPosition.getY());
        Point2D directionAfterWind = direction;
        if(influencedByWind && !isInWindbreak(oldPosition, direction)) directionAfterWind = direction.add(wind);
        Point2D directionAfterFriction = directionAfterWind.multiply(getFriction(oldPosition));
        if(directionAfterFriction.magnitude() < 1 && directionAfterWind.magnitude() > 1) {
            directionAfterFriction = directionAfterWind.normalize(); // even with friction, move at least 1 px if movement >1px requested (eg. walking on water with medium wind)
        }
        Point2D normalizedDirection = directionAfterFriction.normalize();
        debugLog("start position: " + oldPosition);
        debugLog("normalized velocity: " + normalizedDirection);

        final int runs = (int) directionAfterFriction.magnitude();

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

                for (int y = minY; y < maxY && !triedDiagonal; y++) {
                    for (int x = minX; x < maxX && !triedDiagonal; x++) {
                        //debugLog(hitRegion + " " + terrain.get(y).get(x).getType() + " field: " + rec);
                        Figure intersectingFigure = null;
                        boolean intersects = true;
                        try {
                            intersects = terrain.get(y).get(x).intersects(hitRegion);
                        } catch (IndexOutOfBoundsException e) {
                            debugLog("intersection at " + x + " " + y + " out of bounds");
                        }
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
                                debugLog("intersection at " + x + " " + y + " with " + terrain.get(y).get(x).getType());
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
     * Determines whether a figure is in a windbreak, ie. is not influenced by wind.
     * @param position the position (in px) to be checked
     * @param direction if vertical, the function returns {@code true} (makes jumping easier)
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
            if(wind.getX() < 3.5) maxX += (Figure.NORMED_OBJECT_SIZE / BLOCK_SIZE) + 1; // prevent figures from being pressed at terrain and staying in the air
        } else {
            minX = (int) Math.floor((position.getX() + Figure.NORMED_OBJECT_SIZE) / BLOCK_SIZE);
            maxX = (int) Math.floor((position.getX() + Figure.NORMED_OBJECT_SIZE) / BLOCK_SIZE + 1);
            if(wind.getX() > -3.5) minX -= (Figure.NORMED_OBJECT_SIZE / BLOCK_SIZE) + 1; // prevent figures from being pressed at terrain and staying in the air
        }

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                try {
                    if (!terrain.get(y).get(x).isSky()) {
                        return true;
                    }
                } catch (IndexOutOfBoundsException e) {
                    return true;
                }
            }
        }

        debugLog("not covered");
        return false;
    }

    /**
     * Gives the friction factor for the movement of the figure at the given position
     * example situation:
     * <pre>
     * {@code
     *   P
     *
     *  IIIII
     * }
     * </pre>
     * calling {@code getFriction(P.getPosition())} will return a factor &lt; 1 (ie. figures are quicker on ice)
     * @param pos position (in px) of an object
     * @return friction based on the block directly below the given position; returns 1 if at bottom
     */
    public double getFriction(Point2D pos) {
        int row = (int)((pos.getY()+Figure.NORMED_OBJECT_SIZE)/BLOCK_SIZE);
        int column = (int)(pos.getX()/BLOCK_SIZE);
        if(row >= terrain.size() || column > terrain.get(0).size()) return 1;
        return terrain.get(row).get(column).getFriction();
    }

    /**
     * Adds the given figures to the list of {@link figures} used for collision control.
     * @param figures the figures to be added
     */
    public void addFigures(ArrayList<Figure> figures) {
        this.figures.addAll(figures);
    }

    /**
     * Resets the type of the given block with the given type and re-renders this block
     * @param blockX the x coordinate (in blocks) of the block to be changed
     * @param blockY the x coordinate (in blocks) of the block to be changed
     * @param replacement the new type of terrain
     * @see TerrainBlock
     */
    public void replaceBlock(int blockX, int blockY, char replacement) {
        terrain.get(blockY).get(blockX).setType(replacement);
    }

    /**
     * This function actually calculates the destroyed blocks recursively.
     * At first, the explosion expands to all directions depending on the explosion power left, leaving out
     * already destroyed blocks.
     * @param commands ArrayList of commands to be executed on clients // TODO @Kegny
     * @param blockX Used to move through terrain, which is a grid
     * @param blockY Used to move through terrain, which is a grid
     * @param explosionPower value to determine (int)if block (blockX,blockY) is destroyed
     */
    private void explode(ArrayList<String> commands, int blockX, int blockY, int explosionPower){
        final char DESTROYED_TERRAIN = '#';
        char replacement = DESTROYED_TERRAIN;

        if (explosionPower > 0 && terrain.get(blockY).get(blockX).getType() != DESTROYED_TERRAIN) { //else abort recursion
            double resistanceOfBlock = terrain.get(blockY).get(blockX).getResistance();

            //Calc behaviour for current Block
            if (explosionPower >= resistanceOfBlock) { // Enough destructive force

                //Print Debugging-MSG to console:
                debugLog("Explosion of: \"" + terrain.get(blockY).get(blockX) + "\" (" + blockX + " " + blockY + ")" + "Resistance: " + resistanceOfBlock + "; " + "Explosionpower: " + explosionPower);

                explosionPower -= resistanceOfBlock; //Reduce explosionPower
                replaceBlock(blockX,blockY,replacement); //Mark as destroyed

                // Recursively continue destruction for all directions unless OutOfBounds
                if (blockY+1 < terrain.size()){ explode(commands, blockX, blockY + 1, explosionPower); }

                if (blockX > 0) { explode(commands, blockX - 1, blockY, explosionPower); }
                if (blockX+1 < terrain.get(blockY).size()) { explode(commands, blockX + 1, blockY, explosionPower); }

                if (blockY > 0) { explode(commands, blockX, blockY-1, explosionPower); }

                // Add destruction of actual Block to commandlist
                commands.add("REPLACE_BLOCK " + blockX + " " + blockY + " " + replacement);// ' ' is impossible due to the Client/Server-MSG-System

            } else { // Check if partially enough destructive Force
                if(explosionPower > resistanceOfBlock * TerrainBlock.MODIFIER_FOR_SLANTS && !terrain.get(blockY).get(blockX).isSky()){ // BUT do not create slants out of air

                    debugLog("now a Slant: \"" + terrain.get(blockY).get(blockX) + "\" (" + blockX + " " + blockY + ")" + "Resistance: " + resistanceOfBlock + "; " + "Explosionpower: " + explosionPower);

                    if(blockX > 0 && blockX < terrain.get(blockY).size()){
                        if(terrain.get(blockY).get(blockX-1).getType() != DESTROYED_TERRAIN && terrain.get(blockY).get(blockX-1).getType() != ' '){
                            commands.add("REPLACE_BLOCK " + blockX + " " + blockY + " " + '\\');
                        } else {
                            if(terrain.get(blockY).get(blockX+1).getType() != DESTROYED_TERRAIN && terrain.get(blockY).get(blockX+1).getType() != ' '){
                                commands.add("REPLACE_BLOCK " + blockX + " " + blockY + " " + '/');
                            }
                        }
                    }
                }
            }
        } // recursion
    }

    /**
     * // TODO @Kegny
     * @param impactPoint
     * @param explosionPower
     * @return
     */
    public ArrayList<String> handleExplosion(Point2D impactPoint, int explosionPower) {
        // Get Block, which is center of explosion, from Point2D
        int blockX = (int) impactPoint.getX() / BLOCK_SIZE;
        int blockY = (int) impactPoint.getY() / BLOCK_SIZE;

        ArrayList<String> commands = new ArrayList<String>();
        explode(commands, blockX, blockY, explosionPower); //Recursive Function, actual handling in here, adds commands to the arraylist

        return commands;
    }

    /**
     * This function is the inverse function of {@link #Terrain(de.hhu.propra.team61.io.json.JSONObject)}.
     * @return a JSONObject representing the loaded level, containing the current state of the terrain (ie. including destruction)
     */
    public JSONObject toJson() {
        JSONObject save = new JSONObject();
        JSONArray jsonTerrain = new JSONArray();
        for (ArrayList<TerrainBlock> row : terrain) {
            StringBuilder builder = new StringBuilder();
            for (TerrainBlock block : row) { // forming a String from terrain[i]
                builder.append(block.getType());
            }
            jsonTerrain.put(builder.toString());
        }
        save.put("terrain", jsonTerrain);
        return save;
    }

    /**
     * Destroys columns between the left or right side of the board and a given figure position (usually the boss).
     * @param position the position of the figure
     * @param fromLeft whether to start from the left (true) or right (false)
     * @param movedBy absolute value of x-position change in px since last call of this function
     */
    public void destroyColumns(Point2D position, boolean fromLeft, int movedBy) {
        movedBy = movedBy / BLOCK_SIZE;

        int column = (int)(position.getX() / BLOCK_SIZE + (fromLeft ? 0 : Figure.NORMED_OBJECT_SIZE / BLOCK_SIZE));
        if(column >= terrain.get(0).size()) column = terrain.get(0).size() - 1;

        int colStart = (fromLeft ? 0 : column);
        int colEnd = (fromLeft ? column-1 : terrain.get(0).size()-1);
        if(colEnd >= terrain.get(0).size()) colEnd = terrain.get(0).size() - 1;

        int lastAlreadyDestroyedColumn = (fromLeft ? -1 : terrain.get(0).size());

        for (int col = colStart; col <= colEnd; col++) {
            if(terrain.get(0).get(col).getType() == '@') { // column already destroyed
                if(fromLeft || lastAlreadyDestroyedColumn == terrain.get(0).size()) {
                    lastAlreadyDestroyedColumn = col;
                }
            } else {
                for (ArrayList<TerrainBlock> row : terrain) {
                    row.get(col).setType('@'); // TODO special terrain type?
                }
            }
        }

        Rectangle2D destroyedTerrain = new Rectangle2D(colStart*BLOCK_SIZE, 0, (colEnd + (fromLeft?1:0))*BLOCK_SIZE, terrain.size()*BLOCK_SIZE+BLOCK_SIZE);

        for(Figure figure: figures) {
            if(figure.getHitRegion().intersects(destroyedTerrain)) {
                figure.setHealth(0);
            }
        }

        // do not play destroy animation for all columns
        if(fromLeft) {
            colStart = Math.max(0, lastAlreadyDestroyedColumn - 3 * movedBy + 1);
        } else {
            colEnd = Math.min(terrain.get(0).size() -1, lastAlreadyDestroyedColumn + 3 * movedBy - 1);
        }

        for(int i=colStart; i<=colEnd; i++) {
            ImageView riftImageView = new ImageView(RIFT_IMAGE);
            SpriteAnimation riftAnimation = new SpriteAnimation(riftImageView, 500, 12, 1);
            final int col = i;
            Platform.runLater(() -> {
              add(riftImageView, col, 0, 1, terrain.size());
            });
            riftImageView.setFitHeight(terrain.size() * BLOCK_SIZE);
            riftAnimation.setDelay(new Duration((fromLeft ? colEnd - i : i - colStart) * 40 + 500));
            riftAnimation.play();
        }
    }

    /**
     * @return the width of the terrain in px, 0 if no terrain loaded
     */
    public int getTerrainWidth() {
        if(terrain == null || terrain.size() == 0) return 0;
        return terrain.get(0).size() * BLOCK_SIZE;
    }

    /**
     * @return the height of the terrain in px, 0 if no terrain loaded
     */
    public int getTerrainHeight() {
        if(terrain == null) return 0;
        return terrain.size() * BLOCK_SIZE;
    }

    /**
     * Checks if the figure at the given position is standing on a liquid.
     * @param position the position of the figure
     * @return true if the figure at the given position is standing on a liquid
     */
    public boolean standingOnLiquid(Point2D position) {
        final int minX = (int)Math.floor(position.getX() / BLOCK_SIZE);
        final int maxX = (int)Math.ceil((position.getX() + Figure.NORMED_OBJECT_SIZE) / BLOCK_SIZE);
        final int y = (int)Math.ceil((Math.round(position.getY()) + Figure.NORMED_OBJECT_SIZE) / BLOCK_SIZE);

        if(y >= terrain.size()) {
            return false;
        }

        for(int x=minX; x < maxX && x < terrain.get(y).size(); x++) {
            if(terrain.get(y).get(x).isLiquid()) {
                return true;
            }
        }

        return false;
    }
}
