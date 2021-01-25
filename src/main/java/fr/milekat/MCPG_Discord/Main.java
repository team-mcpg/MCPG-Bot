package fr.milekat.MCPG_Discord;

import fr.milekat.MCPG_Discord.core.Init;
import fr.milekat.MCPG_Discord.utils.DateMilekat;
import fr.milekat.MCPG_Discord.utils.MariaManage;
import net.dv8tion.jda.api.JDA;
import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;

import java.sql.Connection;

public class Main {
    public static boolean debugExeptions = false;
    /* SQL */
    public static String SQLPREFIX = "BOT_";
    /* Jedis */
    public static boolean debugJedis = true;
    public static Jedis jedis;
    /* Core */
    private static JSONObject configs;
    private static JDA jda;
    private static MariaManage mariaManage;

    public static void main(String[] args) throws Exception {
        Init init = new Init();
        configs = init.getConfigs();
        //  Load SQL + Lancement du ping
        mariaManage = init.setSQL();
        //  Load Jedis + Sub Thread
        init.getJedis().start();
        //  Discord bot load

        // Console load
        init.getConsole().start();
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
    public static Connection getMariaManage() {
        return mariaManage.getConnection();
    }

    /**
     * Discord BOT
     */
    public static JDA getJda() {
        return jda;
    }
}
