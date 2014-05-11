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
    private ArrayList<ArrayList<Character>> terrain;
    private Scene drawing;
    private Stage primaryStage;
    private StackPane root;
    private GridPane grid;

    public MapWindow(String map) {
        terrain = TerrainLoader.load(map);

        primaryStage = new Stage();

        root = new StackPane();

        drawFirstTime();

        drawing = new Scene(root, 800, 600);

        primaryStage.setTitle("The Playground");
        primaryStage.setScene(drawing);
        primaryStage.show();
    }

    /**
     * creates Image objects for the fields
     */
    private void drawFirstTime() {
        //Draw Terrain
        grid = new GridPane();
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
                    case '|': loadImg += "wall_le.png";
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
                //grid.setConstraints(content,j,i);
            }
        }

        root.getChildren().add(grid);
    }

    public void draw() {

        System.out.println("bla");
    }

    @Override
    public void start(Stage ostage) { }
}
