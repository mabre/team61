package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.objects.NoMunitionException;
import de.hhu.propra.team61.objects.Projectile;
import de.hhu.propra.team61.objects.Terrain;
import de.hhu.propra.team61.objects.Weapon;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.ArrayList;

/**
 * Created by kevin on 25.06.14.
 */
public class Bananabomb extends Weapon {
    private final static String  NAME           = "Bananabomb";
    private final static String  DESCRIPTION    = "Do not plug this into your ear."; //TODO @diniiantares you know charly better than I do

    private final static String  PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    private final static String  WEAPON_IMG     = "file:resources/weapons/temp0.png";
    private final static String  DAMAGETYPE     = "Explosiondamage";
    private final static int     DAMAGE         =  40;
    private final static int     EXPLOSIONPOWER = 100;
    private final static int     SHOCKWAVE      =  15;
    private final static int     SHARDS         =   5;
    private final static int     DELAY          =   5;  // ToDo make this variable

    private final static boolean POISONS       = false; // toggle isPoisoned
    private final static boolean PARALYZES     = false; // toggle isBurning
    private final static boolean BLOCKS        = false; // toggle isStuck

    private final static int     MASS          =   40;
    private final static int     SPEED         =    8;
    private final static boolean DRIFTS        = true;

    //    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------
    public Bananabomb(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
    }

    @Override
    /**
     * This Function coordinates damage caused to Figures and Terrain.
     * It also produces the shards.
     * It returns a series of commands the server has to send to the clients
     */
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Rectangle2D impactArea, Boolean isShard) { //ToDo modify this to make use of a fuse
        ArrayList<String> output = new ArrayList<>();
        JSONArray shards = new JSONArray();
        Point2D impactPoint = new Point2D(impactArea.getMinX(), impactArea.getMinY());

        output.addAll(super.handleCollision(terrain, teams, impactArea,isShard));

        if(!isShard) {
            for (int i = 0; i < SHARDS; i++) {
                Projectile shot = new Projectile(new Image(PROJECTILE_IMG,4,4,true,true), impactPoint, impactPoint.add(-Math.random() * 10, -Math.random() * 10), 8, this);
                shot.setShard();
                shards.put(shot.toJson());
            }
            output.add("ADD_FLYING_PROJECTILE " + shards);
        }
        return output;
    }
}
