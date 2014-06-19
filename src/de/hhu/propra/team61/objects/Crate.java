package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.MapWindow;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by kevin on 17.06.14.
 */
public class Crate extends ImageView {
    private static final int NORMED_OBJECT_SIZE = 16;
    private static final int NORMED_BLOCK_SIZE  = 8;
    private static final String IMGSRC = "file:/resources/weapons/crate.png";

    private static final int MASS               = 1000;
    private static final int EXPLOSIONPOWER     = 30;
    private static final int DAMAGERSISTANCE    = 10;


        public  static final int JUMP_SPEED = 18 + (int)(MapWindow.GRAVITY.getY() * MASS);
        private static final int FALL_DAMAGE_THRESHOLD = JUMP_SPEED;
        private static final Point2D GRAVEYARD = new Point2D(-1000,-1000);


        /** position of the figure, has to be synced with translateX/Y (introduced to prevent timing issues on JavaFX thread) */
        private Point2D velocity = new Point2D(0,0);
        /** the maximal speed (absolute value) in y direction since last call of resetVelocity, used to limit jump speed */
        private double maxYSpeed = 0;

        private Rectangle2D hitRegion;

    private String content;

        // In and Out
        public Crate(int x, String content){
            // TODO set Content

            setImage(new Image(IMGSRC,NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE,true,true));
            setTranslateX(100*8);//TODO add random
            setTranslateY(20*8);

            hitRegion = new Rectangle2D(getTranslateX(), getTranslateY(),NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE);

            velocity = new Point2D(0,0);

            //ToDo fall to the ground + Wind(?)

            //ToDo add yourself to terrain
        }
}
