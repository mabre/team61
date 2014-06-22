package de.hhu.propra.team61.objects;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Created by kevin on 21.05.14.
 * Abstract class implementing all methods which Items have in common
 */
public abstract class Item extends StackPane{
    protected String name;
    protected String description;
    protected int munition; // Item can only be used when munition > 0

    public Item(String name, String description){
        this.name = name;
        this.description = description;
    }

    // Implementation varies
    public abstract void angleDraw(boolean faces_right);
    public abstract void angleUp(boolean faces_right);
    public abstract void angleDown(boolean faces_right);
   // public abstract void angleLeft(boolean faces_right);
   // public abstract void angleRight(boolean faces_right);

    /**
     *
     * @param user
     * @return projectile in case of weapon else returns NULL
     * @throws NoMunitionException
     */
    public abstract Projectile use(Figure user) throws NoMunitionException;

    /**
     * @param pos in px
     */
    public abstract void setPosition(Point2D pos);

    public void refill(){ munition++; }


    public String getName() {
        return name;
    }
    public int getMunition() {
        return munition;
    }
}
