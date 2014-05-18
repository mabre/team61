package de.hhu.propra.team61.IO;

import de.hhu.propra.team61.IO.JSON.JSONObject;

import java.io.*;

/**
 * Created by markus on 13.05.14.
 */
public class Settings {

    private static final String SETTINGS_FILE = "Afrobob.conf";

    /**
     * @param json is saved to SETTINGS_FILE
     */
    public static void save(JSONObject json) {
        Json.save(json, SETTINGS_FILE);
    }

    /**
     * @return game state from SAVE_STATE_FILE
     */
    public static JSONObject getSavedSettings() {
        return Json.getFromFile(SETTINGS_FILE);
    }

}
