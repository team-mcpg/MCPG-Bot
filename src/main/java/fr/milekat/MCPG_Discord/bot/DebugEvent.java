package fr.milekat.MCPG_Discord.bot;

import fr.milekat.MCPG_Discord.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

public class DebugEvent extends ListenerAdapter {
    /* Public Channels */
    private final TextChannel cRegister;
    private final TextChannel cTeamSearch;
    private final TextChannel cCandid;

    public DebugEvent(JDA api, JSONObject id) {
        this.cRegister = api.getTextChannelById((Long) id.get("cRegister"));
        this.cTeamSearch = api.getTextChannelById((Long) id.get("cTeamSearch"));
        this.cCandid = api.getTextChannelById((Long) id.get("cCandid"));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getChannel().getType().equals(ChannelType.PRIVATE)) {
            if (Main.DEBUG_ERROR) Main.log("[" + event.getAuthor().getAsTag() + "] Private msg: " + event.getMessage().getContentRaw());
        } else if (event.getTextChannel().equals(cRegister) || event.getTextChannel().equals(cTeamSearch)) {
            if (Main.DEBUG_ERROR) Main.log("[" + event.getAuthor().getAsTag() + "] Send msg: " + event.getMessage().getContentRaw() +
                    " in: " + event.getChannel().getName());
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        if (event.getChannel().getType().equals(ChannelType.PRIVATE)) {
            if (Main.DEBUG_ERROR) Main.log("[" + event.getUser().getAsTag() + "] Private reaction: " + event.getReactionEmote().getEmoji());
        } else if (event.getTextChannel().equals(cRegister) || event.getTextChannel().equals(cTeamSearch) ||
                event.getTextChannel().equals(cCandid)) {
            if (Main.DEBUG_ERROR) Main.log("[" + event.getUser().getAsTag() + "] Add reaction: " + event.getReactionEmote().getEmoji() +
                    " in: " + event.getChannel().getName());
        }
    }
}
