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
 * Instances of this class represent a team.
 * A team has a certain number of figures, inventory (inventory is shared across all figures of the same team), a color,
 * and name. To create a new team, either {@link #Team(java.util.ArrayList, java.util.ArrayList, javafx.scene.paint.Color, String, String, de.hhu.propra.team61.io.json.JSONArray)}
 * can be used, passing all the team properties, or {@link #Team(de.hhu.propra.team61.io.json.JSONObject)}, providing
 * a json object representing the state of a team.
 * <p>
 * If it is this team’s turn, one can get the active figure with {@link #getCurrentFigure()}. When the team has finished
 * its turn, {@link #endRound()} should be called before the next turn of this team. The team can be removed from the
 * current game by calling {@link #suddenDeath()}.
 */
public class Team extends StackPane {
    /** the number of the currently active figure */
    private int currentFigure = 0;
    /** list of figures playing for this team */
    private ArrayList<Figure> figures;
    /** list of inventory the figures of this team can use */
    private ArrayList<Item> inventory;
    /** colors of the team */
    private Color color;
    /** name of the team */
    private String name;

    /**
     * Creates a new team with the given properties.
     * @param spawnPoints determines number and initial position of the figures
     * @param inventory the list of inventory for the team
     * @param color the color of the team
     * @param name the name of the team
     * @param chosenFigure the figure type ({@link de.hhu.propra.team61.objects.Figure#figureType}) for the figures of the team
     * @param figureNames the names of the figures
     */
    public Team(ArrayList<Point2D> spawnPoints, ArrayList<Item> inventory, Color color, String name, String chosenFigure, JSONArray figureNames) {
        this.inventory = inventory;
        this.color = color;
        this.name = name;
        figures = new ArrayList<>();
        for (int j=0; j < spawnPoints.size(); j++) {
            Point2D sp = spawnPoints.get(j);
            String figureName = j < figureNames.length() ? figureNames.getJSONObject(j).getString("figure") : "UNNAMED#"+j;
            Figure f = new Figure(figureName, chosenFigure, 100, 0, false, false, false); // TODO @Kegny create sensible default constructor
            f.setColor(this.color);
            figures.add(f);
            f.setPosition(sp);
            getChildren().add(f);
        }
        setAlignment(Pos.TOP_LEFT);
    }

    /**
     * Creates a team from a given JSONObject.
     * TODO doc how such an object looks like [has changed an items @Kegny]
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
     * Gets a json object which represents the overall state of the team.
     * This is like the inverse function to {@link #Team(de.hhu.propra.team61.io.json.JSONObject)}.
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

    /**
     * Calculates the next figure which should play.
     * Increases {@link #currentFigure} until a living figure is found. If no living figure can be found, {@link #currentFigure}
     * is set to {@code -1}.
     */
    public void endRound() {
        int i = 0;
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

    /**
     * Gets the currently active figure.
     * @return a reference to the currently active figure
     */
    public Figure getCurrentFigure() {
        return figures.get(currentFigure);
    }

    /**
     * Gets the number of living figures.
     * @return the number of living figures
     */
    public int getNumberOfLivingFigures() {
        int livingFigures = 0;
        for (Figure figure: figures){
            if(figure.getHealth() > 0){
                livingFigures++;
            }
        }
        return livingFigures;
    }

    /**
     * Gets the name of the team
     * @return the name of the team
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of figures in this team
     * @return a list of figures in this team
     */
    public ArrayList<Figure> getFigures() {
        return figures;
    }

    /**
     * Gets the weapon with the given number of this team.
     * @param i the number of the weapon
     * @return a reference to the weapon
     */
    public Item getItem(int i) {
        return inventory.get(i);
    }

    /**
     * Gets the weapon with the given name of this team.
     * @param s Name of the weapon
     * @return a refereance to the weapon
     */
    public Item getItem(String s) {
        for (Item i : inventory) {
            if (i.getName().equals(s)) {
                return i;
            }
        }
        return null; // If not found
    }

    /**
     * Gets the number of inventory of this team
     * @return the number of inventory of this team
     */
    public int getNumberOfWeapons() {
        return inventory.size();
    }

    /**
     * Kills all figures of this team.
     */
    public void suddenDeath() {
        for (Figure figure: figures){
            figure.setHealth(0);
        }
    }
}
