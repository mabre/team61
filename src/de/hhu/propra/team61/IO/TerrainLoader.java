package de.hhu.propra.team61.IO;

import java.util.ArrayList;

public class TerrainLoader {

    /**
     * @param filename the file containing the board to be loaded
     * @return an ArrayList containing the board ([row][column])
     * A valid board file may contain the following chars:
     * TODO
     */
    public static ArrayList<ArrayList<Character>> load(String filename) {
        // TODO read actual file
        ArrayList<ArrayList<Character>> rows = new ArrayList<>();

        ArrayList<Character> l0 = new ArrayList<>();
        l0.add('1');
        l0.add(' ');
        l0.add('2');
        l0.add(' ');
        l0.add(' ');
        rows.add(l0);

        ArrayList<Character> l1 = new ArrayList<>();
        l1.add(' ');
        l1.add('/');
        l1.add('_');
        l1.add('\\');
        l1.add(' ');
        rows.add(l1);

        ArrayList<Character> l2 = new ArrayList<>();
        l2.add('/');
        l2.add('E');
        l2.add('S');
        l2.add('E');
        l2.add('\\');
        rows.add(l2);

        ArrayList <Character> l3 = new ArrayList<>();
        l3.add('/');
        l3.add('E');
        l3.add('S');
        l3.add('E');
        l3.add('\\');
        rows.add(l3);

        ArrayList<Character> l4 = new ArrayList<>();
        l4.add('*');
        l4.add('*');
        l4.add('*');
        l4.add('*');
        l4.add('*');
        rows.add(l4);

        return rows;
    }

}
