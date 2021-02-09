package fr.milekat.MCPG_Discord.bot;

import fr.milekat.MCPG_Discord.Main;
import fr.milekat.MCPG_Discord.classes.*;
import fr.milekat.MCPG_Discord.utils.MojangNames;
import fr.milekat.MCPG_Discord.utils.Numbers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.*;

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
    private final Role rWaiting;
    private final Role rValid;
    private final Role rTeam;
    private final Role rAdmin;
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
        this.gStaff = api.getGuildById((Long) id.get("gStaff"));
        this.gPublic = api.getGuildById((Long) id.get("gPublic"));
        this.rWaiting = api.getRoleById((Long) id.get("rWaiting"));
        this.rValid = api.getRoleById((Long) id.get("rValid"));
        this.rTeam = api.getRoleById((Long) id.get("rTeam"));
        this.rAdmin = api.getRoleById((Long) id.get("rAdmin"));
        this.cRegister = api.getTextChannelById((Long) id.get("cRegister"));
        this.cCandid = api.getTextChannelById((Long) id.get("cCandid"));
        this.cAccept = api.getTextChannelById((Long) id.get("cAccept"));
        this.cDeny = api.getTextChannelById((Long) id.get("cDeny"));
        this.cTeamSearch = api.getTextChannelById((Long) id.get("cTeamSearch"));
        loadSqlStepsForm();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getChannel().getType().equals(ChannelType.TEXT) && event.getMember() != null) {
            if (event.getTextChannel().equals(cTeamSearch)) {
                if (Main.debug) Main.log("[" + event.getAuthor().getAsTag() + "] New msg: " + event.getMessage().getContentRaw() +
                        " in: " + event.getChannel().getName());
                if (event.getMessage().getContentRaw().contains("/invite ") && event.getMessage().getMentionedMembers().size() != 0) {
                    teamInvite(event);
                    event.getMessage().delete().queue();
                }
            }
        } else if (event.getChannel().getType().equals(ChannelType.PRIVATE)) {
            if (Main.debug) Main.log("[" + event.getAuthor().getAsTag() + "] Private send: " + event.getMessage().getContentRaw());
            privateReceive(event.getMessage(), event.getAuthor(), event.getPrivateChannel(), null);
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        User user = event.getUser();
        String emoji = event.getReactionEmote().getEmoji();
        event.retrieveMessage().queue(message -> {
            if (event.getChannel().getType().equals(ChannelType.TEXT) && event.getMember() != null) {
                if (event.getTextChannel().equals(cRegister) && !event.getMember().getRoles().contains(rWaiting)) {
                    if (Main.debug) Main.log("[" + user.getAsTag() + "] Add reaction in: " + event.getChannel().getName());
                    formStart(user);
                } else if (event.getTextChannel().equals(cCandid) && (emoji.equals("✅") || emoji.equals("❌"))) {
                    if (Main.debug) Main.log("[" + user.getAsTag() + "] Add reaction in: " + event.getChannel().getName());
                    event.getReaction().retrieveUsers().queue(users -> gStaff.retrieveMembers(users).onSuccess(members ->
                            formStaffCheckValidate(members, event.getReaction().getReactionEmote().getEmoji(), message)));
                }
            } else if (event.getChannel().getType().equals(ChannelType.PRIVATE) && event.getUser() != null &&
                    message.getAuthor().isBot() && message.getEmbeds().size() == 1) {
                if (Main.debug) Main.log("[" + user.getAsTag() + "] Private reaction");
                privateReceive(message, user, event.getPrivateChannel(), emoji);
            }
        });
    }

    /**
     * Call when a member accept rules
     */
    private void formStart(User user) {
        try {
            Player player = PlayersManager.createPlayer(user.getIdLong());
            Step step = BotManager.steps.get(player.getStep());
            manager.sendPrivate(user, BotManager.setNick(user, step.getMessage()));
            player.setStep(step.getNext());
            PlayersManager.updatePlayer(player);
            user.openPrivateChannel().queue(channel -> privateSend(user, player, channel));
        } catch (SQLException throwables) {
            manager.sendPrivate(user, (String) msg.get("data_error"));
            if (Main.debug) {
                Main.log("[" + user.getAsTag() + "] SQL error");
                throwables.printStackTrace();
            }
        }
    }

    /**
     * Execute actions for the current step of user
     */
    private void privateReceive(Message message, User user, PrivateChannel channel, String emoji) {
        Player player = null;
        try {
            player = PlayersManager.getPlayer(user.getIdLong());
        } catch (SQLException ignore) {
            if (Main.debug) Main.log("[" + user.getAsTag() + "] Unregistered user");
        }
        try {
            if (player == null || player.getDiscord_id() != user.getIdLong()) return;
            if (BotManager.steps.containsKey(player.getStep())) {
                //  Player is in form register
                Step step = BotManager.steps.get(player.getStep());
                switch (step.getType()) {
                    case "TEXT": {
                        if (step.getMin() < message.getContentRaw().length() && step.getMax() > message.getContentRaw().length()) {
                            if (step.getName().equals("Pseudo Mc")) { //  Username exception
                                try {
                                    player.setUsername(message.getContentRaw());
                                    player.setUuid(UUID.fromString(MojangNames.getUuid(message.getContentRaw())));
                                } catch (IOException ignored) {
                                    manager.sendPrivate(user, "Mojang data error please retry.");
                                    if (Main.debug) Main.log("[" + user.getAsTag() + "] Mojang data error, retry..");
                                    privateSend(user, player, channel);
                                    return;
                                }
                            }
                            player.setStep(step.getNext());
                            if (step.isSave()) {
                                LinkedHashMap<String, String> register = player.getRegister();
                                register.put(step.getName(), message.getContentRaw().replaceAll("\\|", ""));
                                player.setRegister(register);
                            }
                        }
                        break;
                    }
                    case "VALID": {
                        if (emoji.equalsIgnoreCase("✅")) {
                            manager.sendPrivate(user, step.getYes());
                            player.setStep(step.getNext());
                            if (step.isSave()) {
                                LinkedHashMap<String, String> register = player.getRegister();
                                register.put(step.getName(), "yes");
                                player.setRegister(register);
                            }
                        } else if (emoji.equalsIgnoreCase("❌")) {
                            manager.sendPrivate(user, step.getNo());
                            player.setStep(step.getBack());
                        }
                        break;
                    }
                    case "CHOICES": {
                        String choice = step.getChoices().get(Numbers.getInt(emoji) - 1);
                        if (choice == null) break;
                        player.setStep(step.getNext());
                        if (step.isSave()) {
                            LinkedHashMap<String, String> register = player.getRegister();
                            register.put(step.getName(), choice);
                            player.setRegister(register);
                        }
                        break;
                    }
                    case "END": {
                        if (emoji.equalsIgnoreCase("✅")) {
                            manager.sendEmbed(cCandid, getFINALEmbed(user, player).build());
                            manager.sendPrivate(user, step.getYes());
                            player.setStep("WAITING");
                            PlayersManager.updatePlayer(player);
                            gPublic.retrieveMember(user).queue(member -> gPublic.addRoleToMember(member, rWaiting).queue());
                            channel.close().queue();
                            return; //  End of from !
                        } else if (emoji.equalsIgnoreCase("❌")) {
                            manager.sendPrivate(user, step.getNo());
                            player.setStep(step.getBack());
                        }
                        break;
                    }
                }
                try {
                    PlayersManager.updatePlayer(player);
                } catch (SQLIntegrityConstraintViolationException throwables) {
                    manager.sendPrivate(user, (String) msg.get("register_already_exist"));
                    player.setStep(step.getName());
                    player.setUuid(null);
                    player.setUsername(null);
                    LinkedHashMap<String, String> save = player.getRegister();
                    save.remove(step.getName());
                    player.setRegister(save);
                }
                privateSend(user, player, channel);
            } else if (player.getStep().equals("ACCEPTED")) {
                //  Green for team invitation ONLY
                if (!message.getEmbeds().get(0).getColor().equals(Color.green)) return;
                if (emoji.equalsIgnoreCase("✅")) {
                    teamAccept(user, message);
                } else if (emoji.equalsIgnoreCase("❌")) {
                    //  User deny request, inform request sender
                    api.retrieveUserById(message.getEmbeds().get(0).getFooter().getText()).queue(uTarget ->
                            gPublic.retrieveMember(user).queue(member ->
                                    manager.sendPrivate(uTarget, BotManager.setNick(member, (String) msg.get("request_reply_deny")))));
                }
                message.delete().queue();
            } else if (player.getStep().equals("REFUSED")) {
                manager.sendPrivate(user, (String) msg.get("register_refused"));
            }
        } catch (SQLException throwables) {
            manager.sendPrivate(user, (String) msg.get("data_error"));
            if (Main.debug) {
                Main.log("[" + user.getAsTag() + "] SQL error");
                throwables.printStackTrace();
            }
        }
    }

    /**
     * Send the new step message to user in channel
     */
    private void privateSend(User user, Player player, PrivateChannel channel) {
        if (!BotManager.steps.containsKey(player.getStep())) {
            if (Main.debug) Main.log("[" + user.getAsTag() + "] Unregister step: " + player.getStep());
            return;
        }
        Step step = BotManager.steps.get(player.getStep());
        if (Main.debug) Main.log("[" + user.getAsTag() + "] Send step: " + step.getName());
        switch (step.getType()) {
            case "TEXT": {
                channel.sendMessage(getTEXTEmbed(user, step).build()).queue();
                break;
            }
            case "VALID": {
                manager.sendEmbed(channel, //   Skin exception
                        (step.getName().equals("Skin") ? getSKINEmbed(user, step, player).build() : getVALIDEmbed(user, step).build()));
                break;
            }
            case "CHOICES": {
                channel.sendMessage(getCHOICESEmbed(user, step).build()).queue(message -> {
                    int loop = 1;
                    for (String ignored : step.getChoices()) {
                        message.addReaction(Numbers.getString(loop)).queue();
                        loop++;
                    }
                });
                break;
            }
            case "END": {
                EmbedBuilder builder = getFINALEmbed(user, player);
                builder.setDescription(step.getMessage());
                manager.sendEmbed(channel, builder.build());
                break;
            }
            default: {
                if (Main.debug) Main.log("Unknown step: " + step.getType());
            }
        }
    }

    /**
     * Called when the staff approve the player
     */
    private void formStaffCheckValidate(List<Member> members, String reaction, Message message) {
        int count = 0;
        for (Member member : members) {
            if (Main.devmode && member.getIdLong() == 194050286535442432L) {
                count=3;
                break;
            }
            if (member.getRoles().contains(rAdmin)) {
                count++;
            }
        }
        try {
            Player player = PlayersManager.getPlayer(Long.parseLong(message.getEmbeds().get(0).getFooter().getText()));
            if (count < 3 || player.getStep().equals("ACCEPTED") || player.getStep().equals("REFUSED")) return;
            if (reaction.equals("✅")) {
                Team team = TeamsManager.createTeam(new Team(player.getUsername(), player.getDiscord_id()));
                player.setTeam(team.getId());
                player.setStep("ACCEPTED");
                PlayersManager.updatePlayer(player);
                api.openPrivateChannelById(player.getDiscord_id()).queue(channel -> {
                    channel.sendMessage(BotManager.setNick(channel.getUser(),(String) msg.get("register_accepted"))).queue();
                    channel.close().queue();
                });
                try {
                    gPublic.retrieveMemberById(player.getDiscord_id()).queue(member ->
                            gPublic.modifyNickname(member, player.getUsername()).queue());
                } catch (HierarchyException ignore) {}
                gPublic.addRoleToMember(player.getDiscord_id(), rValid).queue();
                gPublic.removeRoleFromMember(player.getDiscord_id(), rWaiting).queue();
                cAccept.sendMessage(message).queue();
                if (Main.debug) Main.log("[" + player.getUsername() + "] Candidature acceptée.");
            } else if (reaction.equals("❌")) {
                player.setStep("REFUSED");
                PlayersManager.updatePlayer(player);
                api.openPrivateChannelById(player.getDiscord_id()).queue(channel -> {
                    channel.sendMessage(BotManager.setNick(channel.getUser(), (String) msg.get("register_refused"))).queue();
                    channel.close().queue();
                });
                cDeny.sendMessage(message).queue();
                if (Main.debug) Main.log("[" + player.getUsername() + "] Candidature refusée.");
            }
            message.delete().queue();
        } catch (NullPointerException | SQLException throwables) {
            cCandid.sendMessage("SQL ERROR").queue();
            if (Main.debug) throwables.printStackTrace();
        }
    }

    /**
     * Get an embed with full responses of player
     */
    private EmbedBuilder getFINALEmbed(User user, Player player) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.red).setDescription(BotManager.setNick(user, (String) msg.get("register_staff_message"))).setThumbnail(
                "https://crafatar.com/renders/body/" + player.getUuid().toString() + "?size=512&overlay&default=MHF_Alex");
        for (Map.Entry<String, String> loop : player.getRegister().entrySet()) {
            builder.addField(":question: " + loop.getKey(), ":arrow_right: " + loop.getValue() + "\n", false);
        }
        builder.setFooter(String.valueOf(player.getDiscord_id()));
        return builder;
    }

    /**
     * Get an embed for a TEXT step
     */
    private EmbedBuilder getTEXTEmbed(User user, Step step) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE).setDescription(BotManager.setNick(user, step.getMessage()))
                .addField("Min chars", String.valueOf(step.getMin()), true)
                .addField("Max chars", String.valueOf(step.getMax()), true);
        return builder;
    }

    /**
     * Get an embed for a VALID step
     */
    private EmbedBuilder getVALIDEmbed(User user, Step step) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE).setDescription(BotManager.setNick(user, step.getMessage()));
        return builder;
    }

    /**
     * Get an embed for a CHOICES step
     */
    private EmbedBuilder getCHOICESEmbed(User user, Step step) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE).setDescription(BotManager.setNick(user, step.getMessage()));
        int loop = 1;
        for (String choice : step.getChoices()) {
            builder.addField("Choisir " + Numbers.getString(loop), choice, true);
            if (loop==9) break;
            loop++;
        }
        return builder;
    }

    /**
     * Get an embed with skin of player
     */
    private EmbedBuilder getSKINEmbed(User user, Step step, Player player) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE).setDescription(BotManager.setNick(user, step.getMessage()))
                .setImage("https://crafatar.com/renders/body/" + player.getUuid().toString() + "?size=512&overlay&default=MHF_Alex");
        return builder;
    }

    /**
     * Invite a user to the team of player
     */
    private void teamInvite(MessageReceivedEvent event) {
        Member mTarget = event.getMessage().getMentionedMembers().get(0);
        //  Prevent user to self invite
        if (event.getAuthor().getIdLong() == mTarget.getIdLong()) {
            manager.sendPrivate(event.getAuthor(), (String) msg.get("request_cant_self"));
            return;
        }
        //  If targed is valid
        if (!mTarget.getRoles().contains(rValid)) {
            manager.sendPrivate(event.getAuthor(), (String) msg.get("request_target_not_approved"));
            return;
        }
        //  If targed has already a team
        if (mTarget.getRoles().contains(rTeam)) {
            manager.sendPrivate(event.getAuthor(), (String) msg.get("request_target_already_in_team"));
            return;
        }
        Team senderTeam;
        Player sender;
        try {
            sender = PlayersManager.getPlayer(event.getAuthor().getIdLong());
            senderTeam = TeamsManager.getPlayerTeam(sender.getUsername());
        } catch (SQLException throwables) {
            manager.sendPrivate(event.getAuthor(), (String) msg.get("data_error"));
            if (Main.debug) throwables.printStackTrace();
            return;
        }
        //  Max team size, cancel request
        if (senderTeam.getSize() >= 6) {
            manager.sendPrivate(event.getAuthor(), (String) msg.get("request_team_full"));
            return;
        }
        //  Build request embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.GREEN)
                .setDescription(BotManager.setNick(event.getMember(), (String) msg.get("request_sent_ask")))
                .setImage("https://crafatar.com/renders/body/" + sender.getUuid().toString() + "?size=512&overlay&default=MHF_Alex")
                .setFooter(event.getAuthor().getId());
        manager.sendEmbed(mTarget.getUser(), embed.build());
        manager.sendPrivate(event.getAuthor(), BotManager.setNick(mTarget, (String) msg.get("request_sent_confirm")));
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
                    //  Add player to team
                    gPublic.addRoleToMember(mTarget, rTeam).queue();
                    pTarget.setTeam(team.getId());
                    team.addMembers(pTarget);
                    PlayersManager.updatePlayer(pTarget);
                    //  Send messages
                    manager.sendPrivate(uSender, BotManager.setNick(mTarget,
                            ((String) msg.get("request_reply_validation")).replaceAll("<team_name>", team.getName())));
                    manager.sendPrivate(uTarget,
                            ((String) msg.get("request_reply_confirm")).replaceAll("<nom_équipe>", team.getName()));
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
     * Open all private channels for every players
     */
    private void loadSqlStepsForm() {
        try {
            Connection connection = Main.getSql();
            PreparedStatement q;
            q = connection.prepareStatement("SELECT `discord_id` FROM `mcpg_player` WHERE `step` = 'REFUSED';");
            q.execute();
            ArrayList<Long> refused = new ArrayList<>();
            while (q.getResultSet().next()) {
                refused.add(q.getResultSet().getLong("discord_id"));
            }
            q = connection.prepareStatement("DELETE FROM `mcpg_player` WHERE `step` = 'REFUSED';");
            q.execute();
            q = connection.prepareStatement(
                    "SELECT `discord_id` FROM `mcpg_player` WHERE `step` != 'WAITING' AND `step` != 'ACCEPTED';");
            q.execute();
            while (q.getResultSet().next()) {
                api.openPrivateChannelById(q.getResultSet().getLong("discord_id")).queue();
            }
            q.close();
            cRegister.retrieveMessageById((Long) id.get("mRegister")).queue(message ->
                gPublic.retrieveMembersByIds(refused).onSuccess(members -> {
                    for (Member member : members) {
                        for (MessageReaction reaction : message.getReactions()) {
                            reaction.removeReaction(member.getUser()).queue();
                        }
                        gPublic.removeRoleFromMember(member, rWaiting).queue();
                    }
            }));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
