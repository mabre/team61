package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.CreditsController;
import de.hhu.propra.team61.gui.SceneController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Shows credits.
 *
 * Created by Jessypet on 02.07.14.
 */
public class CreditsWindow {

    /** Controls GUI-elements from FXML-file */
    private CreditsController creditsController = new CreditsController();
    /** contains background and scrollPane */
    private StackPane root = new StackPane();
    /** scrollPane for creditsScene */
    private ScrollPane scrollPane = new ScrollPane();
    /** grid for GUI */
    private Pane grid = new Pane();

    /**
     * Loads GUI for credits.
     * @param sceneController used to switch back to menu
     */
    public CreditsWindow(SceneController sceneController) {
        try {
            FXMLLoader creditsLoader = new FXMLLoader(getClass().getResource("gui/credits.fxml"));
            grid = creditsLoader.load();
            scrollPane.setContent(grid);
            scrollPane.getStyleClass().add("scrollPane");
            creditsController = creditsLoader.getController();
            root.getChildren().add(scrollPane);
            Scene creditsScene = new Scene(root, 1000, 600);
            creditsScene.getStylesheets().add("file:resources/layout/css/credits.css");
            sceneController.switchScene(creditsScene, "Credits");
            creditsController.initialize(sceneController);
        } catch (IOException e) {
            System.out.println("FXML-file not found.");
        }

    }

}
