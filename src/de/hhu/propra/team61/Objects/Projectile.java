package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 */
public class Projectile extends ImageView {
    boolean falls; // Gravitation on/off

    public Projectile(Image image,Point2D position,Point2D firedAt,int velocity,int damage){
        setImage(image);
        // ... //
    }
}
