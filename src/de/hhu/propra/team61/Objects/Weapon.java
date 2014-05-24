package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by kevgny on 21.05.14.
 *
 * Abstract class containing all variables and methods which all Weapons have in common
 * This class is also responsible for the Crosshair
 */
public abstract class Weapon extends Item {
    private final int distance = 15; // Distance between Crosshair to Figure

    //ToDo validate necesity of all of them
    private String name;        //
    private String description; //

    private String damagetype;  // Firedamage etc.
    private int munitions;      // ?
    private int damage;         // Damage to Figures
    private int explosionpower; // Damage to Enviroment
    private int delay;          // Timedelay, Explode in x seconds etc.

    private double angle;       // Angle it is aimed at; 0 <=> Horizontal; +90 <=> straight upwards
    private ImageView crosshair;
    private int velocity;       // Power of shot, affects distance, flightspeed etc.

    public Weapon(Point2D pos,boolean facing_right,String path,int damage){ //ToDo Replace variables with something better?
        this.angle = 0;
        this.damage = damage;

        this.setTranslateX(8 * pos.getX());
        this.setTranslateY(8 * pos.getY());
        Image image = new Image(path, 8, 8, true, true); //ToDo Replace with an actual Weapon
        setImage(image);

        crosshair = new ImageView("file:resources/weapons/crosshair.png");
        angle_draw(facing_right);
    };

    //Getter and Setter
    /*ToDo if necessary*/
    public double getAngle(){
        return angle;
    }
    public ImageView getCrosshair() { return crosshair; }
    public int getDamage() { return damage; }

    public Projectile shoot(){  // Overwritten in SubClasses, but creates specific projectile etc.
        //TODO do more?
        Projectile shot = new Projectile(new Image("file:resources/weapons/temp0.png"),new Point2D(getX(),getY()),new Point2D(crosshair.getX(),crosshair.getY()),20,damage);
        return shot;
    }



    //----------------------------------Crosshair-Related Functions---------------------------------
    public double toRadian(double grad) { // This function transforms angles to rad which are needed for sin/cos etc.
        return grad * Math.PI / 180;
    }


    public void angle_up(boolean faces_right){
        if(angle < 90){ //THEN: crosshair is not straight upwards useful for rotation of weapon
            angle += 5;
            angle_draw(faces_right);
        }
    }
    public void angle_down(boolean faces_right){
        if(angle > -90){ //THEN: crosshair is not straight downwards
            angle -= 5;
            angle_draw(faces_right);
        }
    }
    public void angle_draw(boolean faces_right){ // Actually only sets the Point and changes facing of weapon/ drawn in MapWindow
        if(faces_right){
            crosshair.setTranslateX(getTranslateX() + Math.cos(toRadian(angle)) * distance);
            setScaleX(1); //Reverse mirroring
            setRotate(-angle);
        }
        else{
            crosshair.setTranslateX(getTranslateX() - Math.cos(toRadian(angle))*distance);
            setScaleX(-1); //Mirror Weapon, so its facing left
            setRotate(angle);
        }
        crosshair.setTranslateY(getTranslateY() - Math.sin(toRadian(angle))*distance);
    }
}
