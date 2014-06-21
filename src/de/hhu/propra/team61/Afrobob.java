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

    BigStage mainwindow = new BigStage("Unicorns and Penguins <3");
    SceneController sceneController = new SceneController();
    MenueController menueController;
    Pane root;

    public static void main(String[] args) {
        launch(args);
    }

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
