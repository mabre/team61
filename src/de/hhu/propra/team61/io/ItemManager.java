package de.hhu.propra.team61.io;

import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.objects.Item;
import de.hhu.propra.team61.objects.itemtypes.*;

import java.util.ArrayList;

// Created by kevin on 21.06.14.
/**
 * <u>This class</u> is used to <b>norm</b> all inventories, making loading, saving and adding new {@link de.hhu.propra.team61.objects.Item}s and {@link de.hhu.propra.team61.objects.Weapon}s less complicated.<p>
 * Inventory management <b>should</b> be done through this class.
 */
public class ItemManager {
    /** List of <b>all</b> {@link de.hhu.propra.team61.objects.Item}s(and {@link de.hhu.propra.team61.objects.Weapon}s) existent
     * in the game
     */
    public final static String[] itemlist = {"Bazooka","Grenade","Shotgun","PoisonedArrow","Medipack","Rifle","Digiwise",
                                             "Bananabomb"};
    /** Amount of those {@link de.hhu.propra.team61.objects.Item}s */
    public final static int numberOfItems = itemlist.length;

    /**
     * Generates an filled Inventory using a JSONArray
     *
     * @param inventory JSONArray of {@link java.lang.Integer} being the munition value; The Weapontype is defined by position in Array
     * @return ArrayList<Item> ready to use
     */
    public static ArrayList<Item> generateInventory(JSONArray inventory){
        ArrayList<Item> items = new ArrayList<>();

        //Since only ItemManager is supposed to create the ArrayLists or put them to JSON, Array is always same-sorted
        items.add(new Bazooka(inventory.getInt(0)));
        items.add(new Grenade(inventory.getInt(1)));
        items.add(new Shotgun(inventory.getInt(2)));
        items.add(new PoisonedArrow(inventory.getInt(3)));
        items.add(new Medipack(inventory.getInt(4)));
        items.add(new Rifle(1)); //ToDo: Ask Jessi for more inputs in gui
        items.add(new Digiwise(1));
        items.add(new Bananabomb(5));
        return items;
    }

    /**
     * This function creates an JSONArray containing the munitions of all {@link de.hhu.propra.team61.objects.Item}s in the Inventory<p>
     * The inventory is trough its generation in here correctly sorted, thus the Inventory is simply iterated
     *
     * @param inventory
     * @return JSONArray of {@link java.lang.Integer} indicating the {@link de.hhu.propra.team61.objects.Item#munition} of an {@link de.hhu.propra.team61.objects.Item} to be saved
     */
    public static JSONArray inventoryToJsonArray(ArrayList<Item> inventory){
        JSONArray temp = new JSONArray();

        for(Item i : inventory){ temp.put(i.getMunition()); }
        return temp;
    }

    /**
     * Returns an Item with a single shot for technical aspects an used in {@link de.hhu.propra.team61.objects.Projectile#Projectile(de.hhu.propra.team61.io.json.JSONObject)},<p>
     * where an Instance of a certain {@link de.hhu.propra.team61.objects.Weapon}type is needed but cannot be passed through JSON to clients.<p>
     *
     * @param input String with the Name of needed Weapontype
     * @return instance of the weapon with 1 munition
     */
    public static Item returnItem(String input){
        switch(input){
            case "Bazooka":       return new Bazooka(1);
            case "Grenade":       return new Grenade(1);
            case "Shotgun":       return new Shotgun(1);
            case "PoisonedArrow": return new PoisonedArrow(1);
            case "Medipack":      return new Medipack(1);
            case "Rifle":         return new Rifle(1);
            case "Digiwise":      return new Digiwise(1);
            case "Bananabomb":    return new Bananabomb(1);
            default:              return null;
        }
    }
}
