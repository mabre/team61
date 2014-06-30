package de.hhu.propra.team61.gui;

import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

/**
 * Grid with some presets.
 *
 * Created by Jessypet on 27.05.14.
 */
public class CustomGrid extends GridPane {

    /**
     * Sets the grids' size-variables
     */
    public CustomGrid() {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(25, 25, 25, 25));
    }
}
