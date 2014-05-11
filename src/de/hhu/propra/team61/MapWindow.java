package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.TerrainLoader;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
                char terraintype = terrain.get(i).get(j);
                String loadImg ="file:resources/";
                switch(terraintype) {
                    case 'P': loadImg += "sky.png"; //ToDo sky.png is not the picture we are looking for
                              break;
                    case '_': loadImg += "plain_ground.png";
                              break;
                    case '/': loadImg += "slant_ground_ri.png";
                              break;
                    case '\\':loadImg += "slant_ground_le.png";
                              break;
                    case '|': loadImg += "Wall_le.png";
                              break;
                    case 'S': loadImg += "stones.png";
                              break;
                    case 'E': loadImg += "earth.png";
                              break;
                    case 'W': loadImg += "water.png";
                              break;
                    default : loadImg += "sky.png";
                }
                Image image = new Image(loadImg);
                ImageView content = new ImageView();
                content.setImage(image);

                grid.add(content,j,i);
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
    } // Just for testing of the class
}
