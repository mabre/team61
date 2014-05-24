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
        Projectile shot = new Projectile(new Image("file:resources/weapons/temp1.png"),new Point2D(getTranslateX(),getTranslateY()),new Point2D(getCrosshair().getTranslateX(),getCrosshair().getTranslateY()),4,getDamage());
        return shot;
    }

}
