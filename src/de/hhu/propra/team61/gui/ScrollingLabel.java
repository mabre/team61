package de.hhu.propra.team61.gui;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for a label which removes its lines after a certain timeout.
 * When adding a line using {@link #addLine(String, boolean)}, a new line with the given text is appended to the label.
 * The text is automatically removed after a certain timeout which depends on the length of the text. If the text is set
 * to low priority, the line is alternatively removed when another line is added. New lines are displayed under previous
 * lines; when a line is removed, the other lines are moved up.
 * <p>
 * Example:
 * <ol>
 * <li>addLine("Lorem", false); // text is "Lorem"
 * <li>addLine("Ipsum", true); // text is "Lorem\nIpsum"
 * <li>addLine("Dolor", false); // text is "Lorem\nDolor"
 * <li>// wait a tick
 * <li>// text is "Dolor"
 * <li>// wait even longer
 * <li>// text is ""
 * </ol>
 */
public class ScrollingLabel extends StackPane {

    /** regardless of the length, a line is visible at least this time (in seconds) */
    private static int MIN_TIMEOUT = 2;
    /** the time a line is shown depends on the letters in the line */
    private static int LETTERS_PER_SECOND = 20;

    /** lines currently shown in the label */
    ArrayList<LabelLine> lines = new ArrayList<>();

    /**
     * A label which calls {@link #remove(de.hhu.propra.team61.gui.ScrollingLabel.LabelLine)} after a certain timeout.
     */
    private class LabelLine extends Label {
        /** low priority lines are overwritten right away */
        boolean lowPriority;
        /** on timeout, this line is removed */
        Timer removeTimer = new Timer();

        /**
         * Creates a new line.
         * @param text the text to be shown
         * @param lowPriority whether the line is overwritten right away when a new line is added
         */
        LabelLine(String text, boolean lowPriority) {
            this.lowPriority = lowPriority;
            setText(text);

            final int timeout = Math.max(MIN_TIMEOUT, text.length()/LETTERS_PER_SECOND) * 1000;
            final LabelLine me = this;
            removeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    remove(me);
                }
            }, timeout);
        }

        /**
         * Checks if the line has low priority.
         * @return true if the line has low priority
         */
        public boolean isLowPriority() {
            return lowPriority;
        }

        /**
         * Stops the timer which removes this label.
         * Must be called when the label is removed before the timeout.
         */
        public void stopRemoveTimer() {
            removeTimer.cancel();
        }
    }

    /**
     * Adds another line to this label.
     * If the last line is a low priority line, this line is removed.
     * @param text the text to be shown
     * @param lowPriority whether the line is overwritten right away when a new line is added
     */
    public void addLine(String text, boolean lowPriority) {
        removeLowPriorityLine();
        LabelLine newLine = new LabelLine(text, lowPriority);
        lines.add(newLine);
        getChildren().add(newLine);

        if(lines.size() > 1) { // new label is last element
            LabelLine lastLine = lines.get(lines.size() - 2);
            newLine.setTranslateY(lastLine.getTranslateY() + lastLine.getHeight());
        }
    }

    /**
     * Removes the last line if it has low priority.
     */
    private void removeLowPriorityLine() {
        if(lines.size() > 0) {
            LabelLine lastLine = lines.get(lines.size() - 1);
            if (lastLine.isLowPriority()) {
                lastLine.stopRemoveTimer();
                getChildren().remove(lastLine);
                lines.remove(lastLine);
            }
        }
    }

    /**
     * Removes the given line and updates the positions of the other labels.
     * @param line the line to be removed.
     */
    private void remove(LabelLine line) {
        Platform.runLater(() -> {
            int lineIndex = lines.indexOf(line);
            getChildren().remove(line);
            lines.remove(lineIndex);
            for(int i=lineIndex; i < lines.size(); i++) {
                lines.get(i).setTranslateY(lines.get(i).getTranslateY() - line.getHeight());
            }
        });
    }

}
