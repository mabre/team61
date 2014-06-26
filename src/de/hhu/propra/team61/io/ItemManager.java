package de.hhu.propra.team61.io;

import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.objects.Item;
import de.hhu.propra.team61.objects.Weapon;
import de.hhu.propra.team61.objects.itemtypes.*;

import java.util.ArrayList;

/**
 * Created by kevin on 21.06.14.
 *
 * This class is used to norm all inventories, making loading and saving less complicated.
 * It also makes it easier to add new Items, since they now are mainly controlled in here.
 *
 * ToDo write some more justifications
 */
public class ItemManager {
    public final static String[] itemlist = {"Bazooka","Grenade","Shotgun","PoisonedArrow","Medipack","Rifle","Digiwise",
                                             "Bananabomb"};
    public final static int numberOfItems = itemlist.length;

    /**
     * @param inventory JSONArray OF INT being the munition value; Weapontype defined by position in Array
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

    public static JSONArray inventoryToJsonArray(ArrayList<Item> inventory){
        JSONArray temp = new JSONArray();

        for(Item i : inventory){ temp.put(i.getMunition()); }
        return temp;
    }

    public static Item returnItem(String input){
        switch(input){
            case "Bazooka": return new Bazooka(1);
            case "Grenade": return new Grenade(1);
            case "Shotgun": return new Shotgun(1);
            case "PoisonedArrow": return new PoisonedArrow(1);
            case "Medipack": return new Medipack(1);
            case "Rifle": return new Rifle(1);
            case "Digiwise": return new Digiwise(1);
            case "Bananabomb": return new Bananabomb(1);
            default: return null;
        }
    }
}
