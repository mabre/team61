package de.hhu.propra.team61.io;

import de.hhu.propra.team61.io.json.JSONObject;

/**
 * Created by markus on 13.05.14.
 */
public class Options {

    private static final String OPTIONS_FILE = "OPTIONS_FILE.conf";

    /**
     * @param json is saved to OPTIONS_FILE
     */
    public static void save(JSONObject json) {
        Json.save(json, OPTIONS_FILE);
    }

    /**
     * @return game state from OPTIONS_FILE
     */
    public static JSONObject getSavedSettings() {
        return Json.getFromFile(OPTIONS_FILE);
    }

}
