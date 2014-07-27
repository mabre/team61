package de.hhu.propra.team61.gui;

import javafx.scene.Scene;

/**
 * Makes switching between scenes in one stage possible.
 *
 * Contains all scenes and the stage = mainwindow and resets the scene of the mainwindow. The scenes containing main
 * menu and the game itself need to be saved in sceneController, because they can also be set from outside the class they
 * are created in.
 * All methods in this class are public as sceneController is called by other classes.
 *
 * Created by Jessypet on 01.06.14.
 */

public class SceneController {

    /** The main window */
    private static BigStage mainwindow;
    /** contains main menu */
    private static Scene menuScene;
    /** contains game */
    private static Scene gameScene;
    /** title of the game to be shown at the top at all times */
    public static final String GAME_TITLE = "Charly in Madagascar";

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
    public static BigStage getStage() { // TODO @Jessypet Can we make everything static, so that we do not need to pass the references to sceneController around?
        return mainwindow;
    }

    /**
     * Sets the scene of the main menu.
     * @param menuScene contains the main menu
     */
    public void setMenuScene(Scene menuScene) {
        this.menuScene = menuScene;
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
        mainwindow.setTitle(GAME_TITLE + " - " + title);
    }

    /**
     * Switches to the menu.
     */
    public void switchToMenu() {
        mainwindow.setScene(menuScene);
        mainwindow.setTitle(GAME_TITLE);
    }

    /**
     * Switches to the game.
     */
    public void switchToMapwindow() {
        mainwindow.setScene(gameScene);
        mainwindow.setTitle(GAME_TITLE + " - The Playground");
    }
}
