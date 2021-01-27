package fr.milekat.MCPG_Discord.bot;

import fr.milekat.MCPG_Discord.Main;
import net.dv8tion.jda.api.JDA;
import org.json.simple.JSONObject;

public class BotManager {

    public BotManager () {
        // Event
        JDA api = Main.getJda();
        JSONObject id = (JSONObject) Main.getConfig().get("id");
        api.addEventListener(new Register(api, id));
        api.addEventListener(new Chat(api, id));
        api.addEventListener(new Ban(api, id));
    }
}
