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
import java.util.Objects;

public class TeamEvent extends ListenerAdapter {
    /* Main */
    private final BotManager manager;
    private final JDA api;
    private final JSONObject msg;
    /* Guilds */
    private final Guild gPublic;
    /* Roles */
    private final Role rValid;
    private final Role rTeam;

    public TeamEvent(BotManager manager, JDA api, JSONObject id, JSONObject msg) {
        this.manager = manager;
        this.api = api;
        this.msg = msg;
        this.gPublic = api.getGuildById((Long) id.get("gPublic"));
        this.rValid = api.getRoleById((Long) id.get("rValid"));
        this.rTeam = api.getRoleById((Long) id.get("rTeam"));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        try {
            Player player = PlayersManager.getPlayer(event.getAuthor().getIdLong());
            if (event.getMember() != null && event.getMember().getRoles().contains(rValid) &&
                    event.getMessage().getMentionedMembers().size() == 1) {
                if (event.getMessage().getContentRaw().startsWith("/info")) {
                    checkPlayerTeam(event.getMessage(), event.getMember());
                } else if (event.getMessage().getContentRaw().startsWith("/invite")) {
                    teamInvite(event.getMessage(), event.getMember());
                }
                event.getMessage().delete().queue();
            } else if (player.getStep().equals("ACCEPTED") && event.getMessage().getContentRaw().startsWith("/teamname")) {
                updateTeamName(event.getAuthor(), event.getMessage().getContentRaw());
            }
        } catch (SQLException ignore) {}
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        User user = event.getUser();
        event.retrieveMessage().queue(message -> {
            try {
                if (event.getChannel().getType().equals(ChannelType.PRIVATE) ) {
                    Player player = PlayersManager.getPlayer(event.getUserIdLong());
                    if (!player.getStep().equals("ACCEPTED")) return;
                    teamRequestReply(message, user, event.getReactionEmote().getEmoji());
                    message.delete().queue();
                }
            } catch (SQLException | IllegalStateException ignore) {/* Cannot get emoji code for custom emote reaction */}
        });
    }

    /**
     * Send to member, the team info if the targeted player is Valid
     */
    private void checkPlayerTeam(Message message, Member member) {
        Member mTarget = message.getMentionedMembers().get(0);
        if (mTarget.getRoles().contains(rValid)) {
            try {
                Team team = TeamsManager.getPlayerTeam(mTarget.getIdLong());
                manager.sendPrivate(member.getUser(), TeamsManager.getTeamEmbed(team).build());
            } catch (SQLException throwables) {
                manager.sendPrivate(member.getUser(), (String) msg.get("data_error"));
                if (Main.debug) {
                    Main.log("[" + member.getUser().getAsTag() + "] SQL error");
                    throwables.printStackTrace();
                }
            }
        } else {
            manager.sendPrivate(member.getUser(), (String) msg.get("request_target_not_approved"));
        }
    }

    /**
     * Invite a user to the team of player
     */
    private void teamInvite(Message message, Member member) {
        Member mTarget = message.getMentionedMembers().get(0);
        //  Prevent user to self invite
        if (member.getIdLong() == mTarget.getIdLong()) {
            manager.sendPrivate(member.getUser(), (String) msg.get("request_cant_self"));
            return;
        }
        //  If targed is valid
        if (!mTarget.getRoles().contains(rValid)) {
            manager.sendPrivate(member.getUser(), (String) msg.get("request_target_not_approved"));
            return;
        }
        //  If targed has already a team
        if (mTarget.getRoles().contains(rTeam)) {
            manager.sendPrivate(member.getUser(), (String) msg.get("request_target_already_in_team"));
            return;
        }
        Team senderTeam;
        Player sender;
        try {
            sender = PlayersManager.getPlayer(member.getIdLong());
            senderTeam = TeamsManager.getPlayerTeam(sender.getUsername());
        } catch (SQLException throwables) {
            manager.sendPrivate(member.getUser(), (String) msg.get("data_error"));
            if (Main.debug) throwables.printStackTrace();
            return;
        }
        //  Max team size, cancel request
        if (senderTeam.getSize() >= 6) {
            manager.sendPrivate(member.getUser(), (String) msg.get("request_team_full"));
            return;
        }
        //  Build request embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN)
                .setDescription(BotManager.setNick(member, (String) msg.get("request_sent_ask")))
                .setImage("https://crafatar.com/renders/body/" + sender.getUuid().toString() + "?size=512&overlay&default=MHF_Alex")
                .setFooter(member.getId());
        manager.sendPrivate(mTarget.getUser(), embed.build());
        manager.sendPrivate(member.getUser(), BotManager.setNick(mTarget, (String) msg.get("request_sent_confirm")));
    }

    /**
     * When a user reply to a team request (With ✅ or ❌)
     */
    private void teamRequestReply(Message message, User user, String emoji) {
        //  Green for team invitation ONLY
        if (!Objects.equals(message.getEmbeds().get(0).getColor(), Color.green)) return;
        if (emoji.equalsIgnoreCase("✅")) {
            teamAccept(user, message);
        } else if (emoji.equalsIgnoreCase("❌")) {
            //  User deny request, inform request sender
            api.retrieveUserById(message.getEmbeds().get(0).getFooter().getText()).queue(uTarget ->
                    gPublic.retrieveMember(user).queue(member ->
                            manager.sendPrivate(uTarget, BotManager.setNick(member, (String) msg.get("request_reply_deny")))));
        }
        message.delete().queue();
    }

    /**
     * Check the validation state, then make player join the team
     */
    private void teamAccept(User uTarget, Message message) {
        try {
            //  Init vars
            Team team = TeamsManager.getPlayerTeam(Long.parseLong(message.getEmbeds().get(0).getFooter().getText()));
            Player pTarget = PlayersManager.getPlayer(uTarget.getIdLong());
            gPublic.retrieveMember(uTarget).queue(mTarget -> api.retrieveUserById(team.getChief()).queue(uSender -> {
                try {
                    //  Max team size, cancel action
                    if (team.getSize() >= 6) {
                        manager.sendPrivate(uTarget, (String) msg.get("request_team_full"));
                        manager.sendPrivate(uSender, (String) msg.get("request_team_full"));
                        return;
                    }
                    if (team.getSize() == 1) {
                        manager.sendPrivate(uSender,
                                ((String) msg.get("request_team_created")).replaceAll("<team_name>", team.getName()));
                    }
                    //  Add team role to sender if he didn't have it already
                    gPublic.addRoleToMember(uSender.getIdLong(), rTeam).queue();
                    //  Add player to team
                    gPublic.addRoleToMember(mTarget, rTeam).queue();
                    pTarget.setTeam(team.getId());
                    PlayersManager.updatePlayer(pTarget);
                    //  Send messages
                    for (Player player : team.getMembers()) {
                        api.retrieveUserById(player.getDiscord_id()).queue(uTeamMember -> manager.sendPrivate(uTeamMember,
                                ((String) msg.get("request_reply_validation")).replaceAll("<team_name>", team.getName())));
                    }
                    manager.sendPrivate(uTarget,
                            ((String) msg.get("request_reply_confirm")).replaceAll("<team_name>", team.getName()));
                } catch (SQLException throwables) {
                    manager.sendPrivate(uTarget, (String) msg.get("data_error"));
                    if (Main.debug) throwables.printStackTrace();
                }
            }));
        } catch (SQLException throwables) {
            manager.sendPrivate(uTarget, (String) msg.get("data_error"));
            if (Main.debug) throwables.printStackTrace();
        }
    }

    /**
     * Command /teamname, to rename a team
     */
    private void updateTeamName(User user, String message) {
        if (message.length() > 30) {
            manager.sendPrivate(user, (String) msg.get("team_too_long_name"));
        }
        try {
            Team team = TeamsManager.getPlayerTeam(user.getIdLong());
            if (team.getSize() > 1 && team.getChief()==user.getIdLong()) {
                team.setName(message.replaceAll("/teamname ", ""));
                TeamsManager.updateTeam(team);
                for (Player teamMember : team.getMembers()) {
                    api.openPrivateChannelById(teamMember.getDiscord_id()).queue(channel -> channel.sendMessage(
                            ((String) msg.get("team_renamed")).replaceAll("<team_name>", team.getName())).queue());
                }
            }
        } catch (SQLException throwables) {
            manager.sendPrivate(user, (String) msg.get("data_error"));
            if (Main.debug) throwables.printStackTrace();
        }
    }
}
