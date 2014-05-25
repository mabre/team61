package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.Objects.Figure;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;

/**
 * Created by markus on 17.05.14.
 */


public class Team extends StackPane {
    private int i = 0;
    public int currentFigure = 0;
    private ArrayList<Figure> figures;

    public Team(ArrayList<Point2D> spawnPoints) {
        figures = new ArrayList<>();
        for (Point2D sp : spawnPoints) {
            Figure f = new Figure("Max", 100, 100, false, false, false); // TODO @Kegny create sensible default constructor
            figures.add(f);
            f.setPosition(sp);
            getChildren().add(f);
        }
        setAlignment(Pos.TOP_LEFT);
    }






    public void endRound() {
        i = 0;                                      //nonsense from Dinii
        while (figures.get(currentFigure).getHealth() == 0) {
            if (i == figures.size()) {
                currentFigure = -1;
                break;
            }
            currentFigure++;                        //till here <-
            if (currentFigure == figures.size()) {
                currentFigure = 0;
            }
            i++;
        }
    }

    public int getCurrentPin() {return currentFigure;}

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

    public ArrayList<Figure> getFigures() {
        return figures;
    }
}
