package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;

/**
 * Created by markus on 20.05.14.
 */
public class CollisionWithFigureException extends CollisionException {

    Figure collisionPartner;

    public CollisionWithFigureException(Point2D lastGoodPosition, Figure collisionPartner) {
        super("Figure", lastGoodPosition);
        this.collisionPartner = collisionPartner;
    }

    public Figure getCollisionPartner() {
        return collisionPartner;
    }

}
