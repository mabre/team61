package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.objects.Weapon;

/**
 * Created by kevin on 21.05.14.
 */
public class Bazooka extends Weapon {
    private final static String  NAME           = "Bazooka";
    private final static String  DESCRIPTION    = "A classic.";

    private final static String  PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    private final static String  WEAPON_IMG     = "file:resources/weapons/temp1.png";
    private final static String  DAMAGETYPE     = "Explosiondamage";
    private final static int     DAMAGE         =  50;
    private final static int     EXPLOSIONPOWER = 100;
    private final static int     SHOCKWAVE      =   0;
    private final static int     DELAY          =  -1; // ToDo somehow tell it's on collision

    private final static boolean POISONS       = false; // toggle isPoisoned
    private final static boolean PARALYZES     = false; // toggle isBurning
    private final static boolean BLOCKS        = false; // toggle isStuck

    private final static int     MASS          =   15;
    private final static int     SPEED         =   20;
    private final static boolean DRIFTS        = true;

//    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------
    public Bazooka(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
    }
}
