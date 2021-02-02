package fr.milekat.MCPG_Discord.core;

import fr.milekat.MCPG_Discord.Main;
import fr.milekat.MCPG_Discord.utils.MariaManage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;

import javax.security.auth.login.LoginException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Init {
    /**
     * Load the config file (config.json)
     */
    public JSONObject getConfigs() throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject configs = (JSONObject) jsonParser.parse(new FileReader("config.json"));
        Main.debug = (boolean) configs.get("debug");
        Main.debugJedis = (boolean) configs.get("debugjedis");
        return configs;
    }

    /**
     * SQL connection + SQL auto ping to prevent the connection to get disconnected
     */
    public MariaManage setSQL() {
        JSONObject sqlconfig = (JSONObject) Main.getConfig().get("sql");
        Main.SQLPREFIX = (String) sqlconfig.get("SQLPREFIX");
        //  Open SQL connection
        MariaManage mariaManage = new MariaManage("jdbc:mysql://",
                (String) sqlconfig.get("host"),
                (String) sqlconfig.get("db"),
                (String) sqlconfig.get("user"),
                (String) sqlconfig.get("mdp"));
        mariaManage.connection();
        //  Start SQL ping to keep alive SQL connection
        new Thread("SQL-keepalive") {
            @Override
            public void run() {
                Timer SQL_keepalive = new Timer();
                SQL_keepalive.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            PreparedStatement q = mariaManage.getConnection().prepareStatement("SELECT * FROM `ping`;");
                            q.execute();
                            q.close();
                        } catch (SQLException exception) {
                            exception.printStackTrace();
                        }
                    }
                }, 0, 600000);
            }
        };
        return mariaManage;
    }

    /**
     * Load console thread
     */
    public Thread getConsole() {
        return new Thread("Console") {
            @Override
            public void run() {
                new Console();
            }
        };
    }

    /**
     * Connect to Redis server and subscribe to all servers
     */
    public Jedis getJedis() {
        JSONObject redisconfig = (JSONObject) Main.getConfig().get("redis");
        Main.debugJedis = (boolean) redisconfig.get("debug");
        Jedis jedis = new Jedis((String) redisconfig.get("host"), 6379, 0);
        jedis.auth((String) redisconfig.get("auth"));
        JedisSub subscriber = new JedisSub();
        new Thread("Redis-Discord-Sub") {
            @Override
            public void run() {
                try {
                    Jedis jedis = new Jedis((String) redisconfig.get("host"), 6379, 0);
                    jedis.auth((String) redisconfig.get("auth"));
                    if (Main.debugJedis) Main.log("Load Jedis channels");
                    jedis.subscribe(subscriber, getJedisChannels());
                } catch (Exception e) {
                    Main.log("Subscribing failed." + e);
                    e.printStackTrace();
                }
            }
        };
        return jedis;
    }

    /**
     * Connect to the Discord bot and set the watching text
     */
    public JDA getJDA() throws LoginException, InterruptedException {
        JDA api = JDABuilder.createDefault((String) Main.getConfig().get("bot_token")).build().awaitReady();
        api.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching((String) Main.getConfig().get("bot_game")));
        return api;
    }

    /**
     * Get all channels to subscribe with SQL list
     */
    private String[] getJedisChannels() {
        try {
            Connection connection = Main.getSql();
            PreparedStatement q = connection.prepareStatement("SELECT * FROM `mcpg_redis_channels`;");
            q.execute();
            ArrayList<String> jedisChannels = new ArrayList<>();
            while (q.getResultSet().next()) jedisChannels.add(q.getResultSet().getString("channel"));
            return jedisChannels.toArray(new String[0]);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
}
