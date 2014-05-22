package de.hhu.propra.team61.Objects;

import de.hhu.propra.team61.IO.TerrainManager;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * A GridPane representing a terrain.
 * The class has methods for getting spawn points, walkability checks and destroying terrain (TODO actually not)
 * Created by markus on 17.05.14.
 */
public class Terrain extends GridPane {
    private static String imgPath = "file:resources/";

    private ArrayList<ArrayList<Character>> terrain;
    private ArrayList<Point2D> spawnPoints;

    /**
     * @param terrain 2-D-ArrayList containing the terrain to be displayed
     */
    public Terrain(ArrayList<ArrayList<Character>> terrain) {
        load(terrain);
    }

    public void load(ArrayList<ArrayList<Character>> terrain) {
        getChildren().clear();

        this.terrain = terrain;

        spawnPoints = new ArrayList<Point2D>();

        //Draw Terrain
        setGridLinesVisible(true); //Gridlines on/off
        setAlignment(Pos.TOP_LEFT);

        String img;

        for(int i=0; i < terrain.size(); i++){
            for(int j=0; j < terrain.get(i).size(); j++){
                char terraintype = terrain.get(i).get(j);
                switch(terraintype) {
                    case '_': img = "plain_ground.png";
                        break;
                    case '/': img = "slant_ground_ri.png";
                        break;
                    case '\\': img = "slant_ground_le.png";
                        break;
                    case '|': img = "wall_le.png";
                        break;
                    case 'S': img = "stones.png";
                        break;
                    case 'E': img = "earth.png";
                        break;
                    case 'W': img = "water.png";
                        break;
                    case 'I': img = "ice.png";
                        break;
                    case 'L': img = "lava.png";
                        break;
                    case 'P': // special case: spawn point, add to list and draw sky
                        spawnPoints.add(new Point2D(j, i));
                        terrain.get(i).set(j, ' ');
                    default : img = "sky.png";
                }
                Image image = new Image(imgPath + img);
                ImageView content = new ImageView();
                content.setImage(image);

                add(content, j, i);
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
     * @return a random spawn point, or null if there are no more spawn points
     */
    public Point2D getRandomSpawnPoint() {
        if(spawnPoints.isEmpty()) {
            return null;
        }
        int index = (int) (Math.random()*spawnPoints.size());
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
        for(int i=0; i<n; i++) {
            spawnPoints.add(getRandomSpawnPoint());
        }
        return spawnPoints;
    }

    /**
     * adds direction to oldPosition, but assures that we do not walk/fly through terrain or other figures
     * When stopOnEveryCollision is true,  the movement is always stopped when hitting a object (used for munition);
     * otherwise, the movement may also continue in diagonal direction (used for figures)
     * @param oldPosition old position of the object
     * @param direction direction vector of the object
     * @param hitRegion
     * @param stopOnEveryCollision when true, also stop on diagonal walls
     * @return new position
     * @throws CollisionWithFigureException TODO
     * @throws CollisionWithTerrainException TODO
     * TODO dummy implementation, randomly throws exceptions
     */
    public Point2D getPositionForDirection(Point2D oldPosition, Point2D direction, Rectangle2D hitRegion, boolean stopOnEveryCollision) {
        Point2D newPosition = new Point2D(oldPosition.getX(), oldPosition.getY());
        Point2D preferredFinalPosition = new Point2D(oldPosition.getX(), oldPosition.getY());
        preferredFinalPosition.add(direction);
        Point2D normalizedDirection = new Point2D(oldPosition.getX(), oldPosition.getY());
        normalizedDirection.normalize();

        //while(newPosition.distance(preferredFinalPosition) > 1) {
            //System.out.println("norm vec " + normalizedDirection);
            //newPosition.add(normalizedDirection);

            int minY = (int) Math.floor(hitRegion.getMinY() / 8);
            int maxY = (int) Math.ceil(hitRegion.getMaxY() / 8);
            int minX = (int) Math.floor(hitRegion.getMinX() / 8);
            int maxX = (int) Math.ceil(hitRegion.getMaxX() / 8);

            if (minY < 0) minY = 0; // etc. TODO

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    Rectangle2D rec = new Rectangle2D(x * 8, y * 8, 8, 8);
                    System.out.println(hitRegion + " " + terrain.get(y).get(x) + " field: " + rec);
                    if (terrain.get(y).get(x) != ' ' && hitRegion.intersects(rec)) {
                        System.out.println("intersection at " + x + " " + y);
                    }
                }
            }

//        }
        return oldPosition;
    }

}
