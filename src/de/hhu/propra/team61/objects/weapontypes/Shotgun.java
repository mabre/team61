package de.hhu.propra.team61.objects.weapontypes;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.objects.Terrain;
import de.hhu.propra.team61.objects.Weapon;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;

/**
 * Created by kevin on 08.06.14.
 */
public class Shotgun extends Weapon { //ToDo Override shoot to make two shots possible
    private final static String  NAME           = "Shotgun";
    private final static String  DESCRIPTION    = "Right into the face! - twice";

    private final static String  PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    private final static String  WEAPON_IMG     = "file:resources/weapons/temp3.png";
    private final static String  DAMAGETYPE     = "Physicaldamage";
    private final static int     DAMAGE         =  25;
    private final static int     EXPLOSIONPOWER =  40;
    private final static int     SHOCKWAVE      =   0;
    private final static int     DELAY          =  -1; // ToDo somehow tell it's on collision

    private final static boolean POISONS       = false; // toggle isPoisoned
    private final static boolean PARALYZES     = false; // toggle isBurning
    private final static boolean BLOCKS        = false; // toggle isStuck

    private final static int     MASS          = 1;
    private final static int     SPEED         = 20;
    private final static boolean DRIFTS        = false;

//    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------
    public Shotgun(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
    }

    @Override
    /**
     * This Function coordinates damage caused to Figures and Terrain.
     * It returns a series of commands the server has to send to the clients
     */
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Rectangle2D impactArea){
        return super.handleCollision(terrain, teams, impactArea);
    }
}
