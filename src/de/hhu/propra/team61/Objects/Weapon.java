package de.hhu.propra.team61.Objects;

import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.Team;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

/**
 * Created by kevin on 21.05.14.
 *
 * Abstract class containing all variables and methods which all Weapons have in common
 * This class is also responsible:
 * - for the Crosshair
 * - for the Weaponimage/-facing
 * - Shooting
 * - Transmission of all the attributes by being carried by a projectile
 *
 *  Deriving classes:
 * - Has and sets up nearly all attributes
 * - coordination of Figure.sufferDamage();
 * - coordination of Terraindestruction // ToDo implement this
 * - Sending Figures flying // ToDo implement this
 */
public abstract class Weapon extends Item {
    private final int NORMED_OBJECT_SIZE = 16; //ToDo Move this? Like in Projectile
    private final int RADIUS = 25; // Distance between Crosshair to Figure

    private String projectileImg;
    private String weaponImg;

    private int delay;          // Timedelay, Explode in x seconds etc.

    private String damagetype;  // Firedamage etc.
    private int damage;         // Damage to Figures
    private int explosionpower; // Damage to environment
    private int shockwave;      // Throw Figures away from Collisionpoint

    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow

    private double angle;       // Angle it is aimed at; 0 <=> Horizontal; +90 <=> straight upwards
    private ImageView crosshair;


    public abstract ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Rectangle2D impactArea);

    /**
     * @param munition Number of shots left
     * @param WEAPON_IMG String/Path to the Image representing the Weapon
     * @param PROJECTILE_IMG String/Path to the image representing the projectile
     * @param delay fusetimer  //ToDo validate if this is needed up here or if it's enough if this stays in the implementations
     * @param damagetype e.g. Firedamage //ToDo same here
     * @param damage Figure.sufferDamage(damage)
     * @param explosionpower Destructive force
     * @param shockwave Propulsion of Objects
     */
    public Weapon(int munition, String WEAPON_IMG, String PROJECTILE_IMG, int delay, String damagetype, int damage, int explosionpower, int shockwave){ //ToDo Replace variables with something better
        this.munition = munition;
        this.weaponImg = WEAPON_IMG;
        this.projectileImg = PROJECTILE_IMG;
        this.delay = delay;
        this.damagetype = damagetype;
        this.damage = damage;
        this.explosionpower = explosionpower;
        this.shockwave = shockwave;

        initialize(); //Draw Crosshair etc
    }
    public Weapon(JSONObject json) {
        this.munition = json.getInt("munition");
        this.weaponImg = json.getString("weaponImg");
        this.projectileImg = json.getString("projectileImg");
        this.delay = json.getInt("delay");
        this.damagetype = json.getString("damagetype");
        this.damage = json.getInt("damage");
        this.explosionpower = json.getInt("explosionpower");
        this.shockwave = json.getInt("shockwave");

        initialize(); //Draw Crosshair etc
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("munition",munition);
        json.put("weaponImg",weaponImg);
        json.put("projectileImg",projectileImg);
        json.put("delay",delay);
        json.put("damagetype",damagetype);
        json.put("damage",damage);
        json.put("explosionpower",explosionpower);
        json.put("shockwave",shockwave);
        return json;
    }

    private void initialize() {
        this.angle = 0;

        Image weaponImage = new Image(weaponImg, NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true);
        setImage(weaponImage);

        Image crosshairImage = new Image("file:resources/weapons/crosshair.png",NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE,true,true);
        crosshair = new ImageView(crosshairImage);
        angleDraw(true);
    }

    @Override
    public Projectile shoot(int power) throws NoMunitionException{ //ToDo Actually use power
        // ToDo Ask what this is for        
        if(getTranslateY() < -100 || getTranslateX() < -100 ) {
            throw new NullPointerException("weapon is not in use, is at " + getTranslateX() + " " + getTranslateY());
        }
        if(munition > 0) {
        Image image = new Image(projectileImg,NORMED_OBJECT_SIZE / 4, NORMED_OBJECT_SIZE / 4,true,true);
        int yOffset = (int)(NORMED_OBJECT_SIZE-image.getHeight())/2;
        int xOffset = (int)(NORMED_OBJECT_SIZE-image.getWidth())/2;
        Projectile shot = new Projectile(image, new Point2D(getTranslateX()+xOffset, getTranslateY()+yOffset), new Point2D(getCrosshair().getTranslateX()+xOffset, getCrosshair().getTranslateY()+yOffset), 10, this);

        munition--;
        System.out.println("munition left: " + munition);

        resetAngle();
        return shot;
        } else {
            // ToDo Add "0.Schuss"
            throw new NoMunitionException();
        }
    }


    //Getter and Setter
    public ImageView getCrosshair() { return crosshair; }
    public int getDamage() { return damage; }
    public double getAngle() { return angle; }


    //----------------------------------Crosshair-Related Functions--------------------------------- // TODO own class  // Why? Only this and deriving classes are needing those, while some Items might need a different type
    public double toRadian(double grad) { // This function transforms angles to rad which are needed for sin/cos etc.
        return grad * Math.PI / 180;
    }

    public void resetAngle() { angle = 0; }

    @Override
    public void angleUp(boolean faces_right){
        angle = Math.min(90,angle+5);
        angleDraw(faces_right);
    }
    @Override
    public void angleDown(boolean faces_right){
        angle = Math.max(-90, angle - 5);
        angleDraw(faces_right);
    }
    @Override
    public void angleDraw(boolean faces_right){ // Actually only sets the Point and changes facing of weapon; drawn in MapWindow
        if(faces_right){
            crosshair.setTranslateX(getTranslateX() + Math.cos(toRadian(angle)) * RADIUS);
            setScaleX(1); //Reverse mirroring
            setRotate(-angle);
        }
        else{
            crosshair.setTranslateX(getTranslateX() - Math.cos(toRadian(angle))* RADIUS);
            setScaleX(-1); //Mirror Weapon, so its facing left
            setRotate(angle);
        }
        crosshair.setTranslateY(getTranslateY() - Math.sin(toRadian(angle))* RADIUS);
    }
}
