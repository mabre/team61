package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.objects.Weapon;
import javafx.scene.shape.Line;

/**
 * Created by kevin on 21.06.14.
 */
public class Rifle extends Weapon {
    private final static String  NAME           = "Rifle";
    private final static String  DESCRIPTION    = "Camper!";

    private final static String  PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    private final static String  WEAPON_IMG     = "file:resources/weapons/temp2.png";
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

    //    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------
    public Rifle(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
    }

    @Override
    public void angleDraw(boolean faces_right){ // Actually only sets the Point and changes facing of weapon; drawn in MapWindow
        //Hide original crosshair
        crosshairImage.setTranslateX(-1000);
        crosshairImage.setTranslateY(-1000);

        //TODO: this.getChildren().remove?;
        //Draw redDot instead
        Line redDot = new Line();
        redDot.setStartX(getTranslateX());
        redDot.setStartY(getTranslateY());


        this.getChildren().add(redDot);
    }
}
