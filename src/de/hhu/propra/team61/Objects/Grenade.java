package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;

/**
 * Created by kevgny on 22.05.14.
 */
public class Grenade extends Weapon { //ToDo rename to a more fitting one
    public Grenade(Point2D pos, boolean facing_right,int damage){
        super(pos,facing_right,damage);
    }
    public Projectile shoot(){// Overwrittes the function in Weapon
        //TODO do more?
        Projectile shot = new Projectile();
        return shot;
    }

}
