package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.MapWindow;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * Created by markus on 19.06.14.
 */
public class WindIndicator extends StackPane {

    private final static int WIDTH = 150;
    private final static int HEIGHT = 14;
    private Rectangle backgroundRectangle = new Rectangle(0, 0, WIDTH, HEIGHT);
    private Label forceLabel = new Label();
    private Label directionLabel = new Label();

    public WindIndicator() {
        getChildren().add(backgroundRectangle);
        getChildren().add(directionLabel);
        getChildren().add(forceLabel);
        setMaxWidth(WIDTH);
    }

    public void setWindForce(double value) {
        Platform.runLater(() -> {
            final double roundedAbsoluteValue = Math.round(Math.abs(value) * 10) / 10.0;

            if (roundedAbsoluteValue < .5) {
                backgroundRectangle.setFill(Color.web("rgba(25,255,25,.5)")); // light green
                MapWindow.playSoundeffects("lightBreeze.wav");
            } else if (roundedAbsoluteValue < 1.5) {
                backgroundRectangle.setFill(Color.web("rgba(15,155,15,.5)")); // dark green
                MapWindow.playSoundeffects("breeze.wav");
            } else if (roundedAbsoluteValue < 2.5) {
                backgroundRectangle.setFill(Color.web("rgba(255,255,15,.5)")); // yellow
                MapWindow.playSoundeffects("wind.wav");
            } else if (roundedAbsoluteValue < 3.5) {
                backgroundRectangle.setFill(Color.web("rgba(255,175,15,.5)")); // orange
                MapWindow.playSoundeffects("strongWind.wav");
            } else if (roundedAbsoluteValue < 4.5) {
                backgroundRectangle.setFill(Color.web("rgba(255,15,15,.5)")); // red
                MapWindow.playSoundeffects("dangerousWind.wav");
            } else {
                backgroundRectangle.setFill(Color.web("rgba(255,15,255,.5)")); // violet
                MapWindow.playSoundeffects("hurricane.wav");
            }

            String direction = "";
            if(roundedAbsoluteValue != 0) {
                for (int i = 0; i <= roundedAbsoluteValue + .5; i++) {
                    direction += (value > 0 ? ">" : "<");
                }
            }
            directionLabel.setText(direction);
            setAlignment(directionLabel, value > 0 ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            forceLabel.setText(roundedAbsoluteValue+"");
        });
    }

}
