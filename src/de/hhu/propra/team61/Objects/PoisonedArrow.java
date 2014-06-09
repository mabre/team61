package de.hhu.propra.team61.Objects;

import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.Team;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;

/**
 * Created by kevin on 09.06.14.
 */

interface PoisonedArrowAttributes {
    final String  NAME           = "Poisoned Arrow";
    final String  DESCRIPTION    = "To avoid stupid jokes: Don't aim for the knee";

    final String  PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    final String  WEAPON_IMG     = "file:resources/weapons/temp4.png";
    final String  DAMAGETYPE     = "Explosiondamage";
    final int     DAMAGE         =  30;
    final int     EXPLOSIONPOWER =   0;
    final int     SHOCKWAVE      =   0;
    final int     DELAY          =  -1; // ToDo somehow tell it's on collision

    final boolean POISONS       = true;  // toogle isPoisoned
    final boolean IGNITES       = false; // toogle isBurning
    final boolean BLOCKS        = false; // toogle isStuck
}
public class PoisonedArrow extends Weapon implements PoisonedArrowAttributes{
//    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow


    public PoisonedArrow(int munition){
        super(munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,IGNITES,BLOCKS);
    }

    public PoisonedArrow(JSONObject json) {
        super(json);
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = super.toJson();
        json.put("type", "PoisonedArrow");

        return json;
    }

    @Override
    /**
     * This Function coordinates damage caused to Figures and Terrain.
     * It returns a series of commands the server has to send to the clients
     */
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Rectangle2D impactArea){
        ArrayList<String> commandList = new ArrayList<String>();
        commandList.add("REMOVE_FLYING_PROJECTILE");

        int tCounter = 0;
        for(Team t : teams){
            int fCounter = 0;
            for(Figure f : t.getFigures()){
                if(f.getHitRegion().intersects(impactArea)){ //Give this some more love
                    f.sufferDamage(getDamage());
                    if(POISONS){ f.setIsPoisoned(true); }
                  /*  if(IGNITES){ f.setIsPoisoned(true); }
                    if(BLOCKS){ f.setIsPoisoned(true); }*/
                    commandList.add("SET_HP " + tCounter + " " + fCounter + " " + f.getHealth());
                }
                fCounter += 1;
            }
            tCounter += 1;
        }
        return commandList;
    }
}

