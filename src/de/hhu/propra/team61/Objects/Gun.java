package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;

/**
 * Created by kevgny on 21.05.14.
 */
public class Gun extends Weapon { //ToDo rename to a more fitting one
    public Gun(Point2D pos, boolean facing_right){
        super(pos,facing_right);
 }
    public Projectile shoot(){// Overwrittes the function in Weapon
        //TODO do more?
        Projectile shot = new Projectile();
        return shot;
    }

}
