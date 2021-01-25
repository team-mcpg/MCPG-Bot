package fr.milekat.MCPG_Discord.utils;

import fr.milekat.MCPG_Discord.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple SQL connection manager
 */
public class MariaManage {
    private final String url;
    private final String host;
    private final String database;
    private final String user;
    private final String pass;
    private Connection connection;

    public MariaManage(String url, String host, String database, String user, String pass) {
        this.url = url;
        this.host = host;
        this.database = database;
        this.user = user;
        this.pass = pass;
    }

    public void connection() {
        try {
            connection = DriverManager.getConnection(url + host + "/" + database + "?autoReconnect=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, pass);
            Main.log("SQL connecté !");
        } catch (SQLException exception) {
            Main.log("Erreur SQL.");
            exception.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            connection.close();
            Main.log("SQL déconnecté !");
        } catch (SQLException exception) {
            Main.log("Erreur SQL.");
            exception.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}