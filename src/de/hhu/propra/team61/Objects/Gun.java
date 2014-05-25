package de.hhu.propra.team61.Objects;

import de.hhu.propra.team61.IO.JSON.JSONObject;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;

/**
 * Created by kevgny on 21.05.14.
 */
public class Gun extends Weapon { //ToDo rename to a more fitting one
    public Gun(String path, int damage, int munition){
        super(damage, munition);

        Image image = new Image(path, 8, 8, true, true); //ToDo Replace with an actual Weapon
        setImage(image);
    }

    public Gun(JSONObject json) {
        super(json);
    }

    @Override
    public Projectile shoot() throws NoMunitionException {
        if(munition > 0) {
            Projectile shot = new Projectile(new Image("file:resources/weapons/temp0.png"), new Point2D(getX(), getY()), new Point2D(getCrosshair().getX(), getCrosshair().getY()), 20, getDamage());
            munition--;
            System.out.println("munition left: " + munition);
            return shot;
        } else {
            throw new NoMunitionException();
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = super.toJson();
        json.put("type", "Gun");

        return json;
    }

}
