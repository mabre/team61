package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 */
public class Projectile extends ImageView {
    boolean falls; // Gravitation on/off

    private Point2D velocity;
    private int damage;

    private Rectangle2D hitRegion;

    /**
     * @param image
     * @param position
     * @param firedAt
     * @param velocity
     * @param damage
     */
    public Projectile(Image image,Point2D position,Point2D firedAt,int velocity,int damage){
        setImage(image);
        setTranslateX(position.getX());
        setTranslateY(position.getY());
        this.velocity = firedAt.subtract(position);
        this.velocity = this.velocity.normalize().multiply(velocity);
        this.damage = damage;
        hitRegion = new Rectangle2D(position.getX(), position.getY(), image.getWidth(), image.getHeight());
        System.out.println("created projectile, v=" + this.velocity);
    }

    /**
     * @param pos new position in px
     */
    public void setPosition(Point2D pos) {
        setTranslateX(pos.getX());
        setTranslateY(pos.getY());
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

}
