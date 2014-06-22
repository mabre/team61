package de.hhu.propra.team61;

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
    private ArrayList<Weapon> weapons;
    private Color color;
    private String name;

    public Team(ArrayList<Point2D> spawnPoints, ArrayList<Weapon> weapons, Color color, String name, String chosenFigure, JSONArray figureNames) {
        this.weapons = weapons;
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
        weapons = new ArrayList<>();
        JSONArray weaponsArray = state.getJSONArray("weapons");
        for(int i=0; i<weaponsArray.length(); i++){
            JSONObject w = weaponsArray.getJSONObject(i);
            switch(w.getString("name")){
                case "Bazooka": weapons.add(new Bazooka(w.getInt("munition")));
                    break;
                case "Grenade": weapons.add(new Grenade(w.getInt("munition")));
                    break;
                case "Shotgun": weapons.add(new Shotgun(w.getInt("munition")));
                    break;
                case "Poisoned Arrow": weapons.add(new PoisonedArrow(w.getInt("munition")));
                    break;
            }
        }
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
        JSONArray weaponsArray = new JSONArray();
        for(Weapon w: weapons) {
            weaponsArray.put(w.toJson());
        }
        output.put("weapons", weaponsArray);
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

    public String getName() {
        return name;
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

    public void suddenDeath() {
        for (Figure figure: figures){
            figure.setHealth(0);
        }
    }
}
