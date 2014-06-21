package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.objects.*;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.ArrayList;

/**
 * Created by kevin on 20.06.14.
 */
public class Medipack extends Weapon {

    private final static String  NAME           = "Medipack";
    private final static String  DESCRIPTION    = "Alternatively you could man up.";

    private final static String  PROJECTILE_IMG = "";
    private final static String  WEAPON_IMG     = "file:resources/weapons/temp0.png";
    private final static String  DAMAGETYPE     = "Healing";
    private final static int     DAMAGE         = -50;
    private final static int     EXPLOSIONPOWER =   0;
    private final static int     SHOCKWAVE      =   0;
    private final static int     DELAY          =   5;  // ToDo make this variable

    private final static boolean POISONS       = false; // toggle isPoisoned
    private final static boolean PARALYZES     = false; // toggle isBurning
    private final static boolean BLOCKS        = false; // toggle isStuck

    private final static int     MASS          =    0;
    private final static int     SPEED         =    0;
    private final static boolean DRIFTS        = false;

    //    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------
    public Medipack(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
    }

    @Override
    public Projectile shoot(int power) throws NoMunitionException { //Todo kick power
        if(munition > 0) {
            munition--;
            System.out.println("munition left: " + munition);

            return new Projectile(null, new Point2D(getTranslateX(), getTranslateY()), new Point2D(getTranslateX(), getTranslateY()), 0, this);
        } else {
            throw new NoMunitionException();
        }
    }
    @Override
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Rectangle2D impactArea) { //ToDo modify this to make use of a fuse
        for(Team t : teams){
            for(Figure f : t.getFigures()){
                if(impactArea.intersects(f.getHitRegion())){
                    f.setHealth(f.getHealth()-DAMAGE);
                    f.setIsPoisoned(false);
                    f.setIsParalyzed(false);
                }
            }
        }
        return null;
    }


    //Override functions to disable them
    @Override
    public void angleUp(boolean faces_right){}
    @Override
    public void angleDown(boolean faces_right){}
    @Override
    public void angleDraw(boolean faces_right){}
}
