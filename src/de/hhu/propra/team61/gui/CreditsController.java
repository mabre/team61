package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.Afrobob;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

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
    @FXML private Label version = new Label();
    @FXML private GridPane creditsGrid;

    private String keysEntered = "";

    /**
     * Initialize the sceneController
     * @param sceneController passed controller
     */
    public void initialize(SceneController sceneController) {
        this.sceneController = sceneController;
        imageView.setImage(image);
        version.setText(Afrobob.VERSION_CODENAME + " Version: " + Afrobob.VERSION_NUMBER);

        creditsGrid.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            System.out.println("key pressed: " + keyEvent.getCode() + " " + keysEntered);
            switch (keyEvent.getCode()) {
                case SHIFT:
                case A:
                case B:
                case F:
                case O:
                case R:
                    keysEntered += keyEvent.getCode();
                    if (!"SHIFTAFROBOB".startsWith(keysEntered)) {
                        keysEntered = "";
                    } else if (keysEntered.equals("SHIFTAFROBOB")) {
                        System.out.println("Evolutionary.");
                    }
                    break;
                default:
                    keysEntered = "";
            }
        });
    }

    /**
     * Switches back to menu.
     */
    @FXML
    public void handleClose() {
        sceneController.switchToMenu();
    }
}
