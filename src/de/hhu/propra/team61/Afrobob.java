package de.hhu.propra.team61;

import de.hhu.propra.team61.gui.BigStage;
import de.hhu.propra.team61.gui.MenueController;
import de.hhu.propra.team61.gui.SceneController;
import de.hhu.propra.team61.io.json.JSONArray;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main class for starting the game, creating the stage and the menu.
 * <p>
 * Created by dinii on 15.04.14.<br/>
 * ProPra Team 61:<br/>
 * Markus Brenneis 2194529 Git: mabre<br/>
 * Jan Ecknigk 2202505 Git: Jan-Ecknigk<br/>
 * Jessica Petrasch 2166230 Git: Jessypet<br/>
 * Kevin Gnyp 2166803 Git: Kegny<br/>
 * Simon Franz 2204765 Git: DiniiAntares<br/>
 * Project: Worms clone
 */
public class Afrobob extends Application {

    public static String VERSION_NUMBER = "0.1b140709";
    public static String VERSION_CODENAME = "Afrobob";

    /** public, because sceneController needs to change the stage's scene */
    public BigStage mainwindow = new BigStage(SceneController.GAME_TITLE);
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
        System.out.println("--- "+VERSION_CODENAME+" "+VERSION_NUMBER+" ---");
        sceneController.setStage(mainwindow);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/menue.fxml"));
        root = loader.load();
        menueController = loader.getController();
        Scene scene = new Scene(root, 1000, 600);
        mainwindow.setScene(scene);
        mainwindow.getIcons().add(new Image("file:resources/figures/Penguin.png"));
        scene.getStylesheets().add("file:resources/layout/css/menue.css");
        sceneController.setMenuScene(scene);
        menueController.setMainWindow(mainwindow, sceneController);
        mainwindow.show();
    }
}
