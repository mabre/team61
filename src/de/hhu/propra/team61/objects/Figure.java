package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.MapWindow;
import de.hhu.propra.team61.animation.SpriteAnimation;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.objects.itemtypes.Rifle;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static de.hhu.propra.team61.JavaFxUtils.toHex;

// Created by kevin on 14.05.14.
/**
 * An instance of this class represents one Figure, which most likely will be part of a
 * {@link de.hhu.propra.team61.Team}. Each Figure is in possession of a name, health armor, mass and
 * status-conditions like paralysis, being stuck, poisoning and being degitates.
 */
public class Figure extends StackPane {
    public static final int NORMED_OBJECT_SIZE = 16;

    /** note that the actual speed is lower because gravity is subtracted before the figure actually jumps */
    private static final int MASS = 1000;
    /** multiplier for horizontal movement */
    public static final int WALK_SPEED = 5;
    /** multiplier for upward movement */
    public static final int JUMP_SPEED = 18 + (int)(MapWindow.GRAVITY.getY() * MASS);
    /** figures cannot accelerate further in negative (to top) y-direction when y-speed is greater than this during the current jump */
    public static final int MAX_Y_SPEED = (int)(1.2*JUMP_SPEED);
    /** velocity endurable before damage by fall is caused */
    private static final int FALL_DAMAGE_THRESHOLD = JUMP_SPEED;
    /** Position for dead figures, to make them disappear from screen */
    private static final Point2D GRAVEYARD = new Point2D(-1000,-1000);
    /** space between name and frame */
    private static final int ACTIVE_INDICATOR_PADDING = 2;
    /** initial health shield value for figures on rampage; the shield left is added as life bonus (in addition to {@link #RAMPAGE_HP_BONUS_FACTOR} */
    private final static int RAMPAGE_SHIELD = 30;
    /** figures on rampage are less effected by shock waves, implemented by increased mass
     * NB: If the factor is too high, figures suffer fall damage by standing around, TODO maybe change the way wo achieve less shock wave effect */
    private static final int RAMPAGE_MASS_FACTOR = 2;
    /** life steal factor; life steal also considers the {@link #RAMPAGE_SHIELD} left */
    private static final double RAMPAGE_HP_BONUS_FACTOR = .4;
    /** padding in px for rampage overlay */
    private static final int RAMPAGE_IMAGE_PADDING = 2;

    /** Name of the figure */
    private String name;
    /** "Unicorn" or "Penguin" */
    private String figureType;
    /** Health of a figure, every not positive value means death */
    private int health;
    /** Health shield absorbing hp damage */
    private int healthShield = 0;
    /** Color used for hpLabel etc. */
    private Color color;
    /** frame which is drawn around the active figure's name */
    Rectangle activeIndicator = new Rectangle();

    /** position of the figure, has to be synced with translateX/Y (introduced to prevent timing issues on JavaFX thread) */
    private Point2D position = new Point2D(0,0);
    private Point2D velocity = new Point2D(0,0);
    /** the maximal speed (absolute value) in y direction since last call of resetVelocity, used to limit jump speed */
    private double maxYSpeed = 0;

    /** Booleans indicating status conditions */
    private boolean isParalyzed;
    private boolean isPoisoned;
    private boolean isStuck;

    /** Used to highlight figure steered */
    private boolean isActive;
    /** boolean indicating faced direction, used for velocity vector of projectiles and optical reasons */
    private boolean facingRight = true; //Needed for Weapon class, MapWindow, etc.
    /** Item hold */
    private Item selectedItem;

//    private Rectangle hitRegionDebug; // TODO doesn't work
    /** Hitbox of this figure */
    private Rectangle2D hitRegion;
    /** Image of the figure */
    private ImageView figureImage;
    /** Display of figure's name over its head */
    private Text nameTag;
    /** Display of figure's health over its head */
    private Text hpLabel;
    /** Overlay displaying conditions */
    ImageView condition = null;
    /** image overlay which is drawn above figures which are on a rampage */
    private final ImageView rampageOverlay = new ImageView(new Image("file:resources/figures/rampage.png", NORMED_OBJECT_SIZE+2*RAMPAGE_IMAGE_PADDING, NORMED_OBJECT_SIZE+2*RAMPAGE_IMAGE_PADDING, true, true));

    /** properties for digitation, which might modify other values */
    private boolean digitated = false;
    private boolean isOnRampage = false;
    private double massFactor = 1;
    private int jumpDuringFallThreshold = 0;
    private double armor = 0;
    private int causedHpDamage = 0;
    private int recentlySufferedDamage = 0;

    // In and Out

    /**
     * Constructor setting up all variables and images
     *
     * @param name the Figure's name
     * @param figureType Unicorn or penguin?
     * @param hp health
     * @param armor armor rating (damage modifier)
     * @param isParalyzed status condition
     * @param isPoisoned status condition
     * @param isStuck status condition
     */
    public Figure(String name, String figureType, int hp, double armor, boolean isParalyzed, boolean isPoisoned, boolean isStuck){
        this.name   = name;
        this.figureType = figureType;
        this.health = hp;
        this.armor  = armor;

        this.isParalyzed = isParalyzed;
        this.isPoisoned = isPoisoned;
        this.isStuck    = isStuck;

        figureImage = new ImageView();

        initialize();
    }

    /**
     * Constructor setting up all variables and images.
     * @param input JSONObject containing all variables specified in the other constructor <b>and position.x and .y</b>
     */
    public Figure(JSONObject input){
        figureImage = new ImageView();
        position = new Point2D(input.getDouble("position.x"), input.getDouble("position.y"));
        figureImage.setTranslateX(position.getX());
        figureImage.setTranslateY(position.getY());

        this.name = input.getString("name"); // TODO json.get wrapper which checks if property exists an can return default value
        this.figureType = input.getString("figureType");
        this.health = input.getInt("health");
        this.healthShield = input.getInt("healthShield");
        this.armor  = input.getDouble("armor");

        this.jumpDuringFallThreshold = input.getInt("jumpDuringFallThreshold");
        this.massFactor = input.getDouble("massFactor");
        this.causedHpDamage = input.getInt("causedHpDamage");

        this.digitated = input.getBoolean("digitated");
        this.isOnRampage = input.getBoolean("isOnRampage");
        this.isParalyzed = input.getBoolean("isParalyzed");
        this.isPoisoned = input.getBoolean("isPoisoned");
        this.isStuck    = input.getBoolean("isStuck");

        System.out.println("FIGURE created from json");

        initialize();
    }

    /**
     * Extension of constructor which takes over all tasks which are identical for all constructors in this class
     * e.g. setting up hitboxes and images or the labels.
     */
    private void initialize() {
        selectedItem = null;

        setAlignment(Pos.TOP_LEFT);

        hitRegion = new Rectangle2D(position.getX(), position.getY(),16,16);

        System.out.println("Chosen figure: " + figureType);
        Image image = new Image("file:resources/figures/"+ (digitated?"digi":"") + figureType +".png", NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true);
        figureImage.setImage(image);
        getChildren().add(figureImage);

        getChildren().add(rampageOverlay);
        rampageOverlay.setVisible(false);

        nameTag = new Text(name);
        hpLabel = new Text();
        getChildren().add(nameTag);
        getChildren().add(hpLabel);

        activeIndicator.setArcWidth(5);
        activeIndicator.setArcHeight(5);
        activeIndicator.setStroke(Color.rgb(255, 0, 0, .4));
        activeIndicator.setStrokeWidth(1);
        activeIndicator.setVisible(false);
        this.getChildren().add(activeIndicator);

        Platform.runLater(() -> rampageOverlay.setVisible(isOnRampage));

        updateHpLabelText();
    }

    /** Creates a JSONObject containing all variables' values of this instance (e.g. for saving) */
    public JSONObject toJson(){
        JSONObject output = new JSONObject();
        output.put("name", name);
        output.put("figureType", figureType);
        output.put("health", health);
        output.put("healthShield", healthShield);
        output.put("armor", armor);
        output.put("digitated", digitated);
        output.put("isOnRampage", isOnRampage);
        output.put("jumpDuringFallThreshold", jumpDuringFallThreshold);
        output.put("massFactor", massFactor);
        output.put("causedHpDamage", causedHpDamage);
        output.put("position.x", position.getX()); // TODO saveJson as array
        output.put("position.y", position.getY());

        output.put("isParalyzed", isParalyzed);
        output.put("isPoisoned", isPoisoned);
        output.put("isStuck", isStuck);
        return output;
    }

    public void setColor(Color color) {
        this.color = color;
        String style = "-fx-fill: " + toHex(color) + ";";
        nameTag.setStyle(style);
        hpLabel.setStyle(style);
        DropShadow ds = new DropShadow();
        ds.setRadius(2);
        ds.setColor(Color.rgb(250, 250, 250, .5));
        nameTag.setEffect(ds);
        hpLabel.setEffect(ds);
//        if(color.getBrightness() < .5) {
            activeIndicator.setFill(Color.rgb(250, 250, 250, .05));
//        } else { // does not look as good as expected
//            activeIndicator.setFill(Color.rgb(0, 0, 0, .1));
//        }
    }

    public String getName(){return name;}
    public void setName(String name){this.name = name;}

    public double getArmor() {return armor;}
//    public void setArmor(double armor) {this.armor = armor;}

    public boolean getIsParalyzed() {return isParalyzed;}
    public void setIsParalyzed(boolean isParalyzed){
        this.isParalyzed = isParalyzed;
      /*  if (isParalyzed){
            condition = new ImageView("file:resources/figures/paralyzed.png");
            condition.setTranslateX(figureImage.getTranslateX());
            condition.setTranslateY(figureImage.getTranslateY());
            this.getChildren().add(condition);
        } else { if(condition != null){this.getChildren().remove(condition); condition = null;} }*/
    }

    public boolean getIsPoisoned() {return isPoisoned;}
    public void setIsPoisoned(boolean isPoisoned){
        this.isPoisoned = isPoisoned;
        if (isPoisoned){
            condition = new ImageView(new Image("file:resources/figures/poisoned.png",NORMED_OBJECT_SIZE,NORMED_OBJECT_SIZE,true,true));
            condition.setTranslateX(figureImage.getTranslateX());
            condition.setTranslateY(figureImage.getTranslateY());
            this.getChildren().add(condition);
        } else { if(condition != null){this.getChildren().remove(condition); condition = null;} }
    }

    public boolean getIsStuck() {return isStuck;}
    public void setIsStuck(boolean isStuck){this.isStuck = isStuck;}

    public void setActive(boolean isActive) {
        this.isActive = isActive;
        activeIndicator.setVisible(isActive);
    }

    /**
     * Moves the figure to the given position, updates the hit region, position of the figure image and the hp label.
     * The position is rounded to one decimal place. JavaFX parts are run with runLater, hence it is save to call this
     * function from non-fx threads.
     * @param newPosition the new position of the figure in px
     */
    public void setPosition(Point2D newPosition) {
        if(health <= 0 && !newPosition.equals(GRAVEYARD)) { // workaround for a timing issue // TODO we have to do sth when a figure dies when it is its turn
            System.err.println("WARNING: figure " + name + " is dead, hence cannot be moved");
            position = GRAVEYARD;
        }

        position = new Point2D(Math.round(newPosition.getX()*10)/10.0, Math.round(newPosition.getY()*10)/10.0); // round position to one decimal place, see above (10.0 must be double)
        hitRegion = new Rectangle2D(position.getX(),position.getY(),hitRegion.getWidth(),hitRegion.getHeight());
//        getChildren().removeAll(hitRegionDebug);
//        hitRegionDebug = new Rectangle(position.getX(),position.getY(),hitRegion.getWidth(),hitRegion.getHeight());
//        hitRegionDebug.setTranslateX(position.getX());
//        hitRegionDebug.setTranslateY(position.getY());
//        hitRegionDebug.setFill(Color.web("rgba(255,0,0,.3)"));
        //getChildren().add(hitRegionDebug); // TODO brakes scroll pane?!
        updatePositionsOfChildren();
    }

    /**
     * Updates position of image, labels, and weapon.
     * It is safe to call this function from non-JavaFX threads.
     */
    private void updatePositionsOfChildren() {
        int offset = NORMED_OBJECT_SIZE / 2;

        Platform.runLater(() -> {
            figureImage.setTranslateX(Math.round(position.getX())); // round this position to prevent ugly subpixel rendering
            figureImage.setTranslateY(Math.round(position.getY()));
            rampageOverlay.setTranslateX(Math.round(position.getX()) - RAMPAGE_IMAGE_PADDING);
            rampageOverlay.setTranslateY(Math.round(position.getY()) - RAMPAGE_IMAGE_PADDING);
            nameTag.setTranslateX(figureImage.getTranslateX() + offset - nameTag.getLayoutBounds().getWidth() / 2);
            nameTag.setTranslateY(figureImage.getTranslateY() - NORMED_OBJECT_SIZE * 2);
            hpLabel.setTranslateX(figureImage.getTranslateX() + offset - hpLabel.getLayoutBounds().getWidth() / 2);
            hpLabel.setTranslateY(figureImage.getTranslateY() - NORMED_OBJECT_SIZE);
            activeIndicator.setTranslateX(nameTag.getTranslateX() - ACTIVE_INDICATOR_PADDING);
            activeIndicator.setTranslateY(nameTag.getTranslateY() - ACTIVE_INDICATOR_PADDING);
            activeIndicator.setWidth(nameTag.getLayoutBounds().getWidth() + ACTIVE_INDICATOR_PADDING*2);
            activeIndicator.setHeight(nameTag.getLayoutBounds().getHeight() + ACTIVE_INDICATOR_PADDING); // -2 since we otherwise overlap with the hpLabel
            if(condition != null){ // TODO do it like rampageOverlay?
                condition.setTranslateX(figureImage.getTranslateX());
                condition.setTranslateY(figureImage.getTranslateY());
            }
            setSelectedItem(getSelectedItem()); // Update position of weapon
        });
    }

    public Point2D getPosition() {
        return new Point2D(position.getX(), position.getY());
    }

    public Item getSelectedItem(){
        return selectedItem;
    }

    public void setSelectedItem(Item select){
        double previousAngle = -1337;
        if(selectedItem != null && selectedItem instanceof Weapon && // TODO @Kegny is there a better way than using instanceof? Keep angle in Figure?
           !(selectedItem instanceof Rifle) && !(select instanceof Rifle) // do not give aim aid by aiming with rifle before shooting with other weapon
           && select instanceof Weapon) { // do not reset angle when choosing item (so returning to the previous weapon preserves angle)
            previousAngle = ((Weapon)selectedItem).getAngle();
            ((Weapon)selectedItem).resetAngle();
        }
        selectedItem = select;
        if(selectedItem != null) {
            selectedItem.setPosition(new Point2D(position.getX(), position.getY()));
            if(selectedItem instanceof Weapon && previousAngle != -1337) ((Weapon)selectedItem).setAngle(previousAngle);
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

    /**
     * Lets the figure suffer damage.
     * If {@link #armor} is greater than 0, the damage is reduced. {@link #healthShield} can reduce the effect on the
     * actual hp value. Calling this function with {@code damage=0} simply redraws the hp label.
     * Examples:
     * <ul>
     *     <li>hp is at 80, {@code armor=0}, {@code sufferDamage(40)} is called. hp is now at 40.</li>
     *     <li>hp is at 80, {@code armor=.5}, {@code sufferDamage(40)} is called. hp is now at 60.</li>
     *     <li>hp is at 40, {@code sufferDamage(40)} is called. hp is now at 0, the figure becomes invisible, and a {@link DeathException} is thrown.</li>
     * </ul>
     * @param damage the damage (armor will reduce the damage)
     * @param countAsRecentlySufferedDamage if true, the effective damage (ie. after considering armor) is added to {@link #recentlySufferedDamage}
     * @throws DeathException thrown when the figure is dead after suffering the given damage
     */
    public void sufferDamage(final int damage, final boolean countAsRecentlySufferedDamage) throws DeathException {
        int damageAfterArmor = (int)Math.ceil(damage - (armor*damage)); // round up so that digitated figures still suffer 1 hp damage on water
        if(healthShield >= damageAfterArmor) {
            healthShield -= damageAfterArmor;
        } else {
            damageAfterArmor -= healthShield;
            healthShield = 0;
            health -= damageAfterArmor;
        }

        if(countAsRecentlySufferedDamage) {
            addRecentlySufferedDamage(damageAfterArmor);
        }

        if(health <= 0) {
            health = 0;
            Image image = new Image("file:resources/spawnpoint.png", NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true);
            Platform.runLater(() -> figureImage.setImage(image));
            setPosition(GRAVEYARD);
            throw new DeathException(this);
        }
        updateHpLabelText();
        System.out.println(name + " got damage " + damage + " (* 1-"+ armor +"), health at " + health + "~" + healthShield);
    }

    /**
     * Updates the hp label with the current health, shield, and digitation status, and updates the position of the labels.
     * It is save to call this method from non-fx threads.
     */
    private void updateHpLabelText() {
        Platform.runLater(() -> {
            String hpLabelText = health + "";
            if(healthShield > 0) hpLabelText += "~" + healthShield;
            if(digitated) hpLabelText += "+";
            hpLabel.setText(hpLabelText);
            updatePositionsOfChildren();
        });
    }

    /**
     * Lets the figure suffer damage which is not counted as {@link #recentlySufferedDamage}.
     * Same as calling {@code sufferDamage(damage, false)}
     * @param damage the damage to be suffered (armor will reduce the damage)
     * @throws DeathException
     * @see #sufferDamage(int, boolean)
     */
    public void sufferDamage(final int damage) throws DeathException {
        sufferDamage(damage, true);
    }

    public void setHealth(int hp) {
        this.health = hp;
        try {
            sufferDamage(0); // redraws the label and validates new hp
        } catch(DeathException e) {
            // cannot happen here
        }
        System.out.println(name + "'s health at " +hp);
    }

    public int getHealth() {
        return health;
    }

    public int getShield() {
        return healthShield;
    }

    public void setShield(int shield) {
        this.healthShield = shield;
    }

    /**
     * Tells whether this figure is on rampage
     * @return true if figure is on rampage
     */
    public boolean isOnRampage() {
        return isOnRampage;
    }

    /**
     * Ends the rampage of the figure.
     * The figure gets a hp bonus depending on the given hp value (typically caused damage in round) and the {@link #healthShield}.
     * @param hp a hp value used to calculate life steal (typically caused damage in round)
     */
    public void endRampage(int hp) {
        isOnRampage = false;
        // causing 50 damage with 30 shield -> 42 bonus (coincidence, really)
        // causing 50 damage with 0 shield -> 20 bonus
        // causing 0 damage with 30 shield -> 15 bonus
        int hpBonus = (int)(hp * (RAMPAGE_HP_BONUS_FACTOR + healthShield/2.0/100.0) + healthShield/2.0);
        System.out.println("rampage hp bonus: " + hpBonus);
        health += hpBonus;
        isOnRampage = false;
        healthShield = 0;
        Platform.runLater(() -> rampageOverlay.setVisible(false));
        updateHpLabelText();
    }

    /**
     * Starts rampage of figure.
     * Unless it is the figures turn, it will result is a higher mass (see {@link #getMass()} (making effect of weapon
     * shock waves smaller), adds a life shield, and draws a red circle around the figure.
     */
    public void startRampage() {
        if(isOnRampage) {
            System.err.println(name + " already is on rampage");
            return;
        }
        isOnRampage = true;
        healthShield = RAMPAGE_SHIELD;
        Platform.runLater(() -> rampageOverlay.setVisible(true));
        updateHpLabelText();
    }

    public Rectangle2D getHitRegion() {
        return hitRegion;
    }

    public Projectile shoot() throws NoMunitionException {
        return selectedItem.use(this);
    }

    public Point2D getVelocity() {
        return velocity;
    }

    /**
     * resets the velocity vector to 0 and - depending on the speed - the figure suffers fall damage
     */
    public void resetVelocity() throws DeathException {
        int fallDamage = (int)(velocity.magnitude() - FALL_DAMAGE_THRESHOLD);

        if(!velocity.equals(MapWindow.GRAVITY.multiply(getMass()))) { // do not print when "default gravity" is applied when figures are standing on ground
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

    public void jump() {
        if(velocity.getY() > 0) {
            System.out.println("falling, jumping not possible");
            if(velocity.getY() > jumpDuringFallThreshold) {
                return;
            } else {
                System.out.println("oops, digitated, you can!");
                maxYSpeed = velocity.getY(); // allow further acceleration
            }
        }
        if(maxYSpeed < MAX_Y_SPEED) { // figure cannot accelerate further when y-speed was greater than MAX_Y_SPEED during the current jump
            addVelocity(new Point2D(0, -JUMP_SPEED));
            if(maxYSpeed > MAX_Y_SPEED) { // if figure is now faster than MAX_Y_SPEED, slow it down
                velocity = new Point2D(velocity.getX(), -MAX_Y_SPEED);
                System.out.println("jump speed limit reached (cut): " + maxYSpeed);
            }
        } else {
            System.out.println("jump speed limit reached (ignored): " + maxYSpeed);
        }
    }

    public int getMass() {
        return (int)(MASS * massFactor * ((isOnRampage && !isActive) ? RAMPAGE_MASS_FACTOR : 1));
    }

    public void digitate() { // similarity to digivolution is purely coincidental
        switch(figureType) {
            case "Penguin":
                massFactor = .5;
                jumpDuringFallThreshold = MAX_Y_SPEED;
                armor = .2;
                break;
            case "Unicorn":
                massFactor = .9;
                armor = .5;
        }
        digitated = true;

        Platform.runLater(() -> {
            updateHpLabelText();
            ImageView digitationAnimationImage = new ImageView("file:resources/animations/digitation.png");
            digitationAnimationImage.setTranslateX(figureImage.getTranslateX() - NORMED_OBJECT_SIZE/2);
            digitationAnimationImage.setTranslateY(figureImage.getTranslateY() - NORMED_OBJECT_SIZE/2);
            SpriteAnimation digitationAnimation = new SpriteAnimation(digitationAnimationImage, 1000, 6, 1);
            digitationAnimation.setOnFinished((e) -> getChildren().removeAll(digitationAnimationImage));
            digitationAnimation.play();
            getChildren().add(digitationAnimationImage);
            figureImage.setImage(new Image("file:resources/figures/digi"+ figureType +".png", NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true));
            updatePositionsOfChildren();
        });
    }

    public void degitate() {
        massFactor = 1;
        jumpDuringFallThreshold = 0;
        armor = 0;
        digitated = false;
        Platform.runLater(() -> {
            hpLabel.setText(health + "");
            figureImage.setImage(new Image("file:resources/figures/"+ figureType +".png", NORMED_OBJECT_SIZE, NORMED_OBJECT_SIZE, true, true));
        });
    }

    public boolean isDigitated() {
        return digitated;
    }

    public int getCausedHpDamage() {
        return causedHpDamage;
    }

    public void addCausedHpDamage(int damage) {
        causedHpDamage += damage;
        System.out.println(name + " caused " + damage + " hp damage this round");
    }

    private void addRecentlySufferedDamage(int damage) {
        recentlySufferedDamage += damage;
    }

    public int popRecentlySufferedDamage() {
        int rsd = recentlySufferedDamage;
        recentlySufferedDamage = 0;
        return rsd;
    }

    public int getDamageByLiquid() {
        return (digitated ? 1 : 2);
    }
}
