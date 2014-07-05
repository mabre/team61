package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.Team;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;

import java.util.ArrayList;

 //Created by kevin on 21.05.14.
 /** An instance of this class represents a weapon, which is part of an Inventory of a {@link de.hhu.propra.team61.Team} <p>
 * which means that each {@link de.hhu.propra.team61.Team} is in possession of a reference to 1 Instance of each Weapontype. <p>
 * All weapons derive from this class and call {@link #Weapon(String, String, int, String, String, int, String, int, int, int, boolean, boolean, boolean, int, boolean, int)}
 * setting up all the variables in here, which are actually constants to the specific types. <p>
 * This class also derives itself from @link #Item, note that @link #Item itself also derives from Stackpane. <p>
 *
 * Here are offered some standard functions, which should prove sufficient for most weapontypes, without special functionality. These are: <p>
 * <ul>
 *  <li> Laying the weaponimage over the figure equipping it </li>
 *  <li> Placing the crosshair relatively to the weaponimage </li>
 *  <li> Aiming </li>
 *  <li> Shooting; Creating @link #projectiles depending and sending its own reference along with it </li>
 *  <li> A default collision handling of the created projectile, which means: </li>
 *   <ul>
 *    <li> coordination of Figure.sufferDamage(); to all figures within a sphere reducing damage dealt with the distance </li>
 *    <li> Sending Figures flying </li>
 *    <li> coordination of terraindestruction </li>
 *   </ul>
 * </ul>
 * Some or all of these might be overriden in deriving classes.
 */
public abstract class Weapon extends Item {
    /** Graphical constant sizes in px */
    private final int NORMED_OBJECT_SIZE = 16;
    private final int NORMED_BLOCK_SIZE  =  8;
    private final int RADIUS = 20; // Distance between Crosshair to Figure
    /** Paths to the Images to be used for the overlay */
    private String projectileImg;
    /** Actual images used for the overlay */
    protected ImageView crosshairImage;

    // ToDo, validate necessity
    private int delay;          // Timedelay, Explode in x seconds etc.
    /**  */
    private String damagetype;  // Firedamage etc. //ToDo validate obsoleteness
    /** Amount of Damage dealt to a Figure with armorrating of 0 with a direct hit */
    private int damage;
    /** Explosive force, which affects the enviromental destruction */
    private int explosionpower;
    /** Multiplyer multiplied on the vector from collisionpoint to figure */
    private int shockwave;
    /** Status conditions */
    private boolean poisons;
    private boolean paralyzes;
    private boolean blocks;
    /** Mass affects acceleration towards the ground of the projectile */
    private int     mass;
    /** Velocity of shot */
    private int     speed;
    /** Boolean to enable toggling affection of wind to projectiles shot by this weapon */
    private boolean drifts;

    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow
    /**
     * Angle aimed at (360° <=> 1 Circle)
     *   0° <=> Horizontal
     *  90° <=> Straight upwards
     * -90° <=> Straight downwards
     * Direction is determined by @link #figure using its getter
     */
    private double angle;       // Angle it is aimed at; 0 <=> Horizontal; +90 <=> straight upwards

    /**
     * Constructor for deriving classes.
     * Sets up the Constants of a Weapontype to this abstract class
     * and places the weapon- and crosshairimages
     *
     * @param name passed upwards to {@link de.hhu.propra.team61.objects.Item}
     * @param description passed upwards to {@link de.hhu.propra.team61.objects.Item}
     * @param munition Number of shots/uses left
     * @param projectileImg String/Path to the image representing the projectile
     * @param delay fusetimer  //ToDo validate if this is needed up here or if it's enough if this stays in the implementations
     * @param damagetype e.g. Firedamage //ToDo same here
     * @param damage Figure.sufferDamage(damage)
     * @param explosionpower Destructive force
     * @param shockwave Propulsion of Objects
     * @param poisons Does this poison a figure?
     * @param paralyzes Does this paralyze a figure?
     * @param blocks Does this block a figure?
     * @param mass used for ballistics
     * @param drifts toggle windaffection
     * @param speed velocity of the shot
     */
    protected Weapon(String name, String description, int munition, String itemImageSRC, String projectileImg, int delay, String damagetype, int damage, int explosionpower, int shockwave, boolean poisons, boolean paralyzes, boolean blocks, int mass, boolean drifts, int speed){
        super(name,description,itemImageSRC);

        this.munition = munition;
        this.projectileImg = projectileImg;
        this.delay = delay;

        this.damagetype = damagetype;
        this.damage = damage;
        this.explosionpower = explosionpower;
        this.shockwave = shockwave;

        this.poisons = poisons;
        this.paralyzes = paralyzes;
        this.blocks = blocks;

        this.mass = mass;
        this.speed = speed;
        this.drifts = drifts;
        this.angle = 0;

        crosshairImage = new ImageView(new Image("file:resources/weapons/crosshair.png",NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE,true,true));
        this.getChildren().add(crosshairImage);

        munitionDisplay = new Label("Uses left: "+this.munition);
        munitionDisplay.setTextFill(Paint.valueOf("Yellow"));
        this.getChildren().add(munitionDisplay);

        setAlignment(Pos.TOP_LEFT);
    }

    @Override
    public Projectile use(Figure user) throws NoMunitionException{
        return shoot(0); //ToDo implement power and replace 0
    }

    /**
     * This function is a default offered. It should prove suitable for most weapons, but some may override.
     * If there is enough munition it returns a Projectile else a NoMunitionException is thrown, which needs handling.
     *
     * The projectile is shot using a vector calculated from the position of the image.
     * Munition is also count down in here and the angle is reset, since the instance is not closed afterwards, but kept instead.
     *
     * @return a Projectile attaching its own reference to it.
     * @throws {@link de.hhu.propra.team61.objects.NoMunitionException} when not enough munition left
     */
    public Projectile shoot(int power) throws NoMunitionException{ //ToDo Actually use power OR calc Power and use
        if(munition > 0) {
            Image image = new Image(projectileImg,NORMED_OBJECT_SIZE / 4, NORMED_OBJECT_SIZE / 4,true,true);
            int yOffset = (int)(NORMED_OBJECT_SIZE-image.getHeight())/2;
            int xOffset = (int)(NORMED_OBJECT_SIZE-image.getWidth())/2;
            Projectile shot = new Projectile(image, new Point2D(itemImage.getTranslateX()+xOffset, itemImage.getTranslateY()+yOffset), new Point2D(crosshairImage.getTranslateX()+xOffset, crosshairImage.getTranslateY()+yOffset), speed, this);

            munition--;
            munitionDisplay.setText("Uses left: "+this.munition);
            System.out.println("munition left: " + munition);

            resetAngle();
            return shot;
        } else {
            if(munition == 0){
                // ToDo Add "0.Schuss"
            }
            throw new NoMunitionException();
        }
    }

    /**
     * This Function is a default. In the sense of that this should be overriden by some classes
     * It coordinates damage and conditions caused to Figures and Terrain.
     * The damage and acceleration of figures is calculated using vectors and their lengths, while
     * terraindestruction is outsourced to @link #Terrain
     *
     * @param terrain reference to {@link de.hhu.propra.team61.objects.Terrain} to call its {@link de.hhu.propra.team61.objects.Terrain#handleExplosion(javafx.geometry.Point2D, int)}
     * @param teams references to all teams in the Round granting access to all Figures in the Round making the damagestep possible
     * @param impactArea Collisionpoint in addition to projectilesize
     * @param isShard Allows different collisionhandling depending on being a shard and weapontype
     * @return A series of commands the server has to send to the clients is returned, containing all destructed blocks and figureupdates
     */
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Rectangle2D impactArea, Boolean isShard){
        ArrayList<String> commandList = new ArrayList<String>();
        commandList.add("REMOVE_FLYING_PROJECTILE");

        // 1. Destroy Terrain and add necessary commands to list being sent
        commandList.addAll(terrain.handleExplosion(new Point2D(impactArea.getMinX(),impactArea.getMinY()), explosionpower));

        // 2. Make Figures suffer/fly and add necessary commands to list being sent
        Point2D impactCenter = new Point2D(impactArea.getMinX(),impactArea.getMinY());
        for(int t = 0; t < teams.size(); t++){
            for(int f = 0; f < teams.get(t).getFigures().size(); f++){
                //Prep Variables
                Figure treatedFigure = teams.get(t).getFigures().get(f);
                int distance = (int)impactCenter.distance(treatedFigure.getPosition());
                //If close to a or a hit
                if(distance < NORMED_BLOCK_SIZE*4){
                    //1.1 make them suffer
                    try {
                        treatedFigure.sufferDamage(damage-distance/NORMED_BLOCK_SIZE);
                        treatedFigure.addRecentlySufferedDamage(damage-distance/NORMED_BLOCK_SIZE);
                    } catch (DeathException e) {
                        System.out.println("WARNING: unhandled death exception");
                        // TODO IMPORTANT
                    }
                    commandList.add("SET_HP " + t + " " + f + " " + treatedFigure.getHealth());
                    if(treatedFigure.getHealth() == 0) {
                        commandList.add("SET_GAME_COMMENT 0 " + generateKillComment(treatedFigure.getName()));
                    }

                    //1.2 Set conditions
                    if(poisons){ commandList.add("CONDITION" + " " + "POISON" + " "  + t + " " + f + " " + "true"); }
                    if(paralyzes){ commandList.add("CONDITION" + " " + "PARALYZE" + " " + t + " " + f + " " + "true"); }
                    if(blocks) { commandList.add("CONDITION" + " " + "STUCK" + " "  + t + " " + f + " " + "true"); }

                    //1.3 make them fly depending on vector
                    Point2D acceleration = treatedFigure.getPosition().subtract(impactCenter); //get direction to send figure flying
                    acceleration = acceleration.normalize().multiply(shockwave); //set strength
                    acceleration = acceleration.add(new Point2D(0,-10)); //avoid to early terraincollision
                    commandList.add("FIGURE_ADD_VELOCITY" + " " + t + " " + f + " " + acceleration.getX() + " " + acceleration.getY());
                }
            }
        }
        //ToDo setRoundTimer down to 5sec
        return commandList;
    }

    /**
     * Generates a random game comment for the death of a figure, depending on its name
     * @param name the name of the killed figure
     * @return a (funny) game comment
     */
    private String generateKillComment(String name) {
        name = name.toLowerCase();
        if(name.startsWith("c") || name.startsWith("k") || name.endsWith("y") || name.endsWith("i") || name.endsWith("e")) {
            return "Oh my God, they killed " + name + "! You bastards!";
        } else {
            return name + " passed on";
        }
    }

     //Getter and Setter
    public String getProjectileImage() { return projectileImg; } //TODO MOVE?
    public double getAngle() { return angle; }
    public int getMass() { return mass; }
    public boolean getDrifts() {
        return drifts;
    }

    //----------------------------------Crosshair-Related Functions---------------------------------
    /**
     * Math.sin() uses Radian-Values[2*PI <=> 1 Circle], this function transforms angles [360° <=> 1Circle]
     * into this scheme by aplying a basic function.
     * @return the respective radianvalue
     */
    public double toRadian(double grad) { // This function transforms angles to rad which are needed for sin/cos etc.
        return grad * Math.PI / 180;
    }

    /** Resetting is necessary since, the instances are kept, until closing the game */
    public void resetAngle() { angle = 0; }

    @Override
    /** Overriden from @link #Item; Enables aiming higher by adjusting angle, setting the maximum to +90° */
    public void angleUp(boolean faces_right){
        angle = Math.min(90, angle + 2);
        angleDraw(faces_right);
    }
    @Override
    /** Overriden from @link #Item; Enables aiming lower by adjusting angle, setting the minimum to -90° */
    public void angleDown(boolean faces_right){
        angle = Math.max(-90, angle - 2);
        angleDraw(faces_right);
    }

    @Override
    public void angleLeft(boolean faces_right){}

    @Override
    public void angleRight(boolean faces_right){}

    @Override
    /**
     * Actually only sets the Point and changes facing of weapon. It is drawn in @link #MapWindow.
     * The weaponimage is also mirrored in here to face the same direction, as the figure holding it.
     * The crosshair is placed with a constant distance around the weaponimage depending on angle and faced direction
     */
    public void angleDraw(boolean faces_right){
        munitionDisplay.setTranslateX(itemImage.getTranslateX() - NORMED_OBJECT_SIZE / 2);
        munitionDisplay.setTranslateY(itemImage.getTranslateY() + NORMED_OBJECT_SIZE + 5);
        if(faces_right){
            crosshairImage.setTranslateX(itemImage.getTranslateX() + Math.cos(toRadian(angle)) * RADIUS);
            itemImage.setScaleX(1); //Reverse mirroring
            itemImage.setRotate(-angle);
        }
        else{
            crosshairImage.setTranslateX(itemImage.getTranslateX() - Math.cos(toRadian (angle))* RADIUS);
            itemImage.setScaleX(-1); //Mirror Weapon, so its facing left
            itemImage.setRotate(angle);
        }
        crosshairImage.setTranslateY(itemImage.getTranslateY() - Math.sin(toRadian(angle)) * RADIUS);
    }
}
