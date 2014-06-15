package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.Team;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

/**
 * Created by kevin on 21.05.14.
 *
 * This class is responsible:
 * - for the Crosshair
 * - for the Weaponimage/-facing
 * - shooting
 * - transmission of all the attributes by carrying them on a projectile
 * - OFFERS a default handleCollision(), which might prove suitable for most weapontypes
 *   -> coordination of Figure.sufferDamage();
 *   -> Sending Figures flying // ToDo implement this
 *   -> coordination of terraindestruction
 *
 *  Deriving classes:
 * - Have and set up nearly all attributes
 * - Override defaults if necessary
 */
public abstract class Weapon extends Item {
    private final int NORMED_OBJECT_SIZE = 16; //ToDo Move this? Like in Projectile
    private final int RADIUS = 20; // Distance between Crosshair to Figure

    private String projectileImg;
    private String weaponImg;

    private int delay;          // Timedelay, Explode in x seconds etc.

    private String damagetype;  // Firedamage etc.
    private int damage;         // Damage to Figures
    private int explosionpower; // Damage to environment
    private int shockwave;      // Throw Figures away from Collisionpoint

    private boolean poisons;    // toggle isPoisoned
    private boolean ignites;    // toggle isBurning
    private boolean blocks;     // toggle isStuck

    private int     mass;       // toggle if gravity affects projectile
    private int     speed;
    private boolean drifts;     // toggle if wind affects projectile    //ToDo pass this to projectile

    private int velocity;       // Power of shot, affects distance, flightspeed etc. //ToDo check if this will not be implemented as power in MapWindow

    private double angle;       // Angle it is aimed at; 0 <=> Horizontal; +90 <=> straight upwards
    private ImageView crosshair;

    /**
     * Constructor for deriving classes.
     * Sets up the Constants of a Weapontype to this abstract class
     *
     * @param name
     * @param description
     * @param munition Number of shots left
     * @param weaponImg String/Path to the Image representing the Weapon
     * @param projectileImg String/Path to the image representing the projectile
     * @param delay fusetimer  //ToDo validate if this is needed up here or if it's enough if this stays in the implementations
     * @param damagetype e.g. Firedamage //ToDo same here
     * @param damage Figure.sufferDamage(damage)
     * @param explosionpower Destructive force
     * @param shockwave Propulsion of Objects
     * @param poisons Does this poison a figure?
     * @param ignites Does this ignite a figure?
     * @param blocks Does this block a figure?
     * @param mass used for ballistics
     * @param drifts toggle windaffection
     */
    protected Weapon(String name, String description, int munition, String weaponImg, String projectileImg, int delay, String damagetype, int damage, int explosionpower, int shockwave, boolean poisons, boolean ignites, boolean blocks, int mass, boolean drifts, int speed){
        super(name,description);

        this.munition = munition;
        this.weaponImg = weaponImg;
        this.projectileImg = projectileImg;
        this.delay = delay;

        this.damagetype = damagetype;
        this.damage = damage;
        this.explosionpower = explosionpower;
        this.shockwave = shockwave;

        this.poisons = poisons;
        this.ignites = ignites;
        this.blocks = blocks;

        this.mass = mass;
        this.speed = speed;
        this.drifts = drifts;

        initialize(); //Draw Crosshair etc
    }

    private void initialize() {
        this.angle = 0;

        Image weaponImage = new Image(weaponImg, NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true);
        setImage(weaponImage);

        Image crosshairImage = new Image("file:resources/weapons/crosshair.png",NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE,true,true);
        crosshair = new ImageView(crosshairImage);
        angleDraw(true);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("name",name);
        json.put("munition",munition);
        return json;
    }

    @Override
    /**
     * This function is a default offered. It should prove suitable for most weapons.
     * If there is enough munition it returns a Projectile
     */
    public Projectile shoot(int power) throws NoMunitionException{ //ToDo Actually use power OR calc Power and use
        if(munition > 0) {
            Image image = new Image(projectileImg,NORMED_OBJECT_SIZE / 4, NORMED_OBJECT_SIZE / 4,true,true);
            int yOffset = (int)(NORMED_OBJECT_SIZE-image.getHeight())/2;
            int xOffset = (int)(NORMED_OBJECT_SIZE-image.getWidth())/2;
            Projectile shot = new Projectile(image, new Point2D(getTranslateX()+xOffset, getTranslateY()+yOffset), new Point2D(getCrosshair().getTranslateX()+xOffset, getCrosshair().getTranslateY()+yOffset), speed, this);

            munition--;
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
     * It returns a series of commands the server has to send to the clients
     */
    public ArrayList<String> handleCollision(Terrain terrain, ArrayList<Team> teams, Rectangle2D impactArea){
        ArrayList<String> commandList = new ArrayList<String>();
        commandList.add("REMOVE_FLYING_PROJECTILE");

        // 1. Make Figures suffer and add necessary commands to list being sended
        //ToDo add some kind of this sphere, that hurts less centered on impactPoint
        for(int t = 0; t < teams.size(); t++){
            for(int f = 0; f < teams.get(t).getFigures().size(); f++){
                if(teams.get(t).getFigures().get(f).getHitRegion().intersects(impactArea)){
                    try {
                        teams.get(t).getFigures().get(f).sufferDamage(damage);
                    } catch (DeathException e) {
                        e.printStackTrace();
                        // TODO IMPORTANT
                    }
                    if(poisons){ commandList.add("CONDITION" + " " + "POISON" + " "  + t + " " + f + " " + teams.get(t).getFigures().get(f).getHealth()); }
                    if(ignites){ commandList.add("CONDITION" + " " + "FIRE" + " "  + t + " " + f + " " + teams.get(t).getFigures().get(f).getHealth()); }
                    if(blocks) { commandList.add("CONDITION" + " " + "STUCK" + " "  + t + " " + f + " " + teams.get(t).getFigures().get(f).getHealth()); }
                    commandList.add("SET_HP " + t + " " + f + " " + teams.get(t).getFigures().get(f).getHealth());
                }
            }
        }

        // 2. Destroy Terrain and add necessary commands to list being sent
        commandList.addAll(terrain.handleExplosion(new Point2D(impactArea.getMinX(),impactArea.getMinY()), explosionpower));

        // 3. Send Figures flying
        //ToDo commandList.addAll();

        return commandList;
    }

    //Getter and Setter
    public ImageView getCrosshair() { return crosshair; }
    public double getAngle() { return angle; }
    public int getMass() { return mass; }


    //----------------------------------Crosshair-Related Functions---------------------------------
    public double toRadian(double grad) { // This function transforms angles to rad which are needed for sin/cos etc.
        return grad * Math.PI / 180;
    }

    public void resetAngle() { angle = 0; }

    @Override
    public void angleUp(boolean faces_right){
        angle = Math.min(90,angle + 2);
        angleDraw(faces_right);
    }
    @Override
    public void angleDown(boolean faces_right){
        angle = Math.max(-90, angle - 2);
        angleDraw(faces_right);
    }
    @Override
    public void angleDraw(boolean faces_right){ // Actually only sets the Point and changes facing of weapon; drawn in MapWindow
        if(faces_right){
            crosshair.setTranslateX(getTranslateX() + Math.cos(toRadian(angle)) * RADIUS);
            setScaleX(1); //Reverse mirroring
            setRotate(-angle);
        }
        else{
            crosshair.setTranslateX(getTranslateX() - Math.cos(toRadian(angle))* RADIUS);
            setScaleX(-1); //Mirror Weapon, so its facing left
            setRotate(angle);
        }
        crosshair.setTranslateY(getTranslateY() - Math.sin(toRadian(angle))* RADIUS);
    }
}
