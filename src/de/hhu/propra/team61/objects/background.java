package de.hhu.propra.team61.objects;

import de.hhu.propra.team61.animation.SpriteAnimation;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.util.ArrayList;

/**
 * Created by jan on 22.06.14.
 */
public class background extends GridPane implements Runnable {
    private static final ImageView SLICE = new ImageView("file:resources/animation/leaf.png");
    private static final int HEIGHT = 40;
    private static final int WIDTH = 40;
    private static final int FRAMES = 8;
    private static final int FRAME_HEIGHT = 19;

    @Override
    public void run(){
        for (int i = HEIGHT; i>0; i--){
            if (i==0)i = HEIGHT;
            int index = i%FRAMES;
            int x = (index*WIDTH);
            SLICE.setViewport(new Rectangle2D(x,0,WIDTH,FRAME_HEIGHT));

        }
    }
}
