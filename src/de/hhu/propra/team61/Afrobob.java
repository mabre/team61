package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.TerrainLoader;
import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

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

        Button mstartl = new Button("Start local game");  //menue-start-local, menue-start-network, menue-options, menue-exit
        grid.setHalignment(mstartl, HPos.CENTER);  //centers the buttons, not needed for mstartn as the biggest button
        grid.add(mstartl, 0, 1);
        Button mstartn = new Button("Start network game");
        grid.add(mstartn, 0, 2);
        Button moptions = new Button("Options");
        grid.setHalignment(moptions, HPos.CENTER);
        grid.add(moptions, 0, 3);
        Button mexit = new Button("Exit");
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
        mstartl.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                MapWindow mapwindow = new MapWindow(TerrainLoader.getAvailableTerrains().get(0)); // TODO map is hardcoded
            }
        });
        moptions.setOnAction(new EventHandler<ActionEvent>() {  //Click on button 'moptions' opens new window for options
            @Override
            public void handle(ActionEvent e) {
                OptionsWindow optionwindow = new OptionsWindow();
                optionwindow.do_options();
            }
        });

        Scene scene = new Scene(grid, 1000, 600);
        mainwindow.setScene(scene);
        mainwindow.show();
    }
}
