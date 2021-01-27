package fr.milekat.MCPG_Discord.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;

public class Ban extends ListenerAdapter {
    /* Main */
    private final JDA api;
    private final JSONObject id;

    public Ban(JDA api, JSONObject id) {
        this.api = api;
        this.id = id;
    }
}
