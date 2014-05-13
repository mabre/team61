package de.hhu.propra.team61.IO;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TerrainManager {

    private static final String LEVEL_DIR = "resources/levels/";
    private static final String SAVE_LEVEL_FILE = "Afrobob.level.conf";

    /**
     * @return a list of available levels, w/o path, including file name extension
     */
    public static ArrayList<String> getAvailableTerrains() {
        File dir = new File(LEVEL_DIR);
        FilenameFilter filter = (f, s) -> s.toLowerCase().endsWith(".txt");
        ArrayList<String> levels = new ArrayList<>(Arrays.asList(dir.list(filter)));
        System.out.println("Levels found: "+levels);
        return levels;
    }

    /**
     * @param filename the file containing the board to be loaded
     * @return an ArrayList containing the board ([row][column])
     * How a valid board looks like is documented in BoardLegend
     */
    public static ArrayList<ArrayList<Character>> load(String filename) {
        ArrayList<ArrayList<Character>> rows = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(LEVEL_DIR + filename))) {
            String line;
            while( (line = br.readLine()) != null) {
                ArrayList<Character> row = new ArrayList<>();
                for(int i=0; i<line.length(); i++) {
                    row.add(line.charAt(i));
                }
                rows.add(row);
            }
        // TODO do something sensible here
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return rows;
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

}
