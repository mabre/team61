package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.objects.*;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

/**
 * Created by kevin on 20.06.14.
 */
public class Medipack extends Item {
    private final static int NORMED_OBJECT_SIZE = 16;

    private final static String  NAME           = "Medipack";
    private final static String  DESCRIPTION    = "Come on, that's just a flesh wound.";

    private final static String  PROJECTILE_IMG = "";
    private final static String  WEAPON_IMG     = "file:resources/weapons/medipack.png";
    private final static String  DAMAGETYPE     = "Healing";
    private final static int     DAMAGE         = -50;

    private ImageView weaponImage;

    // ---------------------------------------------------------------------------------------------
    public Medipack(int munition){
        super(NAME,DESCRIPTION);
        this.munition = munition;

        weaponImage = new ImageView(new Image(WEAPON_IMG, NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true));
        this.getChildren().add(weaponImage);

        setAlignment(Pos.TOP_LEFT);
    }

    public void setPosition(Point2D pos){
        weaponImage.setTranslateX(pos.getX());
        weaponImage.setTranslateY(pos.getY());
    }

    @Override
    public Projectile use(Figure user) throws NoMunitionException {
        if(munition > 0) {
            munition--;
            System.out.println("munition left: " + munition);

            user.setHealth(user.getHealth()-DAMAGE);
            user.setIsPoisoned(false);
            user.setIsParalyzed(false);

            return null;
        } else {
            throw new NoMunitionException();
        }
    }


    //Override these functions to disable them, as they are of no use here
    @Override
    public void angleUp(boolean faces_right){}
    @Override
    public void angleDown(boolean faces_right){}
    @Override
    public void angleDraw(boolean faces_right){}
}
