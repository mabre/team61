package de.hhu.propra.team61.gui;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controls GUI for credits.
 *
 * Created by Jessypet on 02.07.14.
 */
public class CreditsController {

    /** used to switch back to menu */
    private SceneController sceneController;
    /** contains image */
    @FXML private ImageView imageView = new ImageView();
    /** contains heading */
    private Image image = new Image("file:resources/layout/cover.png");

    /**
     * Initialize the sceneController
     * @param sceneController passed controller
     */
    public void initialize(SceneController sceneController) {
        this.sceneController = sceneController;
        imageView.setImage(image);
    }

    /**
     * Switches back to menu.
     */
    @FXML
    public void handleClose() {
        sceneController.switchToMenu();
    }
}
