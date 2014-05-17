package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.TerrainManager;
import de.hhu.propra.team61.Objects.Terrain;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Created by kegny on 08.05.14.
 * This class is supposed to draw the Array given by "TerrainManager" rendering the Map visible.
 */
public class MapWindow extends Application{
    private Scene drawing;
    private Stage primaryStage;
    private StackPane root;
    private Terrain terrain;

    public MapWindow(String map) {
        primaryStage = new Stage();

        root = new StackPane();
        loadTerrain(map);
        root.getChildren().add(terrain);

        drawing = new Scene(root, 800, 600);

        primaryStage.setTitle("The Playground");
        primaryStage.setScene(drawing);
        primaryStage.show();
    }

    /**
     * creates Image objects for the fields
     */
    private void loadTerrain(String name) {
        terrain = new Terrain(TerrainManager.load(name));
    }

    @Override
    public void start(Stage ostage) { }
}
