package de.hhu.propra.team61.Objects;

import javafx.geometry.Point2D;

/**
 * Created by markus on 20.05.14.
 */
public class CollisionWithTerrainException extends CollisionException {
    // TODO collision partner TerrainType?
    public CollisionWithTerrainException(Point2D getLastGoodPosition) {
        super("Terrain", getLastGoodPosition);
    }

}
