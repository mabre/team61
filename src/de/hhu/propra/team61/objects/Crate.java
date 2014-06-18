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
    private static final int NORMED_BLOCK_SIZE  =  8;

        private static final int MASS = 1000;
        public  static final int JUMP_SPEED = 18 + (int)(MapWindow.GRAVITY.getY() * MASS);
        private static final int FALL_DAMAGE_THRESHOLD = JUMP_SPEED;
        private static final Point2D GRAVEYARD = new Point2D(-1000,-1000);

        private static final int DAMAGERSISTANCE = 10;

        /** position of the figure, has to be synced with translateX/Y (introduced to prevent timing issues on JavaFX thread) */
        private Point2D velocity = new Point2D(0,0);
        /** the maximal speed (absolute value) in y direction since last call of resetVelocity, used to limit jump speed */
        private double maxYSpeed = 0;

        private Rectangle2D hitRegion;

    private String content;

        // In and Out
        public Crate(Terrain terrain){
            // TODO set Content
            setTranslateX(Math.random()*terrain.toArrayList().size()*NORMED_BLOCK_SIZE);
            setImage(new Image("file:/resources/weapons/crate.png",NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE,true,true));
            hitRegion = new Rectangle2D(getTranslateX(), getTranslateY(),NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE);
        }

      /*  public Crate(JSONObject input){
            position = new Point2D(input.getDouble("position.x"), input.getDouble("position.y"));
            setTranslateX(position.getX());
            setTranslateY(position.getY());

            this.content = input.getString("content");
        }*/

        public JSONObject toJson(){
            JSONObject output = new JSONObject();
            output.put("content", content);

            output.put("position.x", getTranslateX()); // TODO save as array
            output.put("position.y", getTranslateY());
            return output;
        }

        /**
         * moves the figure to the given position, updated the hit region, position of the figure image and the hp label
         * JavaFX parts are run with runLater, hence it is save to call this function from non-fx threads.
         * @param newPosition the new position of the figure in px
         */
        public void setPosition(Point2D newPosition) {
            setTranslateX(newPosition.getX());
            setTranslateY(newPosition.getY());
            hitRegion = new Rectangle2D(getTranslateX(),getTranslateY(),hitRegion.getWidth(),hitRegion.getHeight());
        }

        public Point2D getPosition() {
            return new Point2D(getTranslateX(),getTranslateY());
        }

        public void sufferDamage(int damage) throws DeathException {
            if(DAMAGERSISTANCE-damage <= 0) {
                setPosition(GRAVEYARD);
                //ToDo throw new DeathException(this);
            }
        }

        public Rectangle2D getHitRegion() {
            return hitRegion;
        }

        public Point2D getVelocity() {
            return velocity;
        }

        /**
         * resets the velocity vector to 0 and - depending on the speed - the figure suffers fall damage
         */
        public void resetVelocity() throws DeathException {
            int fallDamage = (int)(velocity.magnitude() - FALL_DAMAGE_THRESHOLD);

            if(!velocity.equals(MapWindow.GRAVITY.multiply(MASS))) { // do not print when "default gravity" is applied when figures are standing on ground
                System.out.println("v="+velocity.magnitude() + ", fall damage: " + fallDamage);
            }
            velocity = new Point2D(0,0);
            maxYSpeed = 0;

            if(fallDamage > 0) {
                sufferDamage(fallDamage);
            }
        }

        public void addVelocity(Point2D dV) { // TODO interface?
            velocity =  velocity.add(dV);
            if(maxYSpeed < Math.abs(velocity.getY())) {
                maxYSpeed = Math.abs(velocity.getY());
            }
        }

        public int getMass() {
            return MASS;
        }
}
