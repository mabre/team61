package de.hhu.propra.team61.artificialIntelligence;

import de.hhu.propra.team61.Team;
import de.hhu.propra.team61.io.json.JSONObject;
import de.hhu.propra.team61.objects.Crate;
import de.hhu.propra.team61.objects.Terrain;

import java.util.ArrayList;

/**
 * Created by markus on 29.08.14. // TODO IMPORTANT doc
 */
public class ArtificialIntelligence {

    protected final Team ownTeam;
    protected final ArrayList<Team> enemyTeams;
    protected final Terrain terrain;
    protected final ArrayList<Crate> crates;
    protected final JSONObject gameSettings;
    protected final JSONObject gameSettings;

    public ArtificialIntelligence(Team ownTeam, ArrayList<Team> teams, Terrain terrain, ArrayList<Crate> crates, JSONObject gameSettings) {
        this.ownTeam = ownTeam;
        this.enemyTeams = new ArrayList<>();
        this.enemyTeams.addAll(teams);
        this.enemyTeams.remove(ownTeam);
        this.terrain = terrain;
        this.crates = crates;
        this.gameSettings = gameSettings;
    }

    public ArrayList<String> makeMove() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(ownTeam.getNumber() + " 9");
        commands.add(ownTeam.getNumber() + " Space");
        return commands;
    }

}
