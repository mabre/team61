package de.hhu.propra.team61.objects;

/**
 * Created by markus on 12.06.14.
 */
public class DeathException extends Exception {

    Figure figure;

    public DeathException(Figure figure) {
        this.figure = figure;
    }

    public Figure getFigure() {
        return figure;
    }

}
