package de.hhu.propra.team61.gui;

import javafx.stage.Stage;

/**
 * Stage with some presets.
 *
 * Created by Jessypet on 27.05.14.
 */
public class BigStage extends Stage {

    /**
     * Sets the stage's title and size
     * @param title the wanted title of the window
     */
    public BigStage(String title) {
        this.setTitle(title);
        this.setWidth(1000);
        this.setHeight(600);
        this.setResizable(false);
    }
}
