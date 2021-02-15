package fr.milekat.MCPG_Discord.classes;

import java.util.ArrayList;

public class Team {
    private int id;
    private String name;
    private final long chief;
    private final int money;
    private ArrayList<Player> members;

    public Team(String name, long chief) {
        this.name = name;
        this.chief = chief;
        this.money = 0;
        this.members = null;
    }

    public Team(int id, String name, long chief, int money, ArrayList<Player> members) {
        this.id = id;
        this.name = name;
        this.chief = chief;
        this.money = money;
        this.members = members;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getChief() {
        return chief;
    }

    public int getMoney() {
        return money;
    }

    public ArrayList<Player> getMembers() {
        return members;
    }

    public void addMembers(Player member) {
        this.members.add(member);
    }

    public void removeMembers(Player member) {
        this.members.remove(member);
    }

    public void setMembers(ArrayList<Player> members) {
        this.members = members;
    }

    public int getSize() {
        return members.size();
    }
}
