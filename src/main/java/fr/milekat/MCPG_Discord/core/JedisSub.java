package fr.milekat.MCPG_Discord.core;

import fr.milekat.MCPG_Discord.Main;
import org.json.simple.JSONObject;
import redis.clients.jedis.JedisPubSub;

public class JedisSub extends JedisPubSub {
    private final JSONObject redisConfig;

    public JedisSub() {
        this.redisConfig = (JSONObject) Main.getConfig().get("redis");
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equalsIgnoreCase((String) redisConfig.get("this_channel"))) {
            String[] msg = message.split("#:#");
            if (Main.debugJedis) Main.log("SUB:{" + channel + "},MSG:{" + message + "}");
            //  Jedis actions
        } else {
            if (Main.debugJedis) Main.log("PUB:{" + message + "}");
        }
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        Main.log("Redis connecté à " + channel);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        Main.log("Redis déconnecté de " + channel);
    }
}