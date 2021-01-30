package fr.milekat.MCPG_Discord.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;

public class Chat extends ListenerAdapter {
    /* Main */
    private final BotManager manager;
    private final JDA api;
    private final JSONObject id;
    private final JSONObject msg;

    public Chat(BotManager manager, JDA api, JSONObject id, JSONObject msg) {
        this.manager = manager;
        this.api = api;
        this.id = id;
        this.msg = msg;
    }
}
