package de.hhu.propra.team61.objects;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;

/**
 * Created by kevgny on 21.05.14.
 */
public abstract class Item extends ImageView{
    private String name;
    private String description;
    protected String imagePath;

    protected int munition;     // Item can only be used when munition > 0


    public abstract ImageView getCrosshair();
    public abstract void angleDraw(boolean faces_right);
    public abstract void angleUp(boolean faces_right);
    public abstract void angleDown(boolean faces_right);

    public abstract Projectile shoot() throws NoMunitionException;

    /**
     * @param pos in px
     */
    public void setPosition(Point2D pos) {
        this.setTranslateX(pos.getX());
        this.setTranslateY(pos.getY());
    }

    public void hide() {
        this.setTranslateX(-1000);
        this.setTranslateY(-1000);
    }
}
