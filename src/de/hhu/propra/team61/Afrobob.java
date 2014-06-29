package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.BigStage;
import de.hhu.propra.team61.gui.MenueController;
import de.hhu.propra.team61.gui.SceneController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/*
 * Main class, an application creating the stage and the menue.
 *
 * Created by dinii on 15.04.14.
 * ProPra Team 61:
 * Markus Brenneis 2194529 Git: mabre
 * Jan Ecknigk 2202505 Git: Jan-Ecknigk
 * Jessica Petrasch 2166230 Git: Jessypet
 * Kevin Gnyp 2166803 Git: Kegny
 * Simon Franz 2204765 Git: DiniiAntares
 * Project: Worms clone
 *
 */

public class Afrobob extends Application {

    /** public, because sceneController needs to change the stage's scene */
    public BigStage mainwindow = new BigStage("Charlie in Madagascar");
    /** used to switch between scenes in one stage */
    private SceneController sceneController = new SceneController();
    /** controls the GUI-elements loaded from menue.fxml */
    private MenueController menueController;
    /** contains scene with GUI-elements */
    private Pane root;

    /**
     * launches the Application, calls {@link #start(Stage)}
     * @param args input from prompt
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Loads GUI and CSS for menue. Initializes sceneController by passing the stage mainwindow.
     * @param filler start needs a stage as parameter, but it is not needed as BigStage is already created
     * @throws IOException in case FXML-file does not exist
     */
    public void start (Stage filler) throws IOException {
        sceneController.setStage(mainwindow);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/menue.fxml"));
        root = loader.load();
        menueController = loader.getController();
        Scene scene = new Scene(root, 1000, 600);
        mainwindow.setScene(scene);
        scene.getStylesheets().add("file:resources/layout/css/menue.css");
        sceneController.setMenueScene(scene);
        menueController.setMainWindow(mainwindow, sceneController);
        mainwindow.show();
    }
}
