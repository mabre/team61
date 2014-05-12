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
    private String ImgSrc ="file:resources/";

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

        //Preload Images
        Image spawn           = new Image(ImgSrc+"spawn.png");          // TODO just temporary shown
        Image plain_ground    = new Image(ImgSrc+"plain_ground.png");
        Image slant_ground_ri = new Image(ImgSrc+"slant_ground_ri.png");
        Image slant_ground_le = new Image(ImgSrc+"slant_ground_le.png");
        Image wall_le         = new Image(ImgSrc+"wall_le.png");
        Image stones          = new Image(ImgSrc+"stones.png");
        Image earth           = new Image(ImgSrc+"earth.png");
        Image water           = new Image(ImgSrc+"water.png");
        Image sky             = new Image(ImgSrc+"sky.png");

        //Draw Images on their positions
        for(int i=0; i < terrain.size(); i++){
            for(int j=0; j < terrain.get(i).size(); j++){
                ImageView content = new ImageView();
                char terraintype = terrain.get(i).get(j);
                switch(terraintype) {
                    case 'P': content.setImage(spawn);
                        break;
                    case '_': content.setImage(plain_ground);
                        break;
                    case '/': content.setImage(slant_ground_ri);
                        break;
                    case '\\': content.setImage(slant_ground_le);
                        break;
                    case '|': content.setImage(wall_le);
                        break;
                    case 'S': content.setImage(stones);
                        break;
                    case 'E': content.setImage(earth);
                        break;
                    case 'W': content.setImage(water);
                        break;
                    default : content.setImage(sky);
                }

                grid.add(content,j,i);
                //grid.setConstraints(content,j,i);
            }
        }
        root.getChildren().add(grid);
    }


    @Override
    public void start(Stage ostage) { }
}
