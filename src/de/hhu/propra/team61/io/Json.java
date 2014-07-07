package de.hhu.propra.team61.io;

import de.hhu.propra.team61.io.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * Class containing convenience function for handling JSONObjects.
 */
public class Json {

    /**
     * Saves the given JSONObject to the given file.
     * @param json JSONObject to be saved
     * @param filename file to which the JSONObject is saved
     */
    public static void save(JSONObject json, String filename) {
        try(PrintWriter writer = new PrintWriter(filename, "UTF-8")) {
            writer.print(json);
        // TODO do something sensible here
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the contents of the given file in a JSONObject.
     * @param filename file to be read
     * @return json found in the file, empty json if the file does not exist
     */
    public static JSONObject getFromFile(String filename) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(Paths.get(filename));
        } catch(FileNotFoundException | NoSuchFileException e) {
            System.err.println("Json.getFromFile: " + filename + " not found, returning empty json object");
        } catch(IOException e) {
            System.err.println("Json.getFromFile: problem loading " + filename + ", returning empty json object");
            e.printStackTrace();
        }
        if(bytes == null) {
            bytes = new byte[]{'{', '}'}; // empty json string
        }
        return new JSONObject(new String(bytes));
    }

}
