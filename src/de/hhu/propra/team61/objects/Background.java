package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.animation.SpriteAnimation;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

/**
 * Created by jan on 22.06.14.
 */
public class Background extends Application implements Runnable {
    private static final ImageView SPRITE = new ImageView("file:resources/animation/leaf.png");
    private static final int HEIGHT = 40;
    private static final int WIDTH = 40;
    private static final int FRAMES = 8;
    private static final int FRAME_HEIGHT = 19;
    private Terrain stage;

    /**
     *
     * @param ostage
     */
    public Background(Terrain ostage){
        this.stage = ostage;
    }
    @Override
    public void run(){
        int col = 6;
        int rows = 6;
        for (int i = HEIGHT; i>=0; i--){
            if (i==0)i = HEIGHT;
            int index = i%FRAMES;
            int x = (index*WIDTH);
            SPRITE.setViewport(new Rectangle2D(x,0,WIDTH,FRAME_HEIGHT));
            for (int j = col; j >= 0; j--){
                for (int k = rows; k>=0;k--){
                    stage.add(SPRITE, j, k);
                }
            }

        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }
}
