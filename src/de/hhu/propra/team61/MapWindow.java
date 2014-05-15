package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.TerrainManager;
import de.hhu.propra.team61.Network.Client;
import de.hhu.propra.team61.Network.Server;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
/**
 * Created by kegny on 08.05.14.
 * This class is supposed to draw the Array given by "TerrainManager" rendering the Map visible.
 */
public class MapWindow extends Application{
    private ArrayList<ArrayList<Character>> terrain;
    private Scene drawing;
    private Stage primaryStage;
    private StackPane root;
    private GridPane grid;

    private Server server;
    private Client client;
    private Thread serverThread;
    private Thread clientThread;

    public MapWindow(String map) {
        terrain = TerrainManager.load(map);

        primaryStage = new Stage();
        primaryStage.setOnCloseRequest(we -> {
            clientThread.interrupt();
            serverThread.interrupt();
            System.out.println("MapWindow threads interrupted");
            client.stop();
            server.stop();
            System.out.println("MapWindow client/server stopped");
        });

        root = new StackPane();

        drawFirstTime();

        drawing = new Scene(root, 800, 600);

        primaryStage.setTitle("The Playground");
        primaryStage.setScene(drawing);
        primaryStage.show();

        serverThread = new Thread(server = new Server());
        serverThread.start();
        clientThread = new Thread(client = new Client()); // TODO race condition
        clientThread.start();
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
                    case 'P': loadImg += "spawn.png"; // TODO just temporary shown
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
