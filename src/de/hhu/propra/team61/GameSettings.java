package de.hhu.propra.team61;

import de.hhu.propra.team61.IO.TerrainManager;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
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

    int anzahl = 2;
    TextField name1 = new TextField();
    TextField name2 = new TextField();
    TextField name3 = new TextField();
    TextField name4 = new TextField();
    ColorPicker colorPicker1 = new ColorPicker();
    ColorPicker colorPicker2 = new ColorPicker();
    ColorPicker colorPicker3 = new ColorPicker();
    ColorPicker colorPicker4 = new ColorPicker();
    HBox hboxplus = new HBox(5);

    public void do_settings() {
        Stage settingstage = new Stage();
        settingstage.setTitle("Game Settings");
        GridPane sgrid = new GridPane();
        sgrid.setAlignment(Pos.TOP_LEFT);
        sgrid.setHgap(10);
        sgrid.setVgap(10);
        sgrid.setPadding(new Insets(25, 25, 25, 25));

        //Save/load settings
        Text save = new Text("Save settings in ");
        sgrid.add(save, 0, 0);
        TextField savef = new TextField();
        sgrid.add(savef, 1, 0);
        Text extension = new Text(".conf");
        sgrid.add(extension, 2, 0);


        Text load = new Text("Load settings: ");
        sgrid.add(load, 0, 1);
        FileChooser loadChooser = new FileChooser();
        loadChooser.setTitle("Choose file to load");
        Button loadbtn = new Button("Choose file..");
        loadbtn.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = loadChooser.showOpenDialog(settingstage);
                    }
                });
        sgrid.add(loadbtn, 1, 1);


        //Weapons, TextFields for entering a quantity
        Text weapons = new Text("Weapons");
        weapons.setFont(Font.font("Verdana", 20));
        sgrid.add(weapons, 0, 3);
        Text enter = new Text ("Enter the quantity of projectiles for each weapon.");
        sgrid.add(enter, 1, 3, 3, 1);
        Text w1 = new Text("Weapon 1: ");
        TextField weapon1 = new TextField();
        weapon1.setPrefWidth(20);
        sgrid.setHalignment(weapon1, HPos.CENTER);
        sgrid.add(w1, 0, 4);
        sgrid.add(weapon1, 1, 4);
        Text w2 = new Text("Weapon 2: ");
        TextField weapon2 = new TextField();
        sgrid.add(w2, 2, 4);
        sgrid.add(weapon2, 3, 4);
        Text w3 = new Text("Weapon 3: ");
        TextField weapon3 = new TextField();
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

        Button plus = new Button("+");
        hboxplus.getChildren().addAll(plus);
        sgrid.add(hboxplus, 0, 10);

        plus.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                anzahl++;
                if (anzahl == 3) {
                    //Team 3
                    Text team3 = new Text("Team 3");
                    sgrid.add(team3, 0, 10);
                    sgrid.add(name3, 1, 10);
                    sgrid.add(colorPicker3, 2, 10);
                    sgrid.add(hboxplus, 0, 11);
                }
                if (anzahl == 4) {
                    //Team 4
                    Text team4 = new Text("Team 4");
                    sgrid.add(team4, 0, 11);
                    sgrid.add(name4, 1, 11);
                    sgrid.add(colorPicker4, 2, 11);
                    hboxplus.getChildren().remove(plus);
                    Text enough = new Text("Max. 4 teams");
                    sgrid.add(enough, 1, 12);
                }
            }
        });
        Button cont = new Button("Continue");
        sgrid.add(cont, 0, 16);

        cont.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                MapWindow mapwindow = new MapWindow(TerrainManager.getAvailableTerrains().get(0));
            }
        });

        Scene sscene = new Scene(sgrid, 1000, 600);
        settingstage.setScene(sscene);
        settingstage.show();
    }

    @Override
    public void start(Stage settingstage) {

    }
}