package de.hhu.propra.team61.animation;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Created by markus on 18.06.14.
 * inspired by http://blog.netopyr.com/2012/03/09/creating-a-sprite-animation-with-javafx/
 */

public class SpriteAnimation extends Transition {

    private final ImageView imageView;
    private final int frames;
    private final int width;
    private final int height;

    private int lastIndex;

    /**
     * Creates a new sprite animation
     * @param imageView image view containing the sprite image to be animated; frames in rows
     * @param duration duration of the whole animation in ms
     * @param frames number of frames in the image
     * @param repeat number of repetitions
     */
    public SpriteAnimation(ImageView imageView, int duration, int frames, int repeat) {
        this.imageView = imageView;
        this.frames    = frames;
        this.width     = (int)(imageView.getImage().getWidth()) / frames;
        this.height    = (int)(imageView.getImage().getHeight());
        imageView.setViewport(new Rectangle2D(0, 0, width, height));
        setCycleDuration(new Duration(duration));
        setInterpolator(Interpolator.LINEAR);
        setCycleCount(repeat);
    }

    @Override
    protected void interpolate(double k) {
        final int index = Math.min((int) Math.floor(k * frames), frames - 1);
        if (index != lastIndex) {
            final int x = (index % frames) * width;
            imageView.setViewport(new Rectangle2D(x, 0, width, height));
            lastIndex = index;
        }
    }
}
