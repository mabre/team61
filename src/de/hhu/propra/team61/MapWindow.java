package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.TerrainLoader;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
/**
 * Created by kegny on 08.05.14.
 * This class is supposed to draw the Array given by "TerrainLoader" rendering the Map visible.
 */
public class MapWindow extends Application{
    public static void main(String[] args){
         launch(args);
    }

    public void start(Stage primaryStage) {
        // TODO should become a member variable initialized in constructor
        ArrayList<ArrayList<Character>> terrain = TerrainLoader.load("Board");

        //Draw Terrain
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true); //Gridlines on/off
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10, 10, 10, 10));

        for(int i=0; i < terrain.size(); i++){
            for(int j=0; j < terrain.get(i).size(); j++){
                Label test = new Label(terrain.get(i).get(j)+""); //+"" Makes the Char a String to avoid type mismatches
                grid.add(test,j,i);
            }
        }

        StackPane root = new StackPane();
        root.getChildren().add(grid);

        Scene zeichnung = new Scene(root, 800, 600);

        primaryStage.setTitle("The Playground");
        primaryStage.setScene(zeichnung);
        primaryStage.show();
    }
}
