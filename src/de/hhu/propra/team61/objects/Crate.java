package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.io.ItemManager;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by kevin on 17.06.14.
 */
public class Crate extends ImageView {
    private static final int NORMED_OBJECT_SIZE = 16;
    private static final int NORMED_BLOCK_SIZE  = 8;
    private static final String IMGSRC = "file:resources/weapons/crate.png";

    private static final int MASS               = 1000;
    private static final int EXPLOSIONPOWER     = 30;
    private static final int DAMAGERESISTANCE   = 10;

    /** position of the figure, has to be synced with translateX/Y (introduced to prevent timing issues on JavaFX thread) */
    private Point2D velocity = new Point2D(0,10);
    private Rectangle2D hitRegion;

    private String content;


    public Crate(int xSize){
        content = ItemManager.itemlist[(int)Math.round(Math.random()*(ItemManager.numberOfItems-1))];
        initialize(xSize);
        }
    public Crate(int xSize, String content) {
        this.content = content;
        initialize(xSize);
    }

    private void initialize(int xSize){
        setImage(new Image(IMGSRC,NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE,true,true));
        setTranslateX(Math.random()*xSize*NORMED_BLOCK_SIZE);
        setTranslateY(0);

        hitRegion = new Rectangle2D(getTranslateX(), getTranslateY(),NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE);
        //ToDo add yourself to terrain for collisionhandling
    }

    public String getContent() {
        return content;
    }

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