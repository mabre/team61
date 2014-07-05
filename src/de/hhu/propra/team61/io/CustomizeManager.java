package de.hhu.propra.team61.io;

import de.hhu.propra.team61.io.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Responsible for loading existing teams, game styles and map into {@link de.hhu.propra.team61.CustomizeWindow} and
 * saving customized results.
 *
 * Created by Jessypet on 13.06.14.
 */
public class CustomizeManager {

    /** directory for all files */
    private static final String CUSTOMIZE_DIR = "resources/";
    /** extension for json-files (teams and game styles) */
    private static final String JSON_FILE_EXTENSION = ".json";
    /** extension for levels */
    private static final String LVL_FILE_EXTENSION = ".lvl";

    /**
     * @param json is saved to CUSTOMIZE_DIR + wanted name + JSON_FILE_EXTENSION
     */
    public static void save(JSONObject json, String file) {
        Json.save(json, CUSTOMIZE_DIR+file+JSON_FILE_EXTENSION);
    }

    /**
     * @param json is saved to CUSTOMIZE_DIR + wanted name + LVL_FILE_EXTENSION
     * @param file wanted name
     */
    public static void saveLevel(JSONObject json, String file) {
        if (file.endsWith(".lvl")) {
            Json.save(json, CUSTOMIZE_DIR+file);
        } else {
            Json.save(json, CUSTOMIZE_DIR + file + LVL_FILE_EXTENSION);
        }
    }

    /**
     * @return game state from wanted file
     */
    public static JSONObject getSavedSettings(String file) {
        return Json.getFromFile(CUSTOMIZE_DIR+file);
    }

    /**
     * Loads all existing teams.
     * @return ArrayList of available teams.
     */
    public static ArrayList<String> getAvailableTeams() {
        File dir = new File(CUSTOMIZE_DIR+"teams/");
        FilenameFilter filter = (f, s) -> s.toLowerCase().endsWith(JSON_FILE_EXTENSION);
        ArrayList<String> teams = new ArrayList<>(Arrays.asList(dir.list(filter)));
        return teams;
    }

    /**
     * Loads all existing game styles.
     * @return ArrayList of available styles.
     */
    public static ArrayList<String> getAvailableGameStyles() {
        File dir = new File(CUSTOMIZE_DIR+"gamestyles/");
        FilenameFilter filter = (f, s) -> s.toLowerCase().endsWith(JSON_FILE_EXTENSION);
        ArrayList<String> gamestyles = new ArrayList<>(Arrays.asList(dir.list(filter)));
        return gamestyles;
    }

    /**
     * Loads all existing levels.
     * @return ArrayList of available levels.
     */
    public static ArrayList<String> getAvailableLevels() {
        File dir = new File(CUSTOMIZE_DIR+"levels/");
        FilenameFilter filter = (f, s) -> s.toLowerCase().endsWith(".lvl");
        ArrayList<String> levels = new ArrayList<>(Arrays.asList(dir.list(filter)));
        return levels;
    }

    /**
     * Loads all existing background images.
     * @return ArrayList of available backgrounds.
     */
    public static ArrayList<String> getAvailableBackgrounds() {
        File dir = new File(CUSTOMIZE_DIR+"levels/");
        FilenameFilter filter = (f, s) -> s.toLowerCase().endsWith(".png");
        ArrayList<String> backgrounds = new ArrayList<>(Arrays.asList(dir.list(filter)));
        return backgrounds;
    }

    public static ArrayList<String> getAvailableBackgroundMusic() {
        File dir = new File(CUSTOMIZE_DIR+"audio/BGM/");
        FilenameFilter filter = (f, s) -> s.toLowerCase().endsWith(".ogg");
        ArrayList<String> music = new ArrayList<>(Arrays.asList(dir.list(filter)));
        return music;
    }

}
