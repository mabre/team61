package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.TerrainManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
//import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.awt.event.KeyEvent;

import java.awt.event.KeyListener;
import java.security.Key;
import java.util.ArrayList;
/**
 * Created by kegny on 08.05.14.
 * Edited by DiniiAntares on 15.05.14
 * This class is supposed to draw the Array given by "TerrainManager" rendering the Map visible.
 */
public class MapWindow extends Application implements KeyListener {
    private ArrayList<ArrayList<Character>> terrain;
    private Scene drawing;
    private Stage primaryStage;
    private StackPane root;
    private GridPane grid;
    private int activeTeam = 0;
    private int turnCount = 0;
    private int levelCounter = 0;

    ArrayList<Integer> team; //TODO use Team class


    public MapWindow(String map) {
        team = new ArrayList<Integer>();
        team.add(0);
        team.add(42);


        terrain = TerrainManager.load(map);

        primaryStage = new Stage();

        root = new StackPane();

        draw();

        drawing = new Scene(root, 800, 600);

        primaryStage.setTitle("The Playground");
        primaryStage.setScene(drawing);
        primaryStage.show();
    }

    /**
     * creates Image objects for the fields
     */
    private void draw() {
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
                    case 'W': loadImg += "water.png";  //Add Ice and Grass "I" and "G"
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


  /*  //Level change trough "Press #" and end turn with "Press Space"
    @Override
    public void keyPressed (KeyEvent keyPress){
        if (keyPress.getCode() == KeyCode.L) {
            cheatMode();
        } else if (keyPress.getCode() == KeyCode.SPACE) {
            endTurn();
        }


    }*/

    public void cheatMode (){
        levelCounter++;
        levelCounter = levelCounter % TerrainManager.getAvailableTerrains().size();
        TerrainManager.getAvailableTerrains().get(levelCounter);

    }

    public void endTurn (){
        //activeTeam = (activeTeam == team.length()-1 ? 0 : activeTeam+1);
        turnCount++;
        turnCount = turnCount % team.size(); //TODO graphical output for turnCount

    }

    @Override
    public void start(Stage ostage) {

    }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {

    }


    @Override
    public void keyPressed(KeyEvent keyPress) {
        System.out.println("here");
        switch (keyPress.getKeyCode()) {
            case KeyEvent.VK_L: cheatMode(); break;
            case KeyEvent.VK_SPACE: endTurn(); break;
        }
    }

    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {

    }
}
