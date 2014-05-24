package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;

/**
 * Created by kevgny on 22.05.14.
 */
public class Grenade extends Weapon { //ToDo rename to a more fitting one
    public Grenade(Point2D pos, boolean facing_right,String path, int damage){
        super(pos,facing_right,path,damage);
    }
    public Projectile shoot(){// Overwrittes the function in Weapon
        //TODO do more?
        Projectile shot = new Projectile(new Image("file:resources/weapons/temp0.png"),new Point2D(getX(),getY()),new Point2D(getCrosshair().getX(),getCrosshair().getY()),20,getDamage());
        return shot;
    }

}
