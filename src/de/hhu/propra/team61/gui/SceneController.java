package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.gui.BigStage;
import javafx.scene.Scene;

/**
 * Makes switching between scenes in one stage possible.
 *
 * Contains all scenes and the stage = mainwindow and resets the scene of the mainwindow. The scenes containing main
 * menue and the game itself need to be saved in sceneController, because they can also be set from outside the class they
 * are created in.
 * All methods in this class are public as sceneController is called by other classes.
 *
 * Created by Jessypet on 01.06.14.
 */

public class SceneController {

    /** The main window */
    private BigStage mainwindow;
    /** contains main menue */
    private Scene menueScene;
    /** contains game */
    private Scene gameScene;

    /**
     * Sets the main stage.
     * @param mainwindow main stage
     */
    public void setStage(BigStage mainwindow) {
        this.mainwindow = mainwindow;
    }

    /**
     * Returns the main stage.
     * @return the main stage
     */
    public BigStage getStage() {
        return mainwindow;
    }

    /**
     * Sets the scene of the main menue.
     * @param menueScene contains the main menue
     */
    public void setMenueScene(Scene menueScene) {
        this.menueScene = menueScene;
    }
    /**
     * Sets the scene of the game ({@link de.hhu.propra.team61.MapWindow}).
     * @param gameScene contains the game
     */
    public void setGameScene(Scene gameScene) {
        this.gameScene = gameScene;
    }

    /**
     * Switches to the given scene.
     * @param scene the scene that is supposed to be shown
     * @param title the new title of the window
     */
    public void switchScene(Scene scene, String title) {
        mainwindow.setScene(scene);
        mainwindow.setTitle(title);
    }

    /**
     * Switches to the menue.
     */
    public void switchToMenue() {
        mainwindow.setScene(menueScene);
        mainwindow.setTitle("Unicorns and penguins <3");
    }

    /**
     * Switches to the game.
     */
    public void switchToMapwindow() {
        mainwindow.setScene(gameScene);
        mainwindow.setTitle("The Playground");
    }
}
