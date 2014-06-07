package de.hhu.propra.team61.Objects;

import de.hhu.propra.team61.IO.JSON.JSONObject;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import static de.hhu.propra.team61.JavaFxUtils.toHex;

/**
 * Created by kevin on 14.05.14.
 */

public class Figure extends StackPane {
    private boolean facingRight = true; //Needed for Weapon class, MapWindow, etc.

    private String name;
    private int health;
    private int armor;

    private boolean isBurning;
    private boolean isPoisoned;
    private boolean isStuck;

    private boolean isActive;

    private Item selectedItem;

    private Rectangle2D hitRegion;
    private ImageView figureImage;
    private Label nameTag;
    private Label hpLabel;

    // In and Out
    public Figure(String name, int hp, int armor, boolean isBurning, boolean isPoisoned, boolean isStuck){
        this.name   = name;
        this.health = hp;
        this.armor  = armor;

        this.isBurning  = isBurning;
        this.isPoisoned = isPoisoned;
        this.isStuck    = isStuck;

        figureImage = new ImageView();

        initialize();
    }

    public Figure(JSONObject input){
        figureImage = new ImageView();
        figureImage.setTranslateX(input.getDouble("position.x"));
        figureImage.setTranslateY(input.getDouble("position.y"));

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
        figureImage = new ImageView();
        figureImage.setTranslateX(input.getDouble("position.x"));
        figureImage.setTranslateY(input.getDouble("position.y"));

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
        selectedItem = null;

        setAlignment(Pos.TOP_LEFT);

        hitRegion = new Rectangle2D(figureImage.getTranslateX(), figureImage.getTranslateY(),16,16);

        Image image = new Image("file:resources/figures/pin.png", 16, 16, true, true);
        figureImage.setImage(image);
        getChildren().add(figureImage);


        nameTag = new Label(name);
        getChildren().add(nameTag);

        hpLabel = new Label(health+"");
        setPosition(getPosition()); // updates label position
        getChildren().add(hpLabel);
    }

    public JSONObject toJson(){
        JSONObject output = new JSONObject();
        output.put("name", name);
        output.put("health", health);
        output.put("armor", armor);
        output.put("position.x", figureImage.getTranslateX()); // TODO save as array
        output.put("position.y", figureImage.getTranslateY());

        output.put("isBurning", isBurning);
        output.put("isPoisoned", isPoisoned);
        output.put("isStuck", isStuck);
        return output;
    }

    public void setColor(Color color) {
        nameTag.setTextFill(color);
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

    public void setActive(boolean isActive) {
        this.isActive = isActive;
        if(isActive) {
            // TODO move to css
            nameTag.setStyle("-fx-border-color: rgba(255,0,0,.2); -fx-border-style: solid; -fx-border-width: 1px; -fx-border-radius: 5px;");
        } else {
            nameTag.setStyle("");
        }
    }

    // TODO rethink parameter, /8 is bad!
    public void setPosition(Point2D position) {
        figureImage.setTranslateX(8 * position.getX());
        figureImage.setTranslateY(8 * position.getY());
        hitRegion = new Rectangle2D(figureImage.getTranslateX(), figureImage.getTranslateY(),hitRegion.getWidth(),hitRegion.getHeight());
        nameTag.setTranslateX(figureImage.getTranslateX()-nameTag.getWidth()/2);
        nameTag.setTranslateY(figureImage.getTranslateY()-25);
        hpLabel.setTranslateX(figureImage.getTranslateX()-hpLabel.getWidth()/2);
        hpLabel.setTranslateY(figureImage.getTranslateY()-15);
    }

    public Point2D getPosition() {
        return new Point2D(figureImage.getTranslateX()/8, figureImage.getTranslateY()/8);
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
            select.setPosition(new Point2D(figureImage.getTranslateX(), figureImage.getTranslateY()));
            selectedItem.angleDraw(facingRight);
        }
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
        figureImage.setScaleX(facingRight ? 1 : -1); // mirror image when not facing right
    }
    public boolean getFacingRight(){
        return facingRight;
    }

    public void sufferDamage(int damage) {
        health -= damage;
        if(health <= 0) {
            health = 0;
            Image image = new Image("file:resources/spawn.png", 8, 8, true, true); // TODO
            figureImage.setImage(image);
            setPosition(new Point2D(-1000, -1000));
        }
        hpLabel.setText(health+"");
        System.out.println(name + " got damage " + damage + ", health at " + health);
    }

    public void setHealth(int hp) {
        this.health = hp;
        sufferDamage(0); // redraws the label and validated new hp
    }

    public int getHealth() {
        return health;
    }

    public Rectangle2D getHitRegion() {
        return hitRegion;
    }

    public Projectile shoot(int power) throws NoMunitionException {
       // selectedItem.setPosition(new Point2D(figureImage.getTranslateX(), figureImage.getTranslateY())); // What is this for?
        return selectedItem.shoot(power);
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
