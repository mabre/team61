package de.hhu.propra.team61.gui;

import javafx.fxml.FXML;

/**
 * Controls GUI for credits.
 *
 * Created by Jessypet on 02.07.14.
 */
public class CreditsController {

    /** used to switch back to menu */
    private SceneController sceneController;

    /**
     * Initialize the sceneController
     * @param sceneController passed controller
     */
    public void initialize(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    /**
     * Switches back to menu.
     */
    @FXML
    public void handleClose() {
        sceneController.switchToMenu();
    }
}
