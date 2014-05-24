package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.JSON.JSONArray;
import de.hhu.propra.team61.IO.JSON.JSONObject;
import de.hhu.propra.team61.IO.Settings;
import de.hhu.propra.team61.IO.TerrainManager;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Created by Jessypet on 21.05.14.
 */

public class GameSettings extends Application {

    int quantity = 2;
    TextField name1 = new TextField();
    TextField name2 = new TextField();
    TextField name3 = new TextField();
    TextField name4 = new TextField();
    ColorPicker colorPicker1 = new ColorPicker();
    ColorPicker colorPicker2 = new ColorPicker();
    ColorPicker colorPicker3 = new ColorPicker();
    ColorPicker colorPicker4 = new ColorPicker();
    TextField savefield = new TextField();
    TextField weapon1 = new TextField();
    TextField weapon2 = new TextField();
    TextField weapon3 = new TextField();
    TextField sizefield = new TextField();
    HBox hboxplus = new HBox(5);

    public void do_settings() throws IllegalArgumentException {
        Stage settingstage = new Stage();
        settingstage.setTitle("Game Settings");
        GridPane sgrid = new GridPane();
        sgrid.setAlignment(Pos.TOP_LEFT);
        sgrid.setHgap(10);
        sgrid.setVgap(10);
        sgrid.setPadding(new Insets(25, 25, 25, 25));

        //Save/load settings
        Text savetext = new Text("Save settings in ");
        sgrid.add(savetext, 0, 0);
        sgrid.add(savefield, 1, 0);
        Text extension = new Text(".conf");
        sgrid.add(extension, 2, 0);
        Button savebutton = new Button("Save");
        sgrid.add(savebutton, 3, 0);
        savebutton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Settings.save(toJson(), savefield.getText());               //create Json-object and save it in the wanted file
                System.out.println("GameSettings: saved settings");
            }
        });
        Text load = new Text("Load settings: ");
        sgrid.add(load, 0, 1);
        FileChooser loadChooser = new FileChooser();
        loadChooser.setTitle("Choose file to load");
        Button loadbtn = new Button("Choose file..");
        /*loadbtn.setOnAction(                                                  //Not working yet
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File loadfile = loadChooser.showOpenDialog(settingstage);     //Open file-window to open a file and save it in 'loadfile'
                        fromJson(loadfile);
                    }
                });*/
        sgrid.add(loadbtn, 1, 1);

        //Weapons, TextFields for entering a quantity
        Text weapons = new Text("Weapons");
        weapons.setFont(Font.font("Verdana", 20));
        sgrid.add(weapons, 0, 3);
        Text enter = new Text ("Enter the quantity of projectiles for each weapon.");
        sgrid.add(enter, 1, 3, 3, 1);
        Text w1 = new Text("Weapon 1: ");
        sgrid.add(w1, 0, 4);
        sgrid.add(weapon1, 1, 4);
        Text w2 = new Text("Weapon 2: ");
        sgrid.add(w2, 2, 4);
        sgrid.add(weapon2, 3, 4);
        Text w3 = new Text("Weapon 3: ");
        sgrid.add(w3, 4, 4);
        sgrid.add(weapon3, 5, 4);
        Text empty2 = new Text ("An empty field will lead to a number of 0.");
        sgrid.add(empty2, 6, 4);

        Text indi = new Text("Individual Team Settings");
        indi.setFont(Font.font("Verdana", 20));
        sgrid.add(indi, 0, 6, 3, 1);

        //Categories, type team-name and choose color
        Text name = new Text("Team-Name");
        sgrid.add(name, 1, 7);
        Text color = new Text("Color");
        sgrid.add(color, 2, 7);
        //Team 1
        Text team1 = new Text("Team 1");
        sgrid.add(team1, 0, 8);
        sgrid.add(name1, 1, 8);
        sgrid.add(colorPicker1, 2, 8);
        //Team 2
        Text team2 = new Text("Team 2");
        sgrid.add(team2, 0, 9);
        sgrid.add(name2, 1, 9);
        sgrid.add(colorPicker2, 2, 9);

        Button plus = new Button("+");                   //Button to add up to 2 more teams
        hboxplus.getChildren().addAll(plus);
        sgrid.add(hboxplus, 0, 10);

        plus.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) throws IllegalArgumentException {
                quantity++;
                if (quantity == 3) {
                    //Team 3
                    Text team3 = new Text("Team 3");
                    sgrid.add(team3, 0, 10);
                    sgrid.add(name3, 1, 10);
                    sgrid.add(colorPicker3, 2, 10);
                    sgrid.add(hboxplus, 0, 11);
                }
                if (quantity == 4) {
                    //Team 4
                    Text team4 = new Text("Team 4");
                    sgrid.add(team4, 0, 11);
                    sgrid.add(name4, 1, 11);
                    sgrid.add(colorPicker4, 2, 11);
                    hboxplus.getChildren().remove(plus);              //Maximum is 4, button is hidden now
                    Text enough = new Text("Max. 4 teams");
                    sgrid.add(enough, 1, 12);
                }
            }
        });

        Text sizetext = new Text("Team-Size:");
        sgrid.add(sizetext, 0, 14);
        sgrid.add(sizefield, 1, 14);
        Button cont = new Button("Continue");
        sgrid.add(cont, 0, 16);

        cont.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Settings.save(toJson(), "SETTINGS_FILE");               //create Json-object and save it in SETTINGS_FILE.conf
                System.out.println("GameSettings: saved settings");
                MapWindow mapwindow = new MapWindow(TerrainManager.getAvailableTerrains().get(0));
            }
        });

        Scene sscene = new Scene(sgrid, 1000, 600);
        settingstage.setScene(sscene);
        settingstage.show();
    }


    public JSONObject toJson() {
        JSONObject output = new JSONObject();
        output.put("teams", quantity);                      //save number of teams
        output.put("team-size", sizefield.getText());       //save size of teams
        output.put("weapon1", weapon1.getText());
        output.put("weapon2", weapon2.getText());
        output.put("weapon3", weapon3.getText());
        JSONArray team1 = formTeam(name1.getText(), colorPicker1);   //create an JSONArray for each team with the elements name and color
        output.put("team1", team1);
        JSONArray team2 = formTeam(name2.getText(), colorPicker2);
        output.put("team2", team2);
        if (quantity > 2) { JSONArray team3 = formTeam(name3.getText(), colorPicker3);
            output.put("team3", team3); }
        if (quantity > 3) { JSONArray team4 = formTeam(name4.getText(), colorPicker4);
            output.put("team4", team4); }
        return output;
    }

    /*public void fromJson(File loadfile) {

        JSONObject savedSettings = Settings.getSavedSettings(loadfile);
        if(savedSettings.has("teams")) {
            quantity = savedSettings.getInt("quantity");            //TODO show all teams
        }
        if(savedSettings.has("team-size")) {
            sizefield.setText(savedSettings.getString("team-size")); }
        if(savedSettings.has("weapon1")) {
            weapon1.setText(savedSettings.getString("weapon1")); }
        if(savedSettings.has("weapon2")) {
            weapon2.setText(savedSettings.getString("weapon2")); }
        if(savedSettings.has("weapon3")) {
            weapon3.setText(savedSettings.getString("weapon3")); }
        if(savedSettings.has("team1")) {
            JSONArray team1 = savedSettings.getJSONArray("team1");
            JSONObject nameobject1 = team1.getJSONObject(0);
            name1.setText(nameobject1.getString("name"));
                                   //TODO load colors
        }
        if(savedSettings.has("team2")) {
            JSONArray team2 = savedSettings.getJSONArray("team2");
            JSONObject nameobject2 = team2.getJSONObject(0);
            name2.setText(nameobject2.getString("name"));
        }
        if(savedSettings.has("team3")) {
            JSONArray team3 = savedSettings.getJSONArray("team3");
            JSONObject nameobject3 = team3.getJSONObject(0);
            name3.setText(nameobject3.getString("name"));
        }
        if(savedSettings.has("team4")) {
            JSONArray team4 = savedSettings.getJSONArray("team4");
            JSONObject nameobject4 = team4.getJSONObject(0);
            name4.setText(nameobject4.getString("name"));
        }
    }*/

    public JSONArray formTeam(String name, ColorPicker color) {
        JSONArray jarray = new JSONArray();
        JSONObject nameobject = new JSONObject();                   //create JSONArray with 2 objects name and color, one JSONArray for each team
        nameobject.put("name", name);
        jarray.put(nameobject);
        JSONObject colorobject = new JSONObject();
        String colorstring = Integer.toHexString(color.getValue().hashCode()).substring(0, 6).toUpperCase();    //save color in HEXA, conversion to int and then String not possible
        colorobject.put("color", colorstring);
        jarray.put(colorobject);
        return jarray;
    }

    @Override
    public void start(Stage settingstage) {

    }
}