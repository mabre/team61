package de.hhu.propra.team61.gui;

import de.hhu.propra.team61.objects.Item;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

/**
 * Created by kevin on 25.06.14. ;
 */
public class InventoryOverlay extends GridPane { //ToDo; is still in development
    private static int xSize    = 0;
    private static int ySize    = 0;
    private static int iconSize = 0;

    private static ArrayList<ArrayList<Item>> inventoryOverlay; //ToDo fill it up

    private boolean isOn        = false;

    public InventoryOverlay(){
        setOpacity(80);
        setTranslateX(0);
        setTranslateY(0);
        //?setBackground();
    }

    public void toggleVisibility(){
        if(isOn){
            setVisible(false);
            isOn = false;
        } else {
            setVisible(true);
            isOn = true;
        }
    }
}
