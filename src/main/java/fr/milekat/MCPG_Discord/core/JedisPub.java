package fr.milekat.MCPG_Discord.core;

import fr.milekat.MCPG_Discord.Main;
import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;

public class JedisPub {
    /**
     *      Send message through Redis MQ
     */
    public static void sendRedisChat(String msg){
        new Thread(() -> {
            try {
                JSONObject redisConfig = (JSONObject) Main.getConfig().get("redis");
                JSONObject redisChannels = (JSONObject) ((JSONObject) redisConfig.get("out-channels")).get("mc");
                Jedis jedis = new Jedis((String) redisConfig.get("host"), 6379, 0);
                jedis.auth((String) redisConfig.get("auth"));
                jedis.publish((String) redisChannels.get("chat"), msg);
                jedis.quit();
            } catch (Exception throwable) {
                Main.log("Exception : " + throwable.getMessage());
            }
        }).start();
    }

    /**
     *      Log something through Redis MQ
     */
    public static void sendRedisLog(String msg){
        new Thread(() -> {
            try {
                JSONObject redisConfig = (JSONObject) Main.getConfig().get("redis");
                JSONObject redisChannels = (JSONObject) ((JSONObject) redisConfig.get("out-channels")).get("mc");
                Jedis jedis = new Jedis((String) redisConfig.get("host"), 6379, 0);
                jedis.auth((String) redisConfig.get("auth"));
                jedis.publish((String) redisChannels.get("log"), msg);
                jedis.quit();
            } catch (Exception throwable) {
                Main.log("Exception : " + throwable.getMessage());
            }
        }).start();
    }
}
