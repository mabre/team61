package de.hhu.propra.team61;

import javafx.scene.paint.Color;

/**
 * This class provides static convenience methods which can be applied to JavaFX objects.
 * Created by markus on 26.05.14.
 * TODO rename class / update description, does more than just JavaFX
 */
public class JavaFxUtils {

    public static String toHex(Color color) {
        return String.format("#%02X%02X%02X", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    /**
     * @param str the string
     * @param after everything after this substring will be returned
     * @return the part of str after the first occurrence of after
     */
    public static String extractPart(String str, String after) {
        return str.substring(str.indexOf(after) + after.length());
    }

}
