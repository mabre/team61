package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.io.ItemManager;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Created by kevin on 17.06.14.
/**
 * An instance of this class represents a crate, which can be picked up.
 * It extends {@link javafx.scene.image.ImageView} in order to ease handling of the image
 * Crates collide with {@link de.hhu.propra.team61.objects.Terrain} and {@link de.hhu.propra.team61.objects.Figure}s
 */
public class Crate extends ImageView {
    /** Sizes in px */
    private static final int NORMED_OBJECT_SIZE = 16;
    private static final int NORMED_BLOCK_SIZE  = 8;
    /** Path to image to be used */
    private static final String IMGSRC = "file:resources/weapons/crate.png";
    /** Used to make gravity effect Crates */
    private static final int MASS               = 1000;
    /** Resistance, if explosion exceeds this the crate detonates //TODO */
    private static final int DAMAGERESISTANCE   = 10;
    /** Power of the explosion caused by detonation //TODO */
    private static final int EXPLOSIONPOWER     = 30;

    /** position of the figure, has to be synced with translateX/Y (introduced to prevent timing issues on JavaFX thread) */
    private Point2D velocity = new Point2D(0,10);
    /** Used for collision */
    private Rectangle2D hitRegion;
    /** Name of an Item */
    private String content;

    /**
     * Chooses a random Content from {@link de.hhu.propra.team61.io.ItemManager#itemlist} in {@link de.hhu.propra.team61.io.ItemManager}
     * and spawns itself on maximal heigth on a random position of the {@link de.hhu.propra.team61.objects.Terrain}
     *
     * @param xSize width of {@link de.hhu.propra.team61.objects.Terrain}
     */
    public Crate(int xSize){
        content = ItemManager.itemlist[(int)Math.round(Math.random()*(ItemManager.numberOfItems-1))];
        initialize(xSize);
        }

    /**
     * Alternative Constructor with set content
     *
     * @param xSize width of {@link de.hhu.propra.team61.objects.Terrain}
     * @param content content of crate
     */
    public Crate(int xSize, String content) {
        this.content = content;
        initialize(xSize);
    }

    /**
     * Constructor for saved games
     * @param input JSONObject containing content and position
     */
    public Crate(JSONObject input){
        this.content = input.getString("content");
        setTranslateX(input.getInt("posX"));
        setTranslateY(input.getInt("posY"));

        setImage(new Image(IMGSRC, NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true));
        hitRegion = new Rectangle2D(getTranslateX(), getTranslateY(),NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE);
        //ToDo add yourself to terrain for collisionhandling
    }

    /**
     * extension of the constructor, which takes over <u>all</u> tasks, which all contructors have in common
     *
     * @param xSize width of {@link de.hhu.propra.team61.objects.Terrain}
     */
    private void initialize(int xSize){
        setImage(new Image(IMGSRC, NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true));
        setTranslateX(Math.random()*xSize*NORMED_BLOCK_SIZE);
        setTranslateY(0);

        hitRegion = new Rectangle2D(getTranslateX(), getTranslateY(),NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE);
        //ToDo add yourself to terrain for collisionhandling
    }

    public String getContent() {
        return content;
    }

    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("content",content);
        output.put("posX",getTranslateX());
        output.put("posY",getTranslateY());
        return output;
    }

    /**
     *
     * @param pos in px
     */
    public void setPosition(Point2D pos) {
        Platform.runLater(() -> {
            setTranslateX(pos.getX());
            setTranslateY(pos.getY());
        });
        hitRegion = new Rectangle2D(pos.getX(), pos.getY(), hitRegion.getWidth(), hitRegion.getHeight());
    }
    /**
     * @return position in px
     */
    public Point2D getPosition() {
        return new Point2D(getTranslateX(), getTranslateY());
    }
    public Rectangle2D getHitRegion() {
        return hitRegion;
    }

    
    public Point2D getVelocity() {
        return velocity;
    }
    public void resetVelocity() {
        velocity =  new Point2D(0,10);
    }
    public void nullifyVelocity() {
        velocity =  new Point2D(0,0);
    }

    public int getMass() {
        return MASS;
    }

}