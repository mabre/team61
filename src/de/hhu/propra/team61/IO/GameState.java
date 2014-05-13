package de.hhu.propra.team61.IO;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by markus on 13.05.14.
 */
public class GameState {

    private static final String SAVE_STATE_FILE = "Afrobob.state.conf";

    /**
     * @param json is saved to SAVE_STATE_FILE
     */
    public static void save(String json) {
        try(PrintWriter writer = new PrintWriter(SAVE_STATE_FILE, "UTF-8")) {
            writer.print(json);
            // TODO do something sensible here
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
