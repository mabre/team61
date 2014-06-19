package de.hhu.propra.team61.io;

import de.hhu.propra.team61.io.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jessypet on 13.06.14.
 */
public class CustomizeManager {

    private static final String CUSTOMIZE_DIR = "resources/";
    private static final String CUSTOMIZE_FILE_EXTENSION = ".json";

    /**
     * @param json is saved to CUSTOMIZE_DIR + wanted name + CUSTOMIZE_FILE_EXTENSION
     */
    public static void save(JSONObject json, String file) {
        Json.save(json, CUSTOMIZE_DIR+file+CUSTOMIZE_FILE_EXTENSION);
    }

    /**
     * @return game state from wanted file
     */
    public static JSONObject getSavedSettings(String file) {
        return Json.getFromFile(CUSTOMIZE_DIR+file);
    }

    public static ArrayList<String> getAvailableTeams() {
        File dir = new File(CUSTOMIZE_DIR+"teams/");
        FilenameFilter filter = (f, s) -> s.toLowerCase().endsWith(CUSTOMIZE_FILE_EXTENSION);
        ArrayList<String> teams = new ArrayList<>(Arrays.asList(dir.list(filter)));
        return teams;
    }

    public static ArrayList<String> getAvailableGameStyles() {
        File dir = new File(CUSTOMIZE_DIR+"gamestyles/");
        FilenameFilter filter = (f, s) -> s.toLowerCase().endsWith(CUSTOMIZE_FILE_EXTENSION);
        ArrayList<String> gamestyles = new ArrayList<>(Arrays.asList(dir.list(filter)));
        return gamestyles;
    }

}
