package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.Objects.Figure;
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

    public Team(ArrayList<Point2D> spawnPoints) {
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
        setAlignment(Pos.TOP_LEFT);
    }

    public Team(String name, ColorPicker color) {
        String[] team = new String[2];
        team[0] = name;
        String colorstring = Integer.toHexString(color.getValue().hashCode()).substring(0, 6).toUpperCase();  //convert chosen color to Hexa (conversion to int and then to String not possible)
        team[1] = colorstring;
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
        return output;
    }

}
