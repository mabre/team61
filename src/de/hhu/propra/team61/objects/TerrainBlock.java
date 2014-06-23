package de.hhu.propra.team61.objects;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by markus on 23.06.14.
 */
public class TerrainBlock extends ImageView {

    private final static String imgPath = "file:resources/";
    private final static Image EARTH_IMAGE = new Image(imgPath + "earth.png");
    private final static Image ICE_IMAGE = new Image(imgPath + "ice.png");
    private final static Image LAVE_IMAGE = new Image(imgPath + "lava.png");
    private final static Image SKY_IMAGE = new Image(imgPath + "sky.png");
    private final static Image SLANT_LE_IMAGE = new Image(imgPath + "slant_ground_le.png");
    private final static Image SLANT_RI_IMAGE = new Image(imgPath + "slant_ground_ri.png");
    private final static Image STONES_IMAGE = new Image(imgPath + "stones.png");
    private final static Image WATER_IMAGE = new Image(imgPath + "water.png");
    private final static Image SPAWN_POINT = new Image(imgPath + "spawn.png");

    //Technical Blocks/Special Cases
    private final static double RESISTANCE_OF_SKY = 15;
    private final static double RESISTANCE_OF_FLUIDS = 99999999;
    //Blocks
    private final static double RESISTANCE_OF_EARTH = 25;
    private final static double RESISTANCE_OF_SAND = 20;
    private final static double RESISTANCE_OF_SNOW = 20;
    private final static double RESISTANCE_OF_STONE = 35;
    private final static double RESISTANCE_OF_ICE = 30;

    private final static double ICE_FRICTION = 2;
    private final static double EARTH_FRICTION = 1;
    private final static double SAND_FRICTION = 0.5;

    private char type;
    private int x;
    private int y;
    
    private class NeighbouringBlocks {
        public TerrainBlock left = null;
        public TerrainBlock top = null;
        public TerrainBlock right = null;
        public TerrainBlock bottom = null;
    }
    private NeighbouringBlocks neighbours = new NeighbouringBlocks();

    /**
     * @param type a terrain type, see BoardLegend for valid types
     */
    public TerrainBlock(char type, int x, int y) {
        if(type == 'P') type = ' '; // draw spawn points as sky (only on creation - changes later on probably done by level editor)
        this.type = type;
        this.x = x;
        this.y = y;

        drawImage();
    }

    public void setLeftNeighbour(TerrainBlock block) {
        neighbours.left = block;
    }

    public void setTopNeighbour(TerrainBlock block) {
        neighbours.top = block;
    }

    public void setRightNeighbour(TerrainBlock block) {
        neighbours.right = block;
    }

    public void setBottomNeighbour(TerrainBlock block) {
        neighbours.bottom = block;
    }

    private void drawImage() {
        switch(type) {
            case ' ':
                this.setImage(SKY_IMAGE);
                break;
            case 'S':
                this.setImage(STONES_IMAGE);
                break;
            case 'E':
                this.setImage(EARTH_IMAGE);
                break;
            case 'I':
                this.setImage(ICE_IMAGE);
                break;
            case '/':
                this.setImage(SLANT_RI_IMAGE);
                break;
            case '\\':
                this.setImage(SLANT_LE_IMAGE);
                break;
            case 'W':
                this.setImage(WATER_IMAGE);
                break;
            case 'L':
                this.setImage(LAVE_IMAGE);
                break;
            case 'P': // special case: spawn point to be shown in level editor
                this.setImage(SPAWN_POINT);
                break;
            default:
                this.setImage(SKY_IMAGE);
        }
    }

    public double getResistance() {
        switch (type) {
            case ' ': return RESISTANCE_OF_SKY;
            case 'W':
            case 'L': return RESISTANCE_OF_FLUIDS;
            case '/':
            case '\\': // slants are depending on blocks below
                if(neighbours.bottom != null) {
                    return neighbours.bottom.getResistance() * Terrain.MODIFIER_FOR_SLANTS;
                } else {
                    return RESISTANCE_OF_SKY; // return an at least somewhat useful information
                }
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

    /**
     * @param hitRegion of a figure or whatever
     * @return true, when hitRegion intersects with the field
     */
    public boolean intersects(Rectangle2D hitRegion) { // TODO keep hitRegion (rename to collisionArea?) as private member; different types of hitRegions as final static members
        switch (type) {
            case ' ':
                return false;
            case '/':
                Point2D p;
                for (int i = 0; i < Terrain.BLOCK_SIZE; i++) {
                    int px = x * Terrain.BLOCK_SIZE + i;
                    int py = y * Terrain.BLOCK_SIZE + Terrain.BLOCK_SIZE - i;
                    p = new Point2D(px, py);
                    if(hitRegion.contains(p)) {
                        Terrain.debugLog("diagonal / intersection at " + px + "x" + py + "px");
                        return true;
                    }
                }
                return false;
            case '\\':
                for(int i=0; i<Terrain.BLOCK_SIZE; i++) {
                    int px = x*Terrain.BLOCK_SIZE+i;
                    int py = y*Terrain.BLOCK_SIZE+1+i;
                    p = new Point2D(px, py);
                    if(hitRegion.contains(p)) {
                        Terrain.debugLog("diagonal / intersection at " + px + "x" + py + "px");
                        return true;
                    }
                }
                return false;
            default:
                Rectangle2D rec = new Rectangle2D(x * Terrain.BLOCK_SIZE, y * Terrain.BLOCK_SIZE, Terrain.BLOCK_SIZE, Terrain.BLOCK_SIZE);
                return hitRegion.intersects(rec);
        }
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
        drawImage();
    }

    public boolean isSky() {
        return type == ' ';
    }

    public boolean isLiquid() {
        return (type == 'W' || type == 'L');
    }

    public double getFriction() {
        switch (type) {
            case 's':
                return SAND_FRICTION;
            case 'I':
                return ICE_FRICTION;
            case 'E':
                return EARTH_FRICTION;
            default : return 1;
        }
    }
}
