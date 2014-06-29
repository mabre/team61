package de.hhu.propra.team61.animation;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * A class for creating sprite animations.
 * <p>
 * If there is an image animation.png, which contains 5 frames (in one row, equal width), the animation should last 1s,
 * and should be played twice, call {@code new SpriteAnimation(new ImageView("animation.png"), 1000, 5, 2)}.
 * <p>
 * inspired by <a href="http://blog.netopyr.com/2012/03/09/creating-a-sprite-animation-with-javafx/">http://blog.netopyr.com</a>
 */
public class SpriteAnimation extends Transition {

    /** image view holding the image with the frames of the animation */
    private final ImageView imageView;
    /** number of frames */
    private final int frames;
    /** width of each frame */
    private final int width;
    /** height of each frame */
    private final int height;

    /** the index of the frame which was most recently played */
    private int lastIndex;

    /**
     * Creates a new sprite animation.
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

    /**
     * Calculates and displays the frame to be displayed at the given point of time.
     * @param frac current position with the animation, between 0 and 1.
     */
    @Override
    protected void interpolate(double frac) {
        final int index = Math.min((int) Math.floor(frac * frames), frames - 1);
        if (index != lastIndex) {
            final int x = (index % frames) * width;
            imageView.setViewport(new Rectangle2D(x, 0, width, height));
            lastIndex = index;
        }
    }
}
