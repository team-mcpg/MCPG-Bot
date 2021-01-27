package fr.milekat.MCPG_Discord.classes;

import fr.milekat.MCPG_Discord.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PlayersManager {
    /**
     * Load Players from SQL
     */
    public PlayersManager() {
        try {
            Connection connection = Main.getSql();
            PreparedStatement q = connection.prepareStatement("SELECT * FROM `mcpg_player`;");
            q.execute();
            while (q.getResultSet().next()) {
                Main.getJda().retrieveUserById(q.getResultSet().getLong("discord_id")).queue(user ->
                {
                    try {
                        Main.players.put(user,
                                new Player(q.getResultSet().getString("username"),
                                        q.getResultSet().getString("prefix"),
                                        UUID.fromString(q.getResultSet().getString("uuid")),
                                        user,
                                        q.getResultSet().getInt("step"),
                                        q.getResultSet().getInt("team"),
                                        q.getResultSet().getString("muted"),
                                        q.getResultSet().getString("banned"),
                                        q.getResultSet().getString("reason")));
                    } catch (SQLException throwables) {
                        if (Main.debugJedis) throwables.printStackTrace();
                    }
                }, throwables -> { if (Main.debugJedis) throwables.printStackTrace(); });
            }
            q.close();
        } catch (SQLException throwables) {
            if (Main.debugJedis) throwables.printStackTrace();
        }
    }

    /**
     * Update a player in SQL
     */
    public void updatePlayer(Player player) {

    }
}
