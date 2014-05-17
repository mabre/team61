package de.hhu.propra.team61.Objects;

import de.hhu.propra.team61.IO.JSON.JSONObject;

/**
 * Created by kevgny on 14.05.14.
 */

public class Figure {
    private String name;
    private int health;
    private int armor;

    private boolean isBurning;
    private boolean isPoisoned;
    private boolean isStuck;

    // In and Out
    public Figure(String name, int hp, int armor, boolean isBurning, boolean isPoisoned, boolean isStuck){
        this.name   = name;
        this.health = hp;
        this.armor  = armor;

        this.isBurning  = isBurning;
        this.isPoisoned = isPoisoned;
        this.isStuck    = isStuck;
    }
    public Figure(JSONObject input){
        this.name = input.getString("name");
        this.health = input.getInt("health");
        this.armor  = input.getInt("armor");
        this.isBurning  = input.getBoolean("isBurning");
        this.isPoisoned = input.getBoolean("isPoisoned");
        this.isStuck    = input.getBoolean("isStuck");
    }
    public JSONObject toJson(){
        JSONObject output = new JSONObject();
        output.put("name", name);
        output.put("health", health);
        output.put("armor", armor);

        output.put("isBurning", isBurning);
        output.put("isPoisoned", isPoisoned);
        output.put("isStuck",isStuck);
        return output;
    }


    // Getter and Setter
    public String getName(){return name;}
    public void setName(String name){this.name = name;}

    public int getHealth() {return health;}
    public void setHealth(int health) {this.health = health;}

    public int getArmor() {return armor;}
    public void setArmor(int armor) {this.armor = armor;}


    public boolean getIsBurning() {return isBurning;}
    public void setIsBurning(boolean isBurning){this.isBurning = isBurning;}
    public boolean getIsPoisoned() {return isPoisoned;}

    public void setIsPoisoned(boolean isPoisoned){this.isPoisoned = isPoisoned;}

    public boolean getIsStuck() {return isStuck;}
    public void setIsStuck(boolean isStuck){this.isStuck = isStuck;}



    //For testing purposes only
    private static void printAllAttributes(Figure testwurm){
        System.out.println("Health  : " + testwurm.getHealth());
        System.out.println("Armor   : " + testwurm.getArmor());
        System.out.println("Name    : " + testwurm.getName());
        System.out.println("Burning : " + testwurm.getIsBurning());
        System.out.println("Poisoned: " + testwurm.getIsPoisoned());
        System.out.println("Stuck   : " + testwurm.getIsStuck());
        System.out.println();
    }
    public static void main(String[] args){
        System.out.println("Var-Constructor-Test");
        System.out.println("-------------------------");
        Figure testwurmA = new Figure("Stig",100,36,false,false,true);
        printAllAttributes(testwurmA);

        System.out.println("Getter-Setter-Test");
        System.out.println("-------------------------");
        testwurmA.setName("The " + testwurmA.getName());
        testwurmA.setArmor(testwurmA.getArmor() - 8);
        testwurmA.setHealth(testwurmA.getHealth() - 50);
        testwurmA.setIsBurning(!testwurmA.getIsBurning());
        testwurmA.setIsPoisoned(!testwurmA.getIsPoisoned());
        testwurmA.setIsStuck(!testwurmA.getIsStuck());
        printAllAttributes(testwurmA);

        System.out.println("JSON-Constructor-IO-Test");
        System.out.println("-------------------------");
        System.out.println("JSON: "+testwurmA.toJson().toString());
        Figure testwurmB = new Figure(testwurmA.toJson());
        printAllAttributes(testwurmB);
    }
}
