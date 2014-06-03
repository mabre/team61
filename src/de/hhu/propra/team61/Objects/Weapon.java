package de.hhu.propra.team61.Objects;

import de.hhu.propra.team61.IO.JSON.JSONObject;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by kevgny on 21.05.14.
 *
 * Abstract class containing all variables and methods which all Weapons have in common
 * This class is also responsible for the Crosshair
 */
public abstract class Weapon extends Item {
    private final int RADIUS = 25; // Distance between Crosshair to Figure

    //ToDo validate necessity of all of them
    private int delay;          // Timedelay, Explode in x seconds etc.

    private String damagetype;  // Firedamage etc.
    private int damage;         // Damage to Figures
    private int explosionpower; // Damage to environment

    private int velocity;       // Power of shot, affects RADIUS, flightspeed etc.

    private double angle;       // Angle it is aimed at; 0 <=> Horizontal; +90 <=> straight upwards
    private ImageView crosshair;


    public Weapon(int damage, int munition){ //ToDo Replace variables with something better
        this.angle = 0;
        this.damage = damage;
        this.munition = munition;

        initialize(); //Draw Crosshair etc
    }
    public Weapon(JSONObject json) {
        this.munition = json.getInt("munition");
        this.damage = json.getInt("damage");
        this.imagePath = json.getString("imagePath");
        Image image = new Image(imagePath, 16, 16, true, true);
        setImage(image);

        initialize(); //Draw Crosshair etc
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("munition", munition);
        json.put("damage", damage);
        json.put("imagePath", imagePath);
        return json;
    }

    private void initialize() {
        Image image = new Image("file:resources/weapons/crosshair.png",16,16,true,true);
        crosshair = new ImageView(image);
        angleDraw(true);
    }

    //Getter and Setter
    /*ToDo if necessary*/
    public ImageView getCrosshair() { return crosshair; }
    public int getDamage() { return damage; }

    @Override
    public abstract Projectile shoot() throws NoMunitionException;


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
