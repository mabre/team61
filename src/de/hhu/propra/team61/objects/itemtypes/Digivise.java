package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.objects.Figure;
import de.hhu.propra.team61.objects.Item;
import de.hhu.propra.team61.objects.NoMunitionException;
import de.hhu.propra.team61.objects.Projectile;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by kevin on 22.06.14. as requested by markus along with the specific typo
 */
public class Digivise extends Item {
    private final static int NORMED_OBJECT_SIZE = 16;

    private final static String  NAME           = "Digivise"; //Yes, really with s not c
    private final static String  DESCRIPTION    = "Remember, if you need to force it, it's usually shit.";

    private final static String  PROJECTILE_IMG = "";
    private final static String  WEAPON_IMG     = "file:resources/weapons/digivise.png";

    private ImageView weaponImage;

    // ---------------------------------------------------------------------------------------------
    public Digivise(int munition){
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

            user.digitate();

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

