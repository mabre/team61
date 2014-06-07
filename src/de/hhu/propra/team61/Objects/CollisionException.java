package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;

/**
 * Created by markus on 20.05.14.
 */
public class CollisionException extends Exception {

    private String collisionPartnerClass;

    private Point2D lastGoodPosition;
    private Point2D collidingPosition;

    public CollisionException(Point2D collidingPosition, Point2D lastGoodPosition) {
      //  this.collisionPartnerClass = collisionPartnerClass;
        this.lastGoodPosition = lastGoodPosition;
        this.collidingPosition = collidingPosition;
    }

    public String getCollisionPartnerClass() {
        return collisionPartnerClass;
    }

    public Point2D getLastGoodPosition() {
        return lastGoodPosition;
    }

    public Point2D getCollidingPosition() {
        return collidingPosition;
    }
}
