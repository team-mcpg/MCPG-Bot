package fr.milekat.MCPG_Discord.core;

import fr.milekat.MCPG_Discord.Main;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.simple.JSONObject;
import redis.clients.jedis.JedisPubSub;

import java.sql.SQLException;
import java.util.Date;

public class JedisSub extends JedisPubSub {
    private final JSONObject redisConfig;
    private final JSONObject redisChannels;
    private final JSONObject discordChannels;

    public JedisSub() {
        this.redisConfig = (JSONObject) Main.getConfig().get("redis");
        this.redisChannels = (JSONObject) ((JSONObject) redisConfig.get("in-channels")).get("mc");
        this.discordChannels = (JSONObject) Main.getConfig().get("id");
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equalsIgnoreCase((String) redisConfig.get("this_channel"))) {
            if (Main.DEBUG_JEDIS) Main.log("SUB:{" + channel + "},MSG:{" + message + "}");
            if (channel.equalsIgnoreCase((String) redisChannels.get("chat"))) {
                newChat(message);
            } else if (channel.equalsIgnoreCase((String) redisChannels.get("log"))) {
                String[] msg = message.split("#:#");
                try {
                    if (msg.length==5) new Moderation().newSanction(msg[0],msg[1],msg[2],msg[3],msg[4]);
                } catch (SQLException throwable) {
                    Main.log("Error SQL with sanction.");
                    throwable.printStackTrace();
                }
            }
        } else {
            if (Main.DEBUG_JEDIS) Main.log("PUB:{" + message + "}");
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

    /**
     * Send @message in Chat channel
     */
    private void newChat(String message) {
        TextChannel chatChannel;
        if (new Date().getTime() < Main.DATE_OPEN.getTime()) {
            chatChannel = Main.getJda().getTextChannelById((Long) discordChannels.get("cChatFake"));
        } else {
            chatChannel = Main.getJda().getTextChannelById((Long) discordChannels.get("cChat"));
        }
        assert chatChannel != null;
        chatChannel.sendMessage(message).queue();
    }
}