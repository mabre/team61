package de.hhu.propra.team61;

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.collections.*;
import java.awt.Color;

/*
 * Created by dinii on 15.04.14.
 * ProPra Team 61:
 * Markus Brenneis (Mat.Nr.) Marbre
 * Jan Ecknigk 2202505 (GitName)
 * Jessica Petrasch 2166230 Git: Jessypet
 * Kevin Gnyp (Mat.Nr/GitName)
 * Simon Franz 2204765 Git: DiniiAntares
 * Project: Worms clone
 *
 */

public class Afrobob extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start (Stage mainwindow) {
        mainwindow.setTitle("Unicorns and Penguins! <3");
        mainwindow.setWidth(1000);
        mainwindow.setHeight(600);
        mainwindow.setResizable(false);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Button mstartl = new Button("Lokales Spiel starten");  //menue-start-local, menue-start-network, menue-options, menue-exit
        grid.setHalignment(mstartl, HPos.CENTER);              //centers the buttons, not needed for mstartn as the biggest button
        grid.add(mstartl, 0, 1);
        Button mstartn = new Button("Netzwerkspiel starten");
        grid.add(mstartn, 0, 2);
        Button moptions = new Button("Optionen");
        grid.setHalignment(moptions, HPos.CENTER);
        grid.add(moptions, 0, 3);
        Button mexit = new Button("Beenden");
        grid.setHalignment(mexit, HPos.CENTER);
        grid.add(mexit, 0, 4);

        mstartl.setOnAction(new EventHandler<ActionEvent>() {  //Click on button starts game.. well not yet
            @Override
            public void handle(ActionEvent e) {

            }
        });

        mexit.setOnAction(new EventHandler<ActionEvent>() {  //Click on Button 'mexit' closes window
            @Override
            public void handle(ActionEvent e) {
                mainwindow.close();
            }
        });

        moptions.setOnAction(new EventHandler<ActionEvent>() {  //Click on button 'moptions' opens new window for options
            @Override
            public void handle(ActionEvent e) {
                Stage options = new Stage();
                options.setTitle("Options");
                options.setWidth(800);
                options.setHeight(500);
                options.setResizable(false);
                GridPane ogrid = new GridPane();
                ogrid.setAlignment(Pos.CENTER);
                ogrid.setHgap(10);
                ogrid.setVgap(10);
                ogrid.setPadding(new Insets(25, 25, 25, 25));

                Text optionst = new Text("Optionen");
                optionst.setFont(Font.font ("Verdana", 20));
                ogrid.add(optionst, 0, 0, 3, 1);

                Text soundt = new Text("Musik: ");
                ogrid.add(soundt, 0, 2);
                ToggleGroup sound = new ToggleGroup();
                RadioButton sound1 = new RadioButton("An");
                sound1.setToggleGroup(sound);
                sound1.setSelected(true);
                RadioButton sound2 = new RadioButton("Aus");
                sound2.setToggleGroup(sound);
                ogrid.add(sound1, 1, 2);
                ogrid.add(sound2, 2, 2);

                Text volt = new Text("Lautstärke: ");
                ogrid.add(volt, 0, 3);
                Slider volume = new Slider(0, 100, 50);
                volume.setShowTickMarks(true);
                volume.setShowTickLabels(true);
                ogrid.add(volume, 1, 3, 2, 1);

                Text resot = new Text("Auflösung: ");
                ogrid.add(resot, 0, 4);
                CheckBox fullscreen = new CheckBox("Vollbild");
                ogrid.add(fullscreen, 1, 4);
                ChoiceBox<String> resolution = new ChoiceBox<>(FXCollections.observableArrayList("800x600", "1024x768", "1280x720", "1366x768", "1440x900", "1920x1080"));
                resolution.getSelectionModel().selectFirst();
                ogrid.add(resolution, 2, 4);

                Text gammat = new Text("Gamma: ");
                ogrid.add(gammat, 0, 5);
                Slider gamma = new Slider(0, 100, 50);
                gamma.setShowTickMarks(true);
                gamma.setShowTickLabels(true);
                ogrid.add(gamma, 1, 5, 2, 1);

                Button oexit = new Button("Schließen");                    //options-exit, click closes options-window
                ogrid.add(oexit, 1, 8);
                oexit.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        options.close();
                    }
                });

                Scene oscene = new Scene(ogrid, 800, 500);
                options.setScene(oscene);
                options.show();
            }
        });

        Scene scene = new Scene(grid, 1000, 600);
        mainwindow.setScene(scene);
        mainwindow.show();
    }
}