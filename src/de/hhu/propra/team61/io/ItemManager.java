package de.hhu.propra.team61.io;

import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.objects.Item;
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
    public final static String[] itemlist = {"Bazooka","Grenade","Shotgun","PoisonedArrow","Medipack","Rifle"};
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
        return items;
    }

    public static JSONArray inventoryToJsonArray(ArrayList<Item> inventory){
        JSONArray temp = new JSONArray();

        for(Item i : inventory){ temp.put(i.getMunition()); }
        return temp;
    }
}
