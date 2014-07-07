package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.objects.*;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Created by kevin on 20.06.14.
/**
 * This class extends {@link de.hhu.propra.team61.objects.Item} having constant values for damage etc.,<p>
 * which are treated as variables in its superclass. The variables are filled with those constant values.<p>
 * <p>
 * Abstract functions are also implemented in here.
 */
public class Medipack extends Item {
    private final static int NORMED_OBJECT_SIZE = 16;

    private final static String  NAME           = "Medipack";
    public final static String  DESCRIPTION    = "Come on, that's just a flesh wound.";
    private final static String  ITEM_IMG = "file:resources/weapons/medipack.png";

    private final static String  DAMAGETYPE     = "Healing";
    private final static int     DAMAGE         = -50;

    // ---------------------------------------------------------------------------------------------
    /**
     * Constructor setting up an Instance of {@link de.hhu.propra.team61.objects.Item} implementing necessary <p>
     * functions.
     *
     * @param munition amount of times this Item can be used
     */
    public Medipack(int munition){
        super(NAME,DESCRIPTION,ITEM_IMG);
        this.munition = munition;

        setAlignment(Pos.TOP_LEFT);
    }


    @Override
    /**
     * If enough munition this function counts munition down and calls {@link de.hhu.propra.team61.objects.Figure#digitate()} <p>
     * from given user.
     *
     * @param user Figure using the Item
     * @throws {@link de.hhu.propra.team61.objects.NoMunitionException} if no munition left
     */
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

    /** Override abstract these abstract functions with empty ones to disable them, as they are of no use to this itemtype */
    @Override
    public void angleUp(boolean faces_right){}
    @Override
    public void angleDown(boolean faces_right){}
    @Override
    public void angleLeft(boolean faces_right){}
    @Override
    public void angleRight(boolean faces_right){}
    @Override
    public void angleDraw(boolean faces_right){}
}
