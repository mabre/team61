package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.io.ItemManager;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

/**
 * Created by kevin on ?
 */
public class Projectile extends ImageView {
    private Point2D velocity;

    private int mass;
    private boolean drifts;

    double angle;  // Rotation of image to make then face direction TODO implement more than just this var

    private Weapon source;

    private Rectangle2D hitRegion;

    private boolean isShard;

    /**
     * @param image the image with which the projectile is drawn, hit region is extracted from its dimension
     * @param position of the weapon (ie position of figure)
     * @param firedAt direction (ie position of cross hair)
     * @param velocity determines speed and direction
     * @param shotBy Weapon which produced this projectile, helpful for specific damaging
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
    public Projectile(JSONObject input){
        setTranslateX(input.getInt("posX"));
        setTranslateY(input.getInt("posY"));

        this.velocity = new Point2D(input.getInt("vx"),input.getInt("vy"));

        this.source = (Weapon)ItemManager.returnItem(input.getString("source"));
        this.angle  = source.getAngle();
        this.mass   = source.getMass();
        this.drifts = source.getDrifts();
        this.isShard = input.getBoolean("isShard");

        setImage(new Image(source.getImage(),4,4,true,true));
        hitRegion = new Rectangle2D(getTranslateX(), getTranslateY(), getImage().getWidth(), getImage().getHeight());
    }
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
     * calculates hitbox from position given and passes down the request to weaponclass with that additional information
     *
     * @param terrain for destruction
     * @param teams ability to affect all figures
     * @param impactPoint NEEDed for a hitbox placed ON colliding position, NOT last "good" one
     */
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Point2D impactPoint){
        return this.source.handleCollision(terrain,teams,new Rectangle2D(impactPoint.getX(),impactPoint.getY(),getImage().getWidth(),getImage().getHeight()),isShard);
    }
}
