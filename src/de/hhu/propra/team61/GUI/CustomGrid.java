package de.hhu.propra.team61.GUI;

import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

/**
 * Created by Jessypet on 27.05.14.
 */
public class CustomGrid extends GridPane {

    public CustomGrid() {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(25, 25, 25, 25));
    }
}
