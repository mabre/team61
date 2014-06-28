package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.io.ItemManager;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

// Created by kevin on ?
/**
 * An instance of this class represents a projectile flying through {@link de.hhu.propra.team61.objects.Terrain} <p>
 * being able to collide with {@link de.hhu.propra.team61.objects.Figure}s and {@link de.hhu.propra.team61.objects.Terrain}.
 */
public class Projectile extends ImageView {
    /** Weapon from which this was fired */
    private Weapon source;
    /** Vector indicating flight-direction and -speed*/
    private Point2D velocity;
    /** Mass effects gravitational force */
    private int mass;
    /** Enables effect of wind on this projectile */
    private boolean drifts;
    /** Rotates image to make it face flight-direction */
    double angle;  // Rotation of image to make then face direction TODO implement more than just this var
    /** Defines if the projectile is a shard to enable different collisionhandling */
    private boolean isShard;
    /** Collisionarea */
    private Rectangle2D hitRegion;


    /**
     * Constructor setting up all variables, called by all {@link de.hhu.propra.team61.objects.Weapon}s
     *
     * @param image the image with which the projectile is drawn, hit region is extracted from its dimension
     * @param position of the weapon (ie position of figure)
     * @param firedAt direction (ie position of cross hair)
     * @param velocity determines speed and direction
     * @param shotBy Weapon which produced this projectile, helpful for specific damaging
     *
     * @throws Exception if projectile has no velocity
     */
    public Projectile(Image image, Point2D position, Point2D firedAt, int velocity, Weapon shotBy){
        setImage(image);
        setTranslateX(firedAt.getX());
        setTranslateY(firedAt.getY());

        this.velocity = firedAt.subtract(position);
        this.velocity = this.velocity.normalize().multiply(velocity);

        this.source = shotBy;
        this.angle  = shotBy.getAngle();
        this.mass   = shotBy.getMass();
        this.drifts = shotBy.getDrifts();

        hitRegion = new Rectangle2D(getTranslateX(), getTranslateY(), image.getWidth(), image.getHeight());
        isShard = false;

        System.out.println("created projectile at " + getTranslateX() + " " + getTranslateY() + ", v=" + this.velocity);

        if(this.velocity.magnitude() == 0) {
            throw new IllegalArgumentException("Projectile with no speed was requested; position: " + position + ", firedAt " + firedAt + ", velocity " + velocity);
        }
    }
    /**
     * Constructor from JSONObject, needed for client-server interaction.<p>
     * Since instances cannot be passed around by JSON in our server-client-architecture a new instance is temporarily created.
     *
     * @param input JSON-String, which <u><b>must</b></u> contain all variables needed in {@link de.hhu.propra.team61.objects.Projectile#Projectile(javafx.scene.image.Image, javafx.geometry.Point2D, javafx.geometry.Point2D, int, Weapon)} and an boolean isShard
     */
    public Projectile(JSONObject input){
        setTranslateX(input.getInt("posX"));
        setTranslateY(input.getInt("posY"));

        this.velocity = new Point2D(input.getInt("vx"),input.getInt("vy"));

        this.source = (Weapon)ItemManager.returnItem(input.getString("source"));
        this.angle  = source.getAngle();
        this.mass   = source.getMass();
        this.drifts = source.getDrifts();
        this.isShard = input.getBoolean("isShard");

        setImage(new Image(source.getProjectileImage(),4,4,true,true));
        hitRegion = new Rectangle2D(getTranslateX(), getTranslateY(), getImage().getWidth(), getImage().getHeight());
    }

    /**
     * Wraps up all variables into an JSONObject
     *
     * @return JSONObject to be send to the clients to reconstruct this instance
     */
    public JSONObject toJson(){
        JSONObject output = new JSONObject();
        output.put("image",getImage());
        output.put("posX",getPosition().getX());
        output.put("posY",getPosition().getY());
        output.put("vx",velocity.getX());
        output.put("vy",velocity.getY());
        output.put("source",source.getName());
        output.put("isShard",isShard);
        return output;
    }

    /**
     * Sets the position of the projectile
     * This method can be called from any thread.
     * @param pos new position in px
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
    public void addVelocity(Point2D dV) {
        velocity =  velocity.add(dV);
    }

    public int getMass() {
        return mass;
    }
    public boolean getDrifts() {
        return drifts;
    }
    public void setShard(){ isShard = true; }

    /**
     * calculates hitbox from position given and own size and passes down the request to weaponclass with that additional information
     *
     * @param terrain for destruction
     * @param teams ability to affect all figures
     * @param impactPoint <u>needed</u> for a hitbox placed <b>on</b> colliding position, <b>not</b> last "good" one
     *
     * @return hands over the return of {@link de.hhu.propra.team61.objects.Weapon#handleCollision(Terrain, java.util.ArrayList, javafx.geometry.Rectangle2D, Boolean)}
     */
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Point2D impactPoint){
        return this.source.handleCollision(terrain,teams,new Rectangle2D(impactPoint.getX(),impactPoint.getY(),getImage().getWidth(),getImage().getHeight()),isShard);
    }
}
