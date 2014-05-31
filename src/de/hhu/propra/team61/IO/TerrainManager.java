package de.hhu.propra.team61.IO;

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
    public static ArrayList<ArrayList<Character>> load(String filename) throws FileNotFoundException {
        ArrayList<ArrayList<Character>> rows = new ArrayList<>();
        String dir = LEVEL_DIR;
        if(filename.equals(SAVE_LEVEL_FILE)) dir = "";

        try(BufferedReader br = new BufferedReader(new FileReader(dir + filename))) {
            String line;
            while( (line = br.readLine()) != null) {
                ArrayList<Character> row = new ArrayList<>();
                for(int i=0; i<line.length(); i++) {
                    row.add(line.charAt(i));
                }
                rows.add(row);
            }
        // TODO do something sensible herewurm
        } catch(IOException e) {
            e.printStackTrace();
        }

        return rows;
    }

    /**
     * @param levelnumber the index of the level in the ArrayList returned by {@link #getAvailableTerrains() getAvailableTerrains}
     * @return an ArrayList containing the board ([row][column])
     */
    public static ArrayList<ArrayList<Character>> load(int levelnumber) throws FileNotFoundException {
        return load(getAvailableTerrains().get(levelnumber));
    }

    /**
     * @param levelstring string representation of a level
     * @return 2-D ArrayList representing the given level
     */
    public static ArrayList<ArrayList<Character>> loadFromString(String levelstring) {
        ArrayList<ArrayList<Character>> level = new ArrayList<>();
        ArrayList<String> rows = new ArrayList<>(Arrays.asList(levelstring.split("0xA")));
        for(String row: rows) {
            ArrayList<Character> fields = new ArrayList<>();
            for(int i=0; i<row.length(); i++) {
                fields.add(row.charAt(i));
            }
            level.add(fields);
        }
        return level;
    }

    /**
     * @param level is saved as ordinary level file to SAVE_LEVEL_FILE
     */
    public static void save(ArrayList<ArrayList<Character>> level) {
        String levelString = "";
        for(ArrayList<Character> row : level) {
            for(Character field : row) {
                levelString += field;
            }
            levelString += "\n";
        }
        if(!levelString.equals("")) {
            levelString = levelString.substring(0, levelString.length()-1);
        }
        try(PrintWriter writer = new PrintWriter(SAVE_LEVEL_FILE, "UTF-8")) {
            writer.print(levelString);
        // TODO do something sensible here
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param level 2-D list representing a level
     * @return a string without newline representing the level, literal "0xA" separates lines
     */
    public static String toString(ArrayList<ArrayList<Character>> level) {
        StringBuilder levelString = new StringBuilder();

        for(ArrayList<Character> row : level) {
            row.forEach(levelString::append);
            levelString.append("0xA");
        }

        return levelString.toString().substring(0, levelString.length()-3); // remove final "0xA"
    }

    /**
     * @return an ArrayList containing the board saved in SAVE_LEVEL_FILE, or the first level when the file does not exist
     */
    public static ArrayList<ArrayList<Character>> loadSavedLevel() {
        try {
            return load(SAVE_LEVEL_FILE);
        } catch (FileNotFoundException e) {
            try {
                return load(0);
            } catch (FileNotFoundException e1) {
                System.out.println("No levels?");
                e1.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

    // TODO move to unit tests
    public static void main(String[] args) {
        try {
            ArrayList<ArrayList<Character>> t = TerrainManager.load(0);
            System.out.println(toString(t));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
