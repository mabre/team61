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
     * @return the part of str after the first occurrence of after; if after does not appear in str, str is returned
     */
    public static String extractPart(String str, String after) {
        if(!str.contains(after)) return str;
        return str.substring(str.indexOf(after) + after.length());
    }

    /**
     * @param str the string
     * @return the string with the last character removed
     */
    public static String removeLastChar(String str) {
        if (str != null && str.length() > 0) {
            str = str.substring(0, str.length()-1);
        }
        return str;
    }

    /**
     * @param arr an array of strings
     * @param from array elements before from are not part of the result
     * @return a string containing the strings in the array with a space between them (reverses String.split(" "))
     */
    public static String arrayToString(String[] arr, int from) {
        StringBuilder builder = new StringBuilder();
        for(int i=from; i<arr.length; i++) builder.append(arr[i]+" ");
        builder.setLength(builder.length()-1); // remove final " "
        return builder.toString();
    }

}
