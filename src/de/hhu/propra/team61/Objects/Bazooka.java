package de.hhu.propra.team61.Objects;

import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.Team;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

/**
 * Created by kevin on 21.05.14.
 */
interface BazookaAttributes {
    final String PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    final String WEAPON_IMG     = "file:resources/weapons/temp1.png";
    final String DAMAGETYPE     = "Explosiondamage";
    final int    DAMAGE         =  50;
    final int    EXPLOSIONPOWER = 100;
    final int    SHOCKWAVE      =   0;
    final int    DELAY          =  -1; // ToDo somehow tell it's on collision
}
public class Bazooka extends Weapon implements BazookaAttributes{
//    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow


    public Bazooka(int munition){
        super(munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE);
    }

    public Bazooka(JSONObject json) {
        super(json);
    }
    @Override
    public JSONObject toJson() {
        JSONObject json = super.toJson();
        json.put("type", "Bazooka");

        return json;
    }

    @Override
    /**
     * This Function coordinates damage caused to Figures and Terrain. Also sends Objects flying. //ToDo do this
     * It returns a series of commands the server has to send to the clients
     */
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Rectangle2D impactArea){
        ArrayList<String> commandList = new ArrayList<String>();
        commandList.add("REMOVE_FLYING_PROJECTILE");

        int tCounter = 0;
        for(Team t : teams){ // Calculate all worms hit, lacks hitradius,terraindestruction(non implemented) usw, but for now I'm just assuring same functionality with the adaptions in background
            int fCounter = 0;
            for(Figure f : t.getFigures()){
                if(f.getHitRegion().intersects(impactArea)){ //Give this some more love
                    f.sufferDamage(getDamage());
                    commandList.add("SET_HP " + tCounter + " " + fCounter + " " + f.getHealth());
                }
                fCounter += 1;
            }
            tCounter += 1;
        }
        return commandList;
    }
}
