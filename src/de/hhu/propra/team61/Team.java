package de.hhu.propra.team61;

import de.hhu.propra.team61.io.ItemManager;
import de.hhu.propra.team61.io.json.JSONArray;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.objects.*;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static de.hhu.propra.team61.JavaFxUtils.toHex;

/**
 * Created by markus on 17.05.14.
 */


public class Team extends StackPane {
    private int i = 0;
    public int currentFigure = 0;
    private ArrayList<Figure> figures;
    private ArrayList<Item> inventory;
    private Color color;
    private String name;

    public Team(ArrayList<Point2D> spawnPoints, ArrayList<Item> inventory, Color color, String name, String chosenFigure, JSONArray figureNames) {

        this.inventory = inventory;
        this.color = color;
        this.name = name;
        figures = new ArrayList<>();
        for (int j=0; j < spawnPoints.size(); j++) {
            Point2D sp = spawnPoints.get(j);
            Figure f = new Figure(figureNames.getJSONObject(j).getString("figure"), chosenFigure, 100, 0, false, false, false); // TODO @Kegny create sensible default constructor
            f.setColor(this.color);
            figures.add(f);
            f.setPosition(sp);
            getChildren().add(f);
        }
        setAlignment(Pos.TOP_LEFT);
    }

    /**
     * create a team from a given JSONObject
     * @param state the JSONObject representing the team state
     */
    public Team(JSONObject state) {
        color = Color.web(state.getString("color"));
        name = state.getString("name");
        figures = new ArrayList<>();
        JSONArray figuresArray = state.getJSONArray("figures");
        for(int i=0; i<figuresArray.length(); i++) {
            Figure f = new Figure(figuresArray.getJSONObject(i));
            figures.add(f);
            f.setColor(color);
            getChildren().add(f);
        }
        JSONArray itemsArray = state.getJSONArray("inventory");
        inventory = ItemManager.generateInventory(itemsArray);
        currentFigure = state.getInt("currentFigure");
        setAlignment(Pos.TOP_LEFT);
    }

    /**
     * @return a JSONObject representing the state of this team, including its figures
     */
    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        JSONArray figuresArray = new JSONArray();
        for(Figure f: figures) {
            figuresArray.put(f.toJson());
        }
        output.put("figures", figuresArray);
        output.put("inventory", ItemManager.inventoryToJsonArray(inventory));
        output.put("color", toHex(color));
        output.put("name", name);
        output.put("currentFigure", currentFigure);
        return output;
    }

    public void endRound() {
        i = 0;
        do {
            if (i == figures.size()) {
                currentFigure = -1;
                break;
            }
            currentFigure++;
            if (currentFigure == figures.size()) {
                currentFigure = 0; // loop
            }
            i++;
            if (figures.get(currentFigure).getIsParalyzed() && getNumberOfLivingFigures() > 1){currentFigure++; i++;} //Skip paralyzed figures IF not last man standing
        }
        while (figures.get(currentFigure).getHealth() == 0);
    }

    public Figure getCurrentFigure() {
        return figures.get(currentFigure);
    }

    public int getNumberOfLivingFigures() {
        int livingFigures = 0;
        for (Figure figure: figures){
            if(figure.getHealth() > 0){
                livingFigures++;
            }
        }
        return livingFigures;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Figure> getFigures() {
        return figures;
    }

    public Item getItem(int i) {
        return inventory.get(i);
    }
    public Item getItem(String s) {
        for(Item i : inventory){
            if(i.getName().equals(s)){  return i; }
        }
        return null; // If not found; To avoid
    }

    public void suddenDeath() {
        for (Figure figure: figures){
            figure.setHealth(0);
        }
    }
}
