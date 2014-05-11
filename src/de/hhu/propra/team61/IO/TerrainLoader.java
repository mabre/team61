package de.hhu.propra.team61.IO;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TerrainLoader {

    /**
     * @param filename the file containing the board to be loaded
     * @return an ArrayList containing the board ([row][column])
     * How a valid board looks like is documented in BoardLegend
     */
    public static ArrayList<ArrayList<Character>> load(String filename) {
        ArrayList<ArrayList<Character>> rows = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
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

}
