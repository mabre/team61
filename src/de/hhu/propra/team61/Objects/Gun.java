package de.hhu.propra.team61.Objects;

import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.Team;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.ArrayList;

/**
 * Created by kevgny on 21.05.14.
 */
public class Gun extends Weapon {
    private final int NORMED_OBJECT_SIZE = 16; // ToDo I guess this needs to be moved upwards
    private final String PROJECTILE_IMG = "file:resources/weapons/temp0.png";

    public Gun(String path, int damage, int munition){
        super(damage, munition);

        imagePath = path;
        Image image = new Image(imagePath, NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true);
        setImage(image);
    }

    public Gun(JSONObject json) {
        super(json);
    }

    @Override
    public Projectile shoot(int power) throws NoMunitionException {
        //ToDo Actually use power

        // ToDo Ask what this is for
        if(getTranslateY() < -100 || getTranslateX() < -100 ) {
            throw new NullPointerException("weapon is not in use, is at " + getTranslateX() + " " + getTranslateY());
        }
        if(munition > 0) {
            Image image = new Image(PROJECTILE_IMG,NORMED_OBJECT_SIZE / 4, NORMED_OBJECT_SIZE / 4,true,true);
            int offset = (int)(NORMED_OBJECT_SIZE-image.getHeight())/2;
            Projectile shot = new Projectile(image, new Point2D(getTranslateX()+offset, getTranslateY()+offset), new Point2D(getCrosshair().getTranslateX()+offset, getCrosshair().getTranslateY()+offset), 10, this);
            munition--;
            System.out.println("munition left: " + munition);
            resetAngle();
            return shot;
        } else {
            // ToDo Add "0.Schuss"
            throw new NoMunitionException();
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = super.toJson();
        json.put("type", "Gun");

        return json;
    }

    @Override
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
