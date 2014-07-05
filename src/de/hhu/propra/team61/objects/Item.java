package de.hhu.propra.team61.objects;

import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

// Created by kevin on 21.05.14.
/**
 * Abstract class implementing all methods which Items have in common. It extends {@link javafx.scene.layout.StackPane} <p>
 * in order to enable imagehandling.
 */
public abstract class Item extends StackPane{
    /** Graphical constant sizes in px */
    private final int NORMED_OBJECT_SIZE = 16; //ToDo Move this? Like in Projectile
    private final int NORMED_BLOCK_SIZE  =  8;
    /** Name of the Item */
    protected String name;
    /** More or less humouristic description of said item */
    protected String description;
    /** Image of item added to the Stackpane */
    protected ImageView itemImage;
    /** Amount of times this item can be used */
    protected int munition; // Item can only be used when munition > 0
    /** Label indicating left uses */
    protected Label munitionDisplay;

    /**
     * Constructor for deriving classes, setting up variables and the Stackpane
     *
     * @param name name of this item
     * @param description description of this item
     * @param itemImageSRC Filepath to image to be used as itemImage
     */
    protected Item(String name, String description, String itemImageSRC){
        this.name = name;
        this.description = description;

        this.itemImage = new ImageView(new Image(itemImageSRC, NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true));
        this.getChildren().add(itemImage);
    }

    /**
     * Abstract functions all Items should have implemented, which differs often.
     * @param faces_right
     */
    public abstract void angleDraw(boolean faces_right);
    public abstract void angleUp(boolean faces_right);
    public abstract void angleDown(boolean faces_right);
    public abstract void angleLeft(boolean faces_right);
    public abstract void angleRight(boolean faces_right);

    /**
     *
     * @param user {@link de.hhu.propra.team61.objects.Figure} using the Item
     * @return projectile in case of weapon else NULL is returned
     * @throws NoMunitionException
     */
    public abstract Projectile use(Figure user) throws NoMunitionException;

    /**
     * @param pos in px
     */
    public void setPosition(Point2D pos){
        itemImage.setTranslateX(pos.getX());
        itemImage.setTranslateY(pos.getY());
    }

    /**
     * increases munition by 1
     */
    public void refill(){ munition++; }
    public int getMunition() {
        return munition;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public ImageView getItemImage() {
        return itemImage;
    }



}
