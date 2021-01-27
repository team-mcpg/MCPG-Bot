package fr.milekat.MCPG_Discord;

import fr.milekat.MCPG_Discord.classes.Player;
import fr.milekat.MCPG_Discord.classes.PlayersManager;
import fr.milekat.MCPG_Discord.classes.Team;
import fr.milekat.MCPG_Discord.classes.TeamsManager;
import fr.milekat.MCPG_Discord.core.Init;
import fr.milekat.MCPG_Discord.utils.DateMilekat;
import fr.milekat.MCPG_Discord.utils.MariaManage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.util.HashMap;

public class Main {
    /* Core */
    public static boolean debugExeptions = false;
    private static JSONObject configs;
    /* SQL */
    public static String SQLPREFIX = "BOT_";
    private static MariaManage mariaManage;
    /* Jedis */
    public static boolean debugJedis = true;
    public static Jedis jedis;
    /* Discord Bot */
    private static JDA jda;
    /* Data */
    public static HashMap<User, Player> players = new HashMap<>();
    public static HashMap<Integer, Team> teams = new HashMap<>();

    /**
     * Main method
     */
    public static void main(String[] args) throws Exception {
        Init init = new Init();
        configs = init.getConfigs();
        //  Load SQL + Lancement du ping
        mariaManage = init.setSQL();
        //  Load data from SQL
        new PlayersManager();
        new TeamsManager();
        //  Load Jedis + Sub Thread
        jedis = init.getJedis();
        //  Discord bot load
        jda = init.getJDA();
        //  Console load
        init.getConsole().start();
        //  Log
        log("Load du bot terminé.");
    }

    /**
     * Simple log with Date !
     *
     * @param log message to send
     */
    public static void log(String log) {
        System.out.println("[" + DateMilekat.setDateNow() + "] " + log);
    }

    /**
     * Send a message through Redis MessengerQueue
     *
     * @param msg message to send
     */
    public static void sendRedis(String msg) {
        Main.jedis.publish("discord", msg);
    }

    /**
     * "config.json" file
     */
    public static JSONObject getConfig() {
        return configs;
    }

    /**
     * SQL Connection to make queries
     */
    public static Connection getSql() {
        return mariaManage.getConnection();
    }

    /**
     * Discord BOT
     */
    public static JDA getJda() {
        return jda;
    }
}
