package fr.milekat.MCPG_Discord.bot;

import fr.milekat.MCPG_Discord.Main;
import fr.milekat.MCPG_Discord.classes.Step;
import fr.milekat.MCPG_Discord.classes.StepManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class BotManager {
    private final JDA api;
    private static JSONObject id;
    private static JSONObject msg;
    public static HashMap<String, Step> steps;

    public BotManager () {
        // Event
        api = Main.getJda();
        id = (JSONObject) Main.getConfig().get("id");
        msg = (JSONObject) Main.getConfig().get("msg");
        api.addEventListener(new Register(this, api, id, msg));
        api.addEventListener(new Chat(this, api, id, msg));
        api.addEventListener(new Ban(this, api, id, msg));
        steps = StepManager.getSteps(Main.getConfig());
    }

    /**
     * Replace "@mention" with mentioned user
     */
    public static String msgMention(User user, String message) {
        return message.replaceAll("@mention", user.getAsMention());
    }

    /**
     * Replace "<pseudo>" with nickname of member
     */
    public static String msgUsername(Member member, String message) {
        return message.replaceAll("<pseudo>", member.getNickname()==null ? member.getEffectiveName() : member.getNickname());
    }

    /**
     * Method to send an embed in channel with ✅/❌
     */
    public void sendEmbed(MessageChannel channel, MessageEmbed embed) {
        channel.sendMessage(embed).queue(message ->
                message.addReaction("✅").queue(reaction ->
                        message.addReaction("❌").queue()));
    }

    /**
     * Method to send an embed to user with ✅/❌
     */
    public void sendPrivate(User user, MessageEmbed embed) {
        user.openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage(embed).queue(message ->
                                message.addReaction("✅").queue(reaction ->
                                        message.addReaction("❌").queue())),
                throwable -> cantSendPrivate(user)
        );
    }

    /**
     * Method to send a simple private message to user
     */
    public void sendPrivate(User user, String message) {
        user.openPrivateChannel().queue(
                privateChannel -> privateChannel.sendMessage(message).queue(),
                throwable -> cantSendPrivate(user)
        );
    }

    /**
     * If the bot got an issue when sending a private message to a User
     */
    private void cantSendPrivate(User user) {
        TextChannel channel = api.getTextChannelById((long) id.get(""));
        if (channel!=null) channel.sendMessage(msgMention(user, (String) msg.get("cantmp"))).queue();
    }

    /**
     * Recharge les messages du bot depuis le fichier config.json
     */
    public static void reloadMsg() {
        try {
            JSONParser jsonParser = new JSONParser();
            msg = (JSONObject) ((JSONObject) jsonParser.parse(new FileReader("config.json"))).get("msg");
            steps = StepManager.getSteps(msg);
        } catch (IOException | ParseException exception) {
            Main.log("config.json not found");
            if (Main.debugExeptions) exception.printStackTrace();
        }
    }

    /**
     * Recharge les id de channels depuis le fichier config.json
     */
    public static void reloadCh() {
        try {
            JSONParser jsonParser = new JSONParser();
            id = (JSONObject) ((JSONObject) jsonParser.parse(new FileReader("config.json"))).get("id");
        } catch (IOException | ParseException exception) {
            Main.log("config.json not found");
            if (Main.debugExeptions) exception.printStackTrace();
        }
    }
}
