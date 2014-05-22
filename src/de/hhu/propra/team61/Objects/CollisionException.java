package de.hhu.propra.team61.Objects;

/**
 * Created by markus on 20.05.14.
 */
public class CollisionException {

    private String collisionPartnerClass;

    public CollisionException(String collisionPartnerClass) {
        this.collisionPartnerClass = collisionPartnerClass;
    }

    public String getCollisionPartnerClass() {
        return collisionPartnerClass;
    }
}
