package fr.milekat.MCPG_Discord.bot;

import fr.milekat.MCPG_Discord.classes.Player;
import fr.milekat.MCPG_Discord.classes.PlayersManager;
import fr.milekat.MCPG_Discord.core.JedisPub;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.sql.SQLException;

public class McChat extends ListenerAdapter {
    /* Roles */
    private final Role rValid;
    /* Public Channels */
    private final TextChannel cChat;
    private final TextChannel cChatFake;

    public McChat(JDA api, JSONObject id) {
        /* Main */
        this.rValid = api.getRoleById((Long) id.get("rValid"));
        this.cChat = api.getTextChannelById((Long) id.get("cChat"));
        this.cChatFake = api.getTextChannelById((Long) id.get("cChatFake"));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!(event.getChannel().equals(cChatFake) || event.getChannel().equals(cChat))) return;
        if (event.getChannel().equals(cChat) && event.getMember()!=null && !event.getMember().getRoles().contains(rValid)) return;
        try {
            Player player = PlayersManager.getPlayer(event.getAuthor().getIdLong());
            if (player.isMute()) return;
            JedisPub.sendRedisChat(player.getUuid().toString() + "|" + event.getMessage().getContentRaw());
            event.getMessage().delete().queue();
        } catch (SQLException ignore) {}
    }
}
