package fr.milekat.MCPG_Discord;

import fr.milekat.MCPG_Discord.bot.BotManager;
import fr.milekat.MCPG_Discord.core.Init;
import fr.milekat.MCPG_Discord.utils.DateMilekat;
import fr.milekat.MCPG_Discord.utils.MariaManage;
import net.dv8tion.jda.api.JDA;
import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;

import java.sql.Connection;

public class Main {
    /* Core */
    public static boolean debug = false;
    private static JSONObject configs;
    /* SQL */
    public static String SQLPREFIX = "BOT_";
    private static MariaManage mariaManage;
    /* Jedis */
    public static boolean debugJedis = true;
    public static Jedis jedis;
    /* Discord Bot */
    private static JDA jda;
    private static BotManager bot;

    /**
     * Main method
     */
    public static void main(String[] args) throws Exception {
        Init init = new Init();
        configs = init.getConfigs();
        //  Load SQL + Lancement du ping
        mariaManage = init.setSQL();
        //  Load Jedis + Sub Thread
        jedis = init.getJedis();
        //  Discord bot load
        jda = init.getJDA();
        bot = new BotManager();
        //  Console load
        init.getConsole().start();
        //  Log
        log("Debug: " + debug);
        log("Application ready.");
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

    /**
     * BOT Manager
     */
    public static BotManager getBot() {
        return bot;
    }
}
