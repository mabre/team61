package de.hhu.propra.team61.IO;

import de.hhu.propra.team61.IO.JSON.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by markus on 13.05.14.
 */
public class GameState {

    private static final String SAVE_STATE_FILE = "Afrobob.state.conf";

    /**
     * @param json is saved to SAVE_STATE_FILE
     */
    public static void save(JSONObject json) {
        try(PrintWriter writer = new PrintWriter(SAVE_STATE_FILE, "UTF-8")) {
            writer.print(json);
        // TODO do something sensible here
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return game state from SAVE_STATE_FILE
     */
    public static JSONObject getSavedGameState() {
        byte[] bytes = null;
        try(BufferedReader br = new BufferedReader(new FileReader(SAVE_STATE_FILE))) {
            bytes = Files.readAllBytes(Paths.get(SAVE_STATE_FILE));
        // TODO do something sensible here
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(new String(bytes));
    }

}
