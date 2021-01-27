package fr.milekat.MCPG_Discord.classes;

import java.util.ArrayList;

public class Team {
    private final int id;
    private final String name;
    private final int money;
    private ArrayList<String> members;

    public Team(int id, String name, int money, ArrayList<String> members) {
        this.id = id;
        this.name = name;
        this.money = money;
        this.members = members;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void addMembers(String member) {
        this.members.add(member);
    }

    public void removeMembers(String member) {
        this.members.remove(member);
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public int getSize() {
        return members.size();
    }
}
