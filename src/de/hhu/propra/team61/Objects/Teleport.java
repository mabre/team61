package de.hhu.propra.team61.Objects;

import javafx.scene.image.ImageView;

/**
 * Created by kevin on 05.06.14.
 * First try, rather a little test
 */
public class Teleport extends Item {
    private final String description = "Safe, as long as you can aim";

    public ImageView getCrosshair(){ return new ImageView(); }
    @Override
    public void angleUp(boolean a){};
    @Override
    public void angleDown(boolean a){};
    @Override
    public void angleDraw(boolean a){};

    @Override
    public Projectile shoot(){

        return null;
    }
}
