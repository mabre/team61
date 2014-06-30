package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.objects.Terrain;
import de.hhu.propra.team61.objects.Weapon;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;

// Created by kevin on 21.05.14.
/**
 * This class extends {@link de.hhu.propra.team61.objects.Weapon} having constant values for damage etc.,<p>
 * which are treated as variables in its superclass. The variables are filled with those constant values.
 */
public class Grenade extends Weapon {
    private final static String  NAME           = "Grenade";
    private final static String  DESCRIPTION    = "Another classic.";

    private final static String  PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    private final static String  WEAPON_IMG     = "file:resources/weapons/grenade.png";
    private final static String  DAMAGETYPE     = "Explosiondamage";
    private final static int     DAMAGE         =  40;
    private final static int     EXPLOSIONPOWER = 100;
    private final static int     SHOCKWAVE      =  15;
    private final static int     DELAY          =   5;  // ToDo make this variable

    private final static boolean POISONS       = false; // toggle isPoisoned
    private final static boolean PARALYZES     = false; // toggle isBurning
    private final static boolean BLOCKS        = false; // toggle isStuck

    private final static int     MASS          =   40;
    private final static int     SPEED         =    8;
    private final static boolean DRIFTS        = true;

    //    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------

    /**
     * Sets up all variables in {@link de.hhu.propra.team61.objects.Weapon}
     *
     * @param munition amount of times this can be used
     */
    public Grenade(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
    }
}
