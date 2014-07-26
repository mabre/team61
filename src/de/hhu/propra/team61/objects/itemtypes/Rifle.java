package de.hhu.propra.team61.objects.itemtypes;

import de.hhu.propra.team61.objects.Weapon;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

// Created by kevin on 21.06.14.
/**
 * This class extends {@link de.hhu.propra.team61.objects.Weapon} having constant values for damage etc.,<p>
 * which are treated as variables in its superclass. The variables are filled with those constant values.<p>
 * <p>
 * The {@link de.hhu.propra.team61.objects.Weapon#projectileImg} is replaced with a red Line in here to <p>
 * allow more accurate aiming.
 */
public class Rifle extends Weapon {
    private final static int NORMED_OBJECT_SIZE = 16;

    private final static String  NAME           = "Rifle";
    public final static String   DESCRIPTION    = "Camper!";

    private final static String  PROJECTILE_IMG = "file:resources/weapons/temp0.png";
    private final static String  WEAPON_IMG     = "file:resources/weapons/rifle.png";
    private final static String  DAMAGETYPE     = "Physicaldamage";
    private final static int     DAMAGE         =  40;
    private final static int     EXPLOSIONPOWER =  10;
    private final static int     SHOCKWAVE      =  10;
    private final static int     DELAY          =  -1; // ToDo somehow tell it's on collision

    private final static boolean POISONS       = false; // toggle isPoisoned
    private final static boolean PARALYZES     = false; // toggle isBurning
    private final static boolean BLOCKS        = false; // toggle isStuck

    private final static int     MASS          =    0;
    private final static int     SPEED         =   36;
    private final static boolean DRIFTS        = false;
    private static final double MAX_POINTER_LENGTH = 1000;

    /** holds the width of the loaded terrain (must be set externally),so that the rifle knows how long the pointer may be
     * if not set correctly, the pointer might move the terrain around when aiming down */
    private static int terrainWidth;
    private static int terrainHeight;

    private Line redDot;

    //    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    // ---------------------------------------------------------------------------------------------
    /**
     * This constructor firstly sets up the variables, then removes the crosshair added in {@link de.hhu.propra.team61.objects.Weapon} <p>
     * from the Stackpane making it invisible and creates the line.
     *
     * @param munition amount of times this can be used
     */
    public Rifle(int munition){
        super(NAME,DESCRIPTION,munition,WEAPON_IMG,PROJECTILE_IMG,DELAY,DAMAGETYPE,DAMAGE,EXPLOSIONPOWER,SHOCKWAVE,POISONS,PARALYZES,BLOCKS,MASS,DRIFTS,SPEED);
        //Hide crosshair
        this.getChildren().remove(crosshairImage);
        //Draw redDot instead
        redDot = new Line();
        redDot.setStroke(Paint.valueOf("Red"));
        this.getChildren().add(redDot);
    }

    /**
     * Actually only sets the Point and changes facing of weapon. It is drawn in MapWindow.
     * This function needs to override {@link de.hhu.propra.team61.objects.Weapon#angleDraw(boolean)} since
     * the red Line is not handled up there.
     */
    @Override
    public void angleDraw(boolean facesRight){
        super.angleDraw(facesRight);
        int xOffset = NORMED_OBJECT_SIZE / 2;
        int yOffset = NORMED_OBJECT_SIZE / 2;

        for(int i=50; i>0; i--) { // TODO hacky + dirty workaround: shorten line until it fits within level
            //draw relatively from there
            redDot.setStartX(0);
            redDot.setStartY(0);
            double endX = MAX_POINTER_LENGTH*i/50 * Math.cos(Math.toRadians(getAngle()));
            redDot.setEndX(endX);
            double endY = -MAX_POINTER_LENGTH*i/50 * Math.sin(Math.toRadians(getAngle()));
            redDot.setEndY(endY);
            // move the upper end of the canvas (this is at the position of the weapon), so that the line does not tilt away
            redDot.setTranslateY(itemImage.getTranslateY() + Math.min(endY, 0) + yOffset);
            if(facesRight) {
                redDot.setTranslateX(itemImage.getTranslateX() + xOffset);
                redDot.setScaleX(1);
                // do not further shorten when end point is within level
                if(endX + redDot.getTranslateX() > 0 && endX + redDot.getTranslateX() < terrainWidth) {
                    if(getAngle() < 0) { // ie. if aiming down
                        if(endY + redDot.getTranslateY() > 0 && endY + redDot.getTranslateY() < terrainHeight) break;
                    } else {
                        if(-endY < terrainHeight) break;
                    }
                }
            } else {
                redDot.setTranslateX(itemImage.getTranslateX() + xOffset - endX);
                redDot.setScaleX(-1);
                if(redDot.getTranslateX() > 0 && redDot.getTranslateX() < terrainWidth) {
                    if(getAngle() < 0) {
                        if(endY + redDot.getTranslateY() > 0 && endY + redDot.getTranslateY() < terrainHeight) break;
                    } else {
                        if(-endY < terrainHeight) break;
                    }
                }
            }
        }
    }

    public static void setTerrainWidth(int terrainWidth) {
        Rifle.terrainWidth = terrainWidth;
    }

    public static void setTerrainHeight(int terrainHeight) {
        Rifle.terrainHeight = terrainHeight;
    }
}
