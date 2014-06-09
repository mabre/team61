package de.hhu.propra.team61.objects;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 */
public class Projectile extends ImageView {
    boolean falls; // Gravitation on/off
    double angle;

    private Point2D velocity;

    private int damage;

    private Rectangle2D hitRegion;

    /**
     * @param image the image with which the projectile is drawn, hit region is extracted from its dimension
     * @param position of the weapon (ie position of figure)
     * @param firedAt direction (ie position of cross hair)
     * @param velocity determines speed and direction
     * @param damage health damage caused when hitting a figure
     */
    public Projectile(Image image, Point2D position, Point2D firedAt, int velocity, int damage){
        setImage(image);
        setTranslateX(firedAt.getX());
        setTranslateY(firedAt.getY());
        this.velocity = firedAt.subtract(position);
        this.velocity = this.velocity.normalize().multiply(velocity);
        this.damage = damage;
        hitRegion = new Rectangle2D(getTranslateX(), getTranslateY(), image.getWidth(), image.getHeight());
        System.out.println("created projectile at " + getTranslateX() + " " + getTranslateY() + ", v=" + this.velocity);
        if(this.velocity.magnitude() == 0) {
            throw new IllegalArgumentException("Projectile with no speed was requested; position: " + position + ", firedAt " + firedAt + ", velocity " + velocity);
        }
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

    public int getDamage() {
        return damage;
    }

}
