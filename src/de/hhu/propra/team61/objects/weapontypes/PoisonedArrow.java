package de.hhu.propra.team61.objects.weapontypes;

import de.hhu.propra.team61.objects.Weapon;

/**
 * Created by kevin on 09.06.14.
 */
public class PoisonedArrow extends Weapon {
    private final static String  NAME           = "Poisoned Arrow";
    private final static String  DESCRIPTION    = "To avoid stupid jokes: Don't aim for the knee";

    private final static String  PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    private final static String  WEAPON_IMG     = "file:resources/weapons/temp4.png";
    private final static String  DAMAGETYPE     = "Physicaldamage";
    private final static int     DAMAGE         =  30;
    private final static int     EXPLOSIONPOWER =   0;
    private final static int     SHOCKWAVE      =   0;
    private final static int     DELAY          =  -1; // ToDo somehow tell it's on collision

    private final static boolean POISONS       = true;  // toggle isPoisoned
    private final static boolean PARALYZES     = false; // toggle isBurning
    private final static boolean BLOCKS        = false; // toggle isStuck

    private final static int     MASS          =   10;
    private final static int     SPEED         =   20;
    private final static boolean DRIFTS        = false;

//    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------
    public PoisonedArrow(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
    }
}

