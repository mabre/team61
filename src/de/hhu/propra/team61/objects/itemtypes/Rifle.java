package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.objects.Weapon;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.transform.Translate;

/**
 * Created by kevin on 21.06.14.
 */
public class Rifle extends Weapon {
    private final static int NORMED_OBJECT_SIZE = 16;

    private final static String  NAME           = "Rifle";
    private final static String  DESCRIPTION    = "Camper!";

    private final static String  PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    private final static String  WEAPON_IMG     = "file:resources/weapons/rifle.png";
    private final static String  DAMAGETYPE     = "Physicaldamage";
    private final static int     DAMAGE         =  40;
    private final static int     EXPLOSIONPOWER =  10;
    private final static int     SHOCKWAVE      =  10;
    private final static int     DELAY          =  -1; // ToDo somehow tell it's on collision

    private final static boolean POISONS       = false; // toggle isPoisoned
    private final static boolean PARALYZES     = false; // toggle isBurning
    private final static boolean BLOCKS        = false; // toggle isStuck

    private final static int     MASS          =    0;
    private final static int     SPEED         =   36;
    private final static boolean DRIFTS        = false;

    private Line redDot;

    //    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------
    public Rifle(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
        //Hide crosshair
        this.getChildren().remove(crosshairImage);
        //Draw redDot instead
        redDot = new Line();
        redDot.setStroke(Paint.valueOf("Red"));
        this.getChildren().add(redDot);
    }

    @Override
    public void angleDraw(boolean faces_right){ // Actually only sets the Point and changes facing of weapon; drawn in MapWindow
        super.angleDraw(faces_right);
        int xOffset = NORMED_OBJECT_SIZE / 2;
        int yOffset = NORMED_OBJECT_SIZE / 2;
        //set absolute position
        redDot.setTranslateX(weaponImage.getTranslateX());
        redDot.setTranslateY(weaponImage.getTranslateY());
        //draw relatively from there
        redDot.setStartX(xOffset);
        redDot.setStartY(yOffset);
        redDot.setEndX(10);
        redDot.setEndY(10 * Math.sin(toRadian(getAngle())));

        System.out.println(redDot.toString()); // TODO this whole redDot-thing doesnt't work..
/*
        //relative Endpoint
        if(faces_right){
            redDot.setEndX(Math.sin(toRadian(getAngle())) * NORMED_OBJECT_SIZE);
        }
        else{
            redDot.setEndX(-Math.sin(toRadian(getAngle()))*NORMED_OBJECT_SIZE);
        }
        redDot.setEndY(Math.sin(toRadian(getAngle()))*NORMED_OBJECT_SIZE);*/
    }
}
