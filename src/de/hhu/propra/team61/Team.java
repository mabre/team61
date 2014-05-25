package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.Objects.Figure;
import de.hhu.propra.team61.Objects.Grenade;
import de.hhu.propra.team61.Objects.Gun;
import de.hhu.propra.team61.Objects.Weapon;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.StackPane;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by markus on 17.05.14.
 */
public class Team extends StackPane {
    private ArrayList<Figure> figures;
    private ArrayList<Weapon> weapons;

    public Team(ArrayList<Point2D> spawnPoints, ArrayList<Weapon> weapons) {
        this.weapons = weapons;
        figures = new ArrayList<>();
        for(Point2D sp: spawnPoints) {
            Figure f = new Figure("Max", 100, 100, false, false, false); // TODO @Kegny create sensible default constructor
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
        figures = new ArrayList<>();
        JSONArray figuresArray = state.getJSONArray("figures");
        for(int i=0; i<figuresArray.length(); i++) {
            Figure f = new Figure(figuresArray.getJSONObject(i));
            figures.add(f);
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
