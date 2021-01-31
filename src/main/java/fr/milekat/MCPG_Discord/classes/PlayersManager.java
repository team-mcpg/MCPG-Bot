package fr.milekat.MCPG_Discord.classes;

import fr.milekat.MCPG_Discord.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class PlayersManager {
    /**
     * Get a player by his discord id
     */
    public static Player getPlayer(Long id) throws SQLException {
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement("SELECT * FROM `mcpg_player` WHERE `discord_id` = ?;");
        q.setLong(1, id);
        q.execute();
        q.getResultSet().last();
        Player player = getFromSQL(q);
        q.close();
        return player;
    }

    /**
     * Get a player by his username
     */
    public static Player getPlayer(String username) throws SQLException {
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement("SELECT * FROM `mcpg_player` WHERE `username` = ?;");
        q.setString(1, username);
        q.execute();
        q.getResultSet().last();
        Player player = getFromSQL(q);
        q.close();
        return player;
    }

    /**
     * Get all current team members
     */
    public static ArrayList<Player> getTeamMembers(Integer id) throws SQLException {
        ArrayList<Player> members = new ArrayList<>();
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement("SELECT * FROM `mcpg_player` WHERE `team_id` = ?;");
        q.setInt(1, id);
        q.execute();
        while (q.getResultSet().next()) {
            members.add(getFromSQL(q));
        }
        q.close();
        return members;
    }

    /**
     * Create a new player
     */
    public static void createPlayer(long discord_id) throws SQLException {
        Player player = new Player(discord_id, "init");
        updatePlayer(player);
    }

    /**
     * Full update or create a player profile in SQL
     */
    public static void updatePlayer(Player player) throws SQLException {
        Connection connection = Main.getSql();
        PreparedStatement q = connection.prepareStatement("INSERT INTO `mcpg_player`(`discord_id`, `step`) VALUES (?,?) ON DUPLICATE KEY UPDATE `mcpg_player` SET `username`=?,`uuid`=?,`discord_id`=?,`step`=?,`register`=?,`team_id`=?,`muted`=?,`banned`=?,`reason`=?");
        q.setString(1, player.getUsername());
        q.setLong(2, player.getDiscord_id());
        q.setString(3, player.getStep());
        q.setString(4, player.getStringRegister());
        q.setInt(5, player.getTeam());
        q.setString(6, player.getMuted());
        q.setString(7, player.getBanned());
        q.setString(8, player.getReason());
        q.setString(9, player.getUuid().toString());
        q.execute();
        q.close();
    }

    /**
     * Set Player from SQL ResultSet
     */
    private static Player getFromSQL(PreparedStatement q) throws SQLException {

        return new Player(q.getResultSet().getString("username"),
                q.getResultSet().getString("prefix"),
                UUID.fromString(q.getResultSet().getString("uuid")),
                q.getResultSet().getLong("discord_id"),
                q.getResultSet().getString("step"),
                q.getResultSet().getString("register"),
                q.getResultSet().getInt("team"),
                q.getResultSet().getString("muted"),
                q.getResultSet().getString("banned"),
                q.getResultSet().getString("reason"));
    }
}
