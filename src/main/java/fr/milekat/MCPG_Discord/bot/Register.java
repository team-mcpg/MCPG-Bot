package fr.milekat.MCPG_Discord.bot;

import fr.milekat.MCPG_Discord.Main;
import fr.milekat.MCPG_Discord.classes.Player;
import fr.milekat.MCPG_Discord.classes.PlayersManager;
import fr.milekat.MCPG_Discord.classes.Team;
import fr.milekat.MCPG_Discord.classes.TeamsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.awt.*;
import java.sql.SQLException;

public class Register extends ListenerAdapter {
    /* Main */
    private final BotManager manager;
    private final JDA api;
    private final JSONObject id;
    private final JSONObject msg;
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

    public Register(BotManager manager, JDA api, JSONObject id, JSONObject msg) {
        this.manager = manager;
        this.api = api;
        this.id = id;
        this.msg = msg;
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
        if (event.getChannel().getType().equals(ChannelType.TEXT) && event.getMember() != null) {
            if (event.getTextChannel().equals(cTeamSearch)) {
                if (event.getMessage().getContentRaw().contains("/invite ") && event.getMessage().getMentionedMembers().size() != 0) {
                    requestSend(event);
                    event.getMessage().delete().queue();
                }
            }
        } else if (event.getChannel().getType().equals(ChannelType.PRIVATE)) {
            // TODO: 30/01/2021  Commade /teamname <NewName>
            // TODO: 30/01/2021  Messages pour la candidature si step = X
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        event.retrieveMessage().queue(message -> {
            if (event.getChannel().getType().equals(ChannelType.TEXT) && event.getMember() != null) {
                if (event.getTextChannel().equals(cRegister)) {
                    if (event.getReactionEmote().getName().equalsIgnoreCase(":white_check_mark:")) {
                        startCandid(event);
                    }
                } else if (event.getTextChannel().equals(cCandid)) {
                    // TODO: 30/01/2021  Réaction sur regMsg, pour lancer le processus de candidature
                } else if (event.getTextChannel().equals(cAccept)) {
                    // TODO: 30/01/2021  Candidature acceptée par le staff
                } else if (event.getTextChannel().equals(cDeny)) {
                    // TODO: 30/01/2021  Candidature refusée par le staff
                }
            } else if (event.getChannel().getType().equals(ChannelType.PRIVATE)) {
                if (!message.getAuthor().isBot() || message.getEmbeds().size() != 1) return;
                if (event.getReactionEmote().getEmoji().equalsIgnoreCase("✅")) {
                    requestValidation(event, message);
                } else if (event.getReactionEmote().getEmoji().equalsIgnoreCase("❌")) {
                    //  User deny request, inform request sender
                    manager.sendPrivate(api.getUserById(message.getEmbeds().get(0).getFooter().getText()),
                            BotManager.msgUsername(gPublic.getMember(event.getUser()), (String) msg.get("request_reply_deny")));
                }
                message.delete().queue();
            }
        });
    }

    /**
     * Call when a member accept rules
     */
    private void startCandid(MessageReactionAddEvent event) {
        // TODO: 30/01/2021 procéssus de candidature
    }

    /**
     * Invite a user to the team of player
     */
    private void requestSend(MessageReceivedEvent event) {
        //  Init vars
        Team senderTeam;
        Player sender;
        Player pTarget;
        Member mTarget = event.getMessage().getMentionedMembers().get(0);
        try {
            sender = PlayersManager.getPlayer(event.getAuthor().getId());
            senderTeam = TeamsManager.getPlayerTeam(sender.getUsername());
            pTarget = PlayersManager.getPlayer(mTarget.getId());
        } catch (SQLException throwables) {
            manager.sendPrivate(event.getAuthor(), (String) msg.get("data error"));
            if (Main.debugExeptions) throwables.printStackTrace();
            return;
        }
        //  Max team size, cancel request
        if (senderTeam.getSize() >= 6) {
            manager.sendPrivate(event.getAuthor(), (String) msg.get("team full"));
            return;
        }
        //  If targed is valid
        if (pTarget.getStep() <= 5) {
            manager.sendPrivate(event.getAuthor(), (String) msg.get("not approved"));
            return;
        }
        //  Build request embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN)
                .setDescription(BotManager.msgUsername(event.getMember(), (String) msg.get("request_sent_ask")))
                .setImage("https://crafatar.com/renders/body/" + sender.getUuid().toString() + "?size=512&overlay&default=MHF_Alex")
                .setFooter(event.getAuthor().getId());
        manager.sendPrivate(mTarget.getUser(), embed.build());
        manager.sendPrivate(event.getAuthor(), BotManager.msgUsername(mTarget, (String) msg.get("request_sent_confirm")));
    }

    /**
     * Check the validation state, then make player join the team
     */
    private void requestValidation(MessageReactionAddEvent event, Message message) {
        User uTarget = event.getUser();
        try {
            //  Init vars
            Team team = TeamsManager.getPlayerTeam(message.getEmbeds().get(0).getFooter().getText());
            Player pTarget = PlayersManager.getPlayer(event.getUserIdLong());
            User uSender = api.getUserById(team.getChief());
            Member mTarget = gPublic.getMember(uTarget);
            //  Max team size, cancel action
            if (team.getSize() >= 6) {
                manager.sendPrivate(uTarget, (String) msg.get("team full"));
                manager.sendPrivate(uSender, (String) msg.get("team full"));
                return;
            }
            if (team.getSize() == 1) {
                manager.sendPrivate(uSender, ((String) msg.get("team growth")).replaceAll("<nom_équipe>", team.getName()));
            }
            //  Add player to team
            pTarget.setTeam(team.getId());
            team.addMembers(pTarget);
            PlayersManager.updatePlayer(pTarget);
            //  Send messages
            manager.sendPrivate(uSender, BotManager.msgUsername(mTarget, (String) msg.get("request_reply_validation")));
            manager.sendPrivate(uTarget, ((String) msg.get("request_reply_confirm")).replaceAll("<nom_équipe>", team.getName()));
        } catch (SQLException throwables) {
            manager.sendPrivate(uTarget, (String) msg.get("data error"));
            if (Main.debugExeptions) throwables.printStackTrace();
        }
    }
}
