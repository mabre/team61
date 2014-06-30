package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.objects.Figure;
import de.hhu.propra.team61.objects.Item;
import de.hhu.propra.team61.objects.NoMunitionException;
import de.hhu.propra.team61.objects.Projectile;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Created by kevin on 22.06.14. as requested by markus along with the specific typo
/**
 * This class extends {@link de.hhu.propra.team61.objects.Item} having constant values for damage etc.,<p>
 * which are treated as variables in its superclass. The variables are filled with those constant values.<p>
 * <p>
 * Abstract functions are also implemented in here.
 */
public class Digiwise extends Item {
    private final static int NORMED_OBJECT_SIZE = 16;

    private final static String  NAME           = "Digiwise"; //Yes, it is supposed to be written this way. Blame markus
    public final static String  DESCRIPTION     = "Remember, if you need to force it, it's usually shit.";
    private final static String ITEM_IMG = "file:resources/weapons/digiwise.png";

    // ---------------------------------------------------------------------------------------------
    /**
     * Constructor setting up an Instance of {@link de.hhu.propra.team61.objects.Item} implementing necessary <p>
     * functions.
     *
     * @param munition amount of times this Item can be used
     */
    public Digiwise(int munition){
        super(NAME,DESCRIPTION,ITEM_IMG);
        this.munition = munition;

        itemImage = new ImageView(new Image(ITEM_IMG, NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true));
        this.getChildren().add(itemImage);

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

            user.digitate();

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

