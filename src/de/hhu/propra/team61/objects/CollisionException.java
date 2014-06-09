package de.hhu.propra.team61.objects;

import javafx.geometry.Point2D;

/**
 * Created by markus on 20.05.14.
 */
public class CollisionException extends Exception {

    private String collisionPartnerClass;

    private Point2D lastGoodPosition;

    public CollisionException(String collisionPartnerClass, Point2D lastGoodPosition) {
        this.collisionPartnerClass = collisionPartnerClass;
        this.lastGoodPosition = lastGoodPosition;
    }

    public String getCollisionPartnerClass() {
        return collisionPartnerClass;
    }

    public Point2D getLastGoodPosition() {
        return lastGoodPosition;
    }
}
