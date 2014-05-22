package de.hhu.propra.team61.Objects;

/**
 * Created by markus on 20.05.14.
 */
public class CollisionWithTerrainException extends CollisionException {
    // TODO collision partner TerrainType?
    public CollisionWithTerrainException() {
        super("Terrain");
    }

}
