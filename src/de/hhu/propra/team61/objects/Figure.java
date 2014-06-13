package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.MapWindow;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Created by kevgny on 14.05.14.
 */

public class Figure extends StackPane {

    public static final int JUMP_SPEED = 28;
    public static final int WALK_SPEED = 5;
    private static final int MASS = 1000;
    /** figures do not get fall damage when collision speed is smaller than jump speed + a little more
     * (needed because our calculations are not exact, e.g. we do not have the zero-velocity-point, thus start-speed != final-speed when jumping */
    private static final int FALL_DAMAGE_THRESHOLD = (int)(JUMP_SPEED);
    private static final Point2D GRAVEYARD = new Point2D(-1000,-1000);

    private boolean facing_right = true; //Needed for Weapon class, MapWindow, etc.

    private String name;
    private int health;
    private int armor;

    /** position of the figure, has to be synced with translateX/Y (introduced to prevent timing issues on JavaFX thread) */
    private Point2D position = new Point2D(0,0);
    private Point2D velocity = new Point2D(0,0);

    private boolean isBurning;
    private boolean isPoisoned;
    private boolean isStuck;

    private Item selectedItem;

    private Rectangle2D hitRegion;
    private Rectangle hitRegionDebug;
    private ImageView imageView;
    private Label hpLabel;

    // In and Out
    public Figure(String name, int hp, int armor, boolean isBurning, boolean isPoisoned, boolean isStuck){
        this.name   = name;
        this.health = hp;
        this.armor  = armor;

        this.isBurning  = isBurning;
        this.isPoisoned = isPoisoned;
        this.isStuck    = isStuck;

        imageView = new ImageView();

        initialize();
    }

    public Figure(JSONObject input){
        imageView = new ImageView();
        this.position = new Point2D(input.getDouble("position.x"), input.getDouble("position.y"));
        imageView.setTranslateX(position.getX());
        imageView.setTranslateY(position.getY());

        this.name = input.getString("name");
        this.health = input.getInt("health");
        this.armor  = input.getInt("armor");
        this.isBurning  = input.getBoolean("isBurning");
        this.isPoisoned = input.getBoolean("isPoisoned");
        this.isStuck    = input.getBoolean("isStuck");

        System.out.println("FIGURE created from json");
        printAllAttributes(this);

        initialize();
    }

    public Figure(String name, JSONObject input){ //Create Figures by giving a name and applying Options TODO: Minor Adjustments after implementation of Options
        imageView = new ImageView();
        this.position = new Point2D(input.getDouble("position.x"), input.getDouble("position.y"));
        imageView.setTranslateX(position.getX());
        imageView.setTranslateY(position.getY());

        this.name = name;
        this.health = input.getInt("health");
        this.armor  = input.getInt("armor");
        this.isBurning  = input.getBoolean("isBurning");
        this.isPoisoned = input.getBoolean("isPoisoned");
        this.isStuck    = input.getBoolean("isStuck");

        System.out.println("FIGURE created from OptionsJson");
        printAllAttributes(this);

        initialize();
    }

    private void initialize() {
        setAlignment(Pos.TOP_LEFT);

        hitRegion = new Rectangle2D(position.getX(), position.getY(),16,16);

        Image image = new Image("file:resources/figures/pin.png", 16, 16, true, true);
        imageView.setImage(image);
        getChildren().add(imageView);

        selectedItem = null;
        hpLabel = new Label(health+"");
        setPosition(getPosition()); // updates label position
        getChildren().add(hpLabel);
    }

    public JSONObject toJson(){
        JSONObject output = new JSONObject();
        output.put("name", name);
        output.put("health", health);
        output.put("armor", armor);
        output.put("position.x", position.getX()); // TODO save as array
        output.put("position.y", position.getY());

        output.put("isBurning", isBurning);
        output.put("isPoisoned", isPoisoned);
        output.put("isStuck",isStuck);
        return output;
    }

    public void setColor(Color color) {
        hpLabel.setTextFill(color);
    }

    public String getName(){return name;}
    public void setName(String name){this.name = name;}

    public int getArmor() {return armor;}
    public void setArmor(int armor) {this.armor = armor;}

    public boolean getIsBurning() {return isBurning;}
    public void setIsBurning(boolean isBurning){this.isBurning = isBurning;}

    public boolean getIsPoisoned() {return isPoisoned;}
    public void setIsPoisoned(boolean isPoisoned){this.isPoisoned = isPoisoned;}

    public boolean getIsStuck() {return isStuck;}
    public void setIsStuck(boolean isStuck){this.isStuck = isStuck;}

    /**
     * moves the figure to the given position, updated the hit region, position of the figure image and the hp label
     * JavaFX parts are run with runLater, hence it is save to call this function from non-fx threads.
     * @param newPosition the new position of the figure in blocks // TODO rethink parameter, /8 is bad! [is done on weaponsFF]
     */
    public void setPosition(Point2D newPosition) {
        if(health <= 0 && !newPosition.equals(GRAVEYARD)) { // workaround for a timing issue // TODO we have to do sth when a figure dies when it is its turn
            System.out.println("WARNING: figure " + name + " is dead, hence cannot be moved");
            position = GRAVEYARD;
        }

        position = new Point2D(8 * newPosition.getX(), 8 * newPosition.getY());
        hitRegion = new Rectangle2D(position.getX(),position.getY(),hitRegion.getWidth(),hitRegion.getHeight());
        getChildren().removeAll(hitRegionDebug);
        hitRegionDebug = new Rectangle(position.getX(),position.getY(),hitRegion.getWidth(),hitRegion.getHeight());
        hitRegionDebug.setTranslateX(position.getX());
        hitRegionDebug.setTranslateY(position.getY());
        hitRegionDebug.setFill(Color.web("rgba(255,0,0,.3)"));
        //getChildren().add(hitRegionDebug); // TODO brakes scroll pane?!
        Platform.runLater(() -> {
            imageView.setTranslateX(this.position.getX());
            imageView.setTranslateY(this.position.getY());
            hpLabel.setTranslateX(position.getX());
            hpLabel.setTranslateY(position.getY() - 15);
        });
    }

    public Point2D getPosition() {
        return new Point2D(position.getX()/8, position.getY()/8);
    }

    public Item getSelectedItem(){
        return selectedItem;
    }
    public void setSelectedItem(Weapon select){
        if(selectedItem != null) {
            selectedItem.hide();
        }
        selectedItem = select;
        if(selectedItem != null) {
            select.setPosition(new Point2D(position.getX(), position.getY()));
            selectedItem.angleDraw(facing_right);
        }
    }

    public void setFacing_right(boolean facing_right) {
        this.facing_right = facing_right;
    }
    public boolean getFacing_right(){return facing_right;}

    public void sufferDamage(int damage) throws DeathException {
        health -= damage;
        if(health <= 0) {
            health = 0;
            Image image = new Image("file:resources/spawn.png", 16, 16, true, true);
            Platform.runLater(() -> imageView.setImage(image));
            setPosition(GRAVEYARD);
            throw new DeathException(this);
        }
        Platform.runLater(() -> hpLabel.setText(health+""));
        System.out.println(name + " got damage " + damage + ", health at " + health);
    }

    public void setHealth(int hp) {
        this.health = hp;
        try {
            sufferDamage(0); // redraws the label and validated new hp
        } catch(DeathException e) {
            // cannot happen here
        }
    }

    public int getHealth() {
        return health;
    }

    public Rectangle2D getHitRegion() {
        return hitRegion;
    }

    public Projectile shoot() throws NoMunitionException {
       // selectedItem.setPosition(new Point2D(imageView.getTranslateX(), imageView.getTranslateY())); // What is this for?
        return selectedItem.shoot();
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

        if(fallDamage > 0) {
            sufferDamage(fallDamage);
        }
    }

    public void addVelocity(Point2D dV) { // TODO interface?
        velocity =  velocity.add(dV);
    }

    public int getMass() {
        return MASS;
    }

    //For testing purposes only
    private static void printAllAttributes(Figure testwurm){
        System.out.println("Health  : " + testwurm.getHealth());
        System.out.println("Armor   : " + testwurm.getArmor());
        System.out.println("Name    : " + testwurm.getName());
        System.out.println("Burning : " + testwurm.getIsBurning());
        System.out.println("Poisoned: " + testwurm.getIsPoisoned());
        System.out.println("Stuck   : " + testwurm.getIsStuck());

        System.out.println("Position: " + testwurm.getPosition());
        System.out.println();
    }
}
