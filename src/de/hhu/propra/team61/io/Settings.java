package de.hhu.propra.team61.io;

import de.hhu.propra.team61.io.json.JSONObject;

/**
 * Saves game settings.
 *
 * Created by Jessypet on 24.05.14.
 */
public class Settings {

    static final String PREFS_FILE = "Afrobob.conf";

    /**
     * Saves the given JSONObject to the given file
     * @param json the json to be saved
     * @param file the file to which the json is saved
     */
    public static void saveJson(JSONObject json, String file) {
        file = file+".conf";
        Json.save(json, file);
    }

    /**
     * Creates a JSONObject from the contents of the given file.
     * @param filename the file containing the json
     * @return the json saved in the given file
     */
    public static JSONObject getSavedJson(String filename) {
        return Json.getFromFile(filename);
    }

    public static void savePrefs(JSONObject json) {
        Json.save(json, PREFS_FILE);
    }

    /**
     * Gets the saved preferences as json.
     * @return json in {@link #PREFS_FILE}
     */
    public static JSONObject getSavedPrefs() {
        return Json.getFromFile(PREFS_FILE);
    }

    /**
     * Gets the value of a boolean preference.
     * @param key the key of the setting
     * @param defaultValue fall back value
     * @return the value for the key
     */
    public static boolean getSavedBoolean(String key, boolean defaultValue) {
        return Json.getFromFile(PREFS_FILE).getBoolean(key, defaultValue);
    }

    /**
     * Gets the value of an integer preference.
     * {@code getSavedInt("foo", 42)} will return the value of the key foo in {@code SETTINGS.conf}, or {@code 42} if
     * the value does not exist in the file, or the file does not exist.
     * @param key the key of the setting
     * @param defaultValue fall back value
     * @return the value for the key
     */
    public static int getSavedInt(String key, int defaultValue) {
        return Json.getFromFile(PREFS_FILE).getInt(key, defaultValue);
    }

}
