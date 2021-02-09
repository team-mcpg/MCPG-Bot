package fr.milekat.MCPG_Discord.classes;

import fr.milekat.MCPG_Discord.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TeamsManager {
    /**
     * Get a team
     */
    public static Team getTeam(Integer id) throws SQLException {
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement("SELECT * FROM `mcpg_team` WHERE `team_id` = ?;");
        q.setInt(1, id);
        q.execute();
        q.getResultSet().next();
        Team team = new Team(id,
                q.getResultSet().getString("name"),
                q.getResultSet().getLong("chief_id"),
                q.getResultSet().getInt("money"),
                PlayersManager.getTeamMembers(id));
        q.close();
        return team;
    }

    /**
     * Get the team of player by his discord id
     */
    public static Team getPlayerTeam(long id) throws SQLException {
        int team;
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement("SELECT team_id FROM `mcpg_player` WHERE `discord_id` = ?;");
        q.setLong(1, id);
        q.execute();
        q.getResultSet().next();
        team = q.getResultSet().getInt("team_id");
        q.close();
        return getTeam(team);
    }

    /**
     * Get the team of player by his username
     */
    public static Team getPlayerTeam(String username) throws SQLException {
        int team;
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement("SELECT `team_id` FROM `mcpg_player` WHERE `username` = ?;");
        q.setString(1, username);
        q.execute();
        q.getResultSet().next();
        team = q.getResultSet().getInt("team_id");
        q.close();
        return getTeam(team);
    }

    /**
     * Create a new team (May not exist)
     */
    public static Team createTeam(Team team) throws SQLException {
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement(
                "INSERT INTO `mcpg_team`(`name`, `chief_id`) VALUES (?, ?) RETURNING `team_id`;");
        q.setString(1, team.getName());
        q.setLong(2, team.getChief());
        q.execute();
        q.getResultSet().next();
        team.setId(q.getResultSet().getInt("team_id"));
        q.close();
        return team;
    }

    /**
     * Update the name of a team
     */
    public static void updateTeam(Team team) throws SQLException {
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement("UPDATE `mcpg_team` SET `name`= ? WHERE `team_id` = ?;");
        q.setString(1, team.getName());
        q.setInt(2, team.getId());
        q.execute();
        q.close();
    }

    /**
     * Set team for players (Update everything for theses players)
     */
    public static void setMembers(Team team) throws SQLException {
        for (Player member : team.getMembers()) {
            PlayersManager.updatePlayer(member);
        }
    }

    /**
     * Check if the name for the team is available
     */
    public static boolean exist(String teamname) throws SQLException {
        boolean exist;
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement("SELECT `name` FROM `mcpg_team` WHERE `name` = ?;");
        q.setString(1, teamname);
        q.execute();
        exist = q.getResultSet().next();
        q.close();
        return exist;
    }
}
