package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.TerrainLoader;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
    private static String map = "";

    public static void draw(String map_src){
        map = map_src;
        launch();
    }

    public void start(Stage primaryStage) {
        // ToDo should become a member variable initialized in constructor -mabre
        // Problem: launch() is static; Can't be called from an Instance; Made a workaround function "draw()" -kegny
        ArrayList<ArrayList<Character>> terrain = TerrainLoader.load(map);

        //Draw Terrain
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true); //Gridlines on/off
        grid.setAlignment(Pos.CENTER);

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

    public static void main(String[] args){
        draw("Board");
    }
}
