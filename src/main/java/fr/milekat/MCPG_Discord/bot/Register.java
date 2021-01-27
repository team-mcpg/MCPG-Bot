package fr.milekat.MCPG_Discord.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

public class Register extends ListenerAdapter {
    /* Main */
    private final JDA api;
    private final JSONObject id;
    /* Guilds */
    private final Guild gStaff;
    private final Guild gPublic;
    /* Roles */
    private final Role rValide;

    /* Staff Channels */
    private final TextChannel cCandid;
    private final TextChannel cAccept;
    private final TextChannel cDeny;
    /* Public Channels */
    private final TextChannel cRegister;
    private Message regMsg;
    private final TextChannel cTeamSearch;

    public Register(JDA api, JSONObject id) {
        this.api = api;
        this.id = id;
        this.gStaff = api.getGuildById((String) id.get("gStaff"));
        this.gPublic = api.getGuildById((String) id.get("gPublic"));
        this.rValide = api.getRoleById((String) id.get("rValide"));
        this.cRegister = api.getTextChannelById((String) id.get("cRegister"));
        this.cCandid = api.getTextChannelById((String) id.get("cCandid"));
        if (cCandid != null) cCandid.retrieveMessageById((long) id.get("regMsg")).queue(message -> this.regMsg = message);
        this.cAccept = api.getTextChannelById((String) id.get("cAccept"));
        this.cDeny = api.getTextChannelById((String) id.get("cDeny"));
        this.cTeamSearch = api.getTextChannelById((String) id.get("cTeamSearch"));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getTextChannel().equals(cTeamSearch)) {

        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getChannel().getType().equals(ChannelType.TEXT) && event.getMember() != null) {
            if (event.getTextChannel().equals(cRegister)) {
                if (event.getReactionEmote().getName().equalsIgnoreCase(":white_check_mark:")) {
                    startCandid(event.getMember());
                }
            } else if (event.getTextChannel().equals(cCandid)) {

            } else if (event.getTextChannel().equals(cAccept)) {

            } else if (event.getTextChannel().equals(cDeny)) {

            }
        } else if (event.getChannel().getType().equals(ChannelType.PRIVATE)) {

        }
    }

    /**
     * Call when a member accept rules
     */
    private void startCandid(Member member) {
        member.getUser();
    }
}
