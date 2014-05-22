package de.hhu.propra.team61.Objects;

/**
 * Created by markus on 20.05.14.
 */
public class CollisionWithFigureException extends CollisionException {

    Figure collisionPartner;

    public CollisionWithFigureException(Figure collisionPartner) {
        super("Figure");
        this.collisionPartner = collisionPartner;
    }

    public Figure getCollisionPartner() {
        return collisionPartner;
    }

}
