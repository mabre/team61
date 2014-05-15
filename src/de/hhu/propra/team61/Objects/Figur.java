package de.hhu.propra.team61.Objects;

/**
 * Created by kevgny on 14.05.14.
 */

public class Figur {
    private String name;
    private int health;
    private int armor;

    private boolean isBurning;
    private boolean isPoisoned;
    private boolean isStuck;


    // In and Out
    public Figur(String name, int hp, int armor){
        this.name   = name;
        this.health = hp;
        this.armor  = armor;
    }
    public Figur(){ //from Json

    }

    public static void toJson(){

    }

    // Getter and Setter
    public String getName(){return name;}
    public void setName(String name){this.name = name;}

    public int  getHealth() {return health;}
    public void setHealth(int health) {this.health = health;}

    public int  getArmor() {return armor;}
    public void setArmor(int armor) {this.armor = armor;}

    public boolean getIsBurning() {return isBurning;}
    public void setIsBurning(boolean isBurning){this.isBurning = isBurning;}

    public boolean getIsPoisoned() {return isPoisoned;}
    public void setIsPoisoned(boolean isPoisoned){this.isPoisoned = isPoisoned;}

    public boolean getIsStuck() {return isStuck;}
    public void setIsStuck(boolean isStuck){this.isStuck = isStuck;}



    //For testing purposes only

    private static void printAllAttributes(Figur testwurm){
        System.out.println("Health  : " + testwurm.getHealth());
        System.out.println("Armor   : " + testwurm.getArmor());
        System.out.println("Name    : " + testwurm.getName());
        System.out.println("Burning : " + testwurm.getIsBurning());
        System.out.println("Poisoned: " + testwurm.getIsPoisoned());
        System.out.println("Stuck   : " + testwurm.getIsStuck());
        System.out.println();
    }

    public static void main(String[] args){
        //Constructor A test
        Figur testwurm = new Figur("Stig",100,36);
        printAllAttributes(testwurm);

        //Setter Test
        testwurm.setName("The Stig");
        testwurm.setArmor(8);
        testwurm.setHealth(20);
        printAllAttributes(testwurm);
    }
}
