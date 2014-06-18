package de.hhu.propra.team61.io;

import de.hhu.propra.team61.io.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TerrainManager {

    private static final String LEVEL_DIR = "resources/levels/";
    private static final String LEVEL_FILE_EXTENSION = ".lvl";
    private static final String SAVE_LEVEL_FILE = "Afrobob.level.conf";

    /**
     * @return a list of available levels, w/o path, including file name extension
     */
    public static ArrayList<String> getAvailableTerrains() {
        File dir = new File(LEVEL_DIR);
        FilenameFilter filter = (f, s) -> s.toLowerCase().endsWith(LEVEL_FILE_EXTENSION);
        ArrayList<String> levels = new ArrayList<>(Arrays.asList(dir.list(filter)));
        System.out.println("Levels found: "+levels);
        return levels;
    }

    /**
     * @return the number of level files found
     */
    public static int getNumberOfAvailableTerrains() {
        return getAvailableTerrains().size();
    }

    /**
     * @param filename the file containing the board to be loaded
     * @return an ArrayList containing the board ([row][column])
     * How a valid board looks like is documented in BoardLegend
     */
    public static /*ArrayList<ArrayList<Character>>*/ JSONObject load(String filename) throws FileNotFoundException {
        //String dir = LEVEL_DIR;
        //if(filename.equals(SAVE_LEVEL_FILE)) dir = ""; //TODO Still necessary?
        return Json.getFromFile(LEVEL_DIR + filename);
    }

    /**
     * @param levelnumber the index of the level in the JSONObject  returned by {@link #getAvailableTerrains() getAvailableTerrains}
     * @return an JSONObject containing the board ([row][column])
     */
    public static JSONObject/*ArrayList<ArrayList<Character>>*/ load(int levelnumber) throws FileNotFoundException {
        return load(getAvailableTerrains().get(levelnumber));
    }

    /**
     * @param levelstring string representation of a level
     * @return 2-D JSONObject representing the given level
     */
    public static JSONObject /*ArrayList<ArrayList<Character>>*/ loadFromString(String levelstring) {
        /*ArrayList<ArrayList<Character>>*/JSONObject level = new /*ArrayList<>()*/ JSONObject();
        return level;
    }

    /**
     * @param level 2-D list representing a level
     * @return a string without newline representing the level, literal "0xA" separates lines
     */
    public static String toString(/*ArrayList<ArrayList<Character>>*/JSONObject level) {
        StringBuilder levelString = new StringBuilder();
        return levelString.toString().substring(0, levelString.length()-3); // remove final "0xA"
    }
    // TODO move to unit tests
    public static void main(String[] args) {
        try {
            /*ArrayList<ArrayList<Character>>*/JSONObject t = TerrainManager.load(0);
            System.out.println(toString(t));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
