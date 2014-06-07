package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;

/**
 * Created by kevin on 21.05.14.
 * Abstract class implementing all methods which Items have in common
 */
public abstract class Item extends ImageView{
    private String name;
    private String description;
    protected String imagePath;

    protected int munition;     // Item can only be used when munition > 0


    // ToDo I'll check if cannot do this nicer
    public abstract ImageView getCrosshair();
    public abstract void angleDraw(boolean faces_right);
    public abstract void angleUp(boolean faces_right);
    public abstract void angleDown(boolean faces_right);

    public abstract Projectile shoot(int power) throws NoMunitionException;

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
