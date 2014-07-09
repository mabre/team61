package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.objects.NoMunitionException;
import de.hhu.propra.team61.objects.Projectile;
import de.hhu.propra.team61.objects.Terrain;
import de.hhu.propra.team61.objects.Weapon;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.ArrayList;

/**
 * This class extends {@link de.hhu.propra.team61.objects.Weapon} having constant values for damage etc.,<p>
 * which are treated as variables in its superclass. The variables are filled with those constant values.
 */
public class Skip extends Weapon {
    private final static String  NAME           = "Skip";
    public final static String  DESCRIPTION     = "Sit out a round.";

    private final static String  PROJECTILE_IMG = "file:resources/weapons/empty.png";
    private final static String  WEAPON_IMG     = "file:resources/weapons/empty.png";
    private final static String  SKIP_IMG       = "file:resources/weapons/skip.png";
    private final static String  DAMAGETYPE     = "None";
    private final static int     DAMAGE         =   0;
    private final static int     EXPLOSIONPOWER =   0;
    private final static int     SHOCKWAVE      =   0;
    private final static int     DELAY          =  -1; // ToDo somehow tell it's on collision

    private final static boolean POISONS       = false; // toggle isPoisoned
    private final static boolean PARALYZES     = false; // toggle isBurning
    private final static boolean BLOCKS        = false; // toggle isStuck

    private final static int     MASS          = 0;
    private final static int     SPEED         = 2000;
    private final static boolean DRIFTS        = false;

//    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------

    /**
     * Constructor setting up the {@link de.hhu.propra.team61.objects.Weapon} correctly.
     * The crosshair is replaced in here.
     *
     * @param munition amount of times, this can be used.
     */
    public Skip(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
        crosshairImage.setImage(new Image(SKIP_IMG));
    }

    /**
     * Calls standard-shoot function from {@link de.hhu.propra.team61.objects.Weapon} and re-adds the lost munition
     * to simulate infinite munition
     *
     * @param power power of shot
     * @return a technical invisible, not-harming projectile to call endTurn() on collision
     * @throws NoMunitionException
     */
    public Projectile shoot(int power) throws NoMunitionException { //ToDo Actually use power OR calc Power and use
        Projectile temp = super.shoot(power);
        munition++;
        return temp;
    }

        /**
         * This Function coordinates damage caused to Figures and Terrain.
         * It returns a series of commands the server has to send to the clients
         */
    @Override
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Rectangle2D impactArea, Boolean isShard){
        return super.handleCollision(terrain, teams, impactArea, isShard);
    }

}
