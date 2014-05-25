package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.Objects.Figure;
import de.hhu.propra.team61.Objects.Grenade;
import de.hhu.propra.team61.Objects.Gun;
import de.hhu.propra.team61.Objects.Weapon;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * Created by markus on 17.05.14.
 */


public class Team extends StackPane {
    private int i = 0;
    public int currentFigure = 0;
    private ArrayList<Figure> figures;
    private ArrayList<Weapon> weapons;
    private Color color;

    public Team(ArrayList<Point2D> spawnPoints, ArrayList<Weapon> weapons, Color color) {
        this.weapons = weapons;
        this.color = color;
        figures = new ArrayList<>();
        for (Point2D sp : spawnPoints) {
            Figure f = new Figure("Max", 100, 100, false, false, false); // TODO @Kegny create sensible default constructor
            f.setColor(this.color);
            figures.add(f);
            f.setPosition(sp);
            getChildren().add(f);
        }
        setAlignment(Pos.TOP_LEFT);
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
                currentFigure = 0;
            }
            i++;
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

    /**
     * create a team from a given JSONObject
     * @param state the JSONObject representing the team state
     */
    public Team(JSONObject state) {
        color = Color.web(state.getString("color"));
        figures = new ArrayList<>();
        JSONArray figuresArray = state.getJSONArray("figures");
        for(int i=0; i<figuresArray.length(); i++) {
            Figure f = new Figure(figuresArray.getJSONObject(i));
            figures.add(f);
            f.setColor(color);
            getChildren().add(f);
        }
        weapons = new ArrayList<>();
        JSONArray weaponsArray = state.getJSONArray("weapons");
        for(int i=0; i<weaponsArray.length(); i++) {
            Weapon w = null;
            if(weaponsArray.getJSONObject(i).getString("type").equals("Gun")) {
                w = new Gun(weaponsArray.getJSONObject(i));
            } else if(weaponsArray.getJSONObject(i).getString("type").equals("Grenade")) {
                w = new Grenade(weaponsArray.getJSONObject(i));
            }
            weapons.add(w);
        }
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
        JSONArray weaponsArray = new JSONArray();
        for(Weapon w: weapons) {
            weaponsArray.put(w.toJson());
        }
        output.put("weapons", weaponsArray);
        output.put("color", "#"+Integer.toHexString(color.hashCode()).substring(0, 6));
        return output;
    }

    public ArrayList<Figure> getFigures() {
        return figures;
    }

    public Weapon getWeapon(int i) {
        return weapons.get(i);
    }

    public int getNumberOfWeapons() {
        return weapons.size();
    }
}
