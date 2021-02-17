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

public class RegisterEvent extends ListenerAdapter {
    /* Main */
    private final BotManager manager;
    private final JDA api;
    private final JSONObject id;
    private final JSONObject msg;
    private final List<String> endSteps = new ArrayList<>(Arrays.asList("WAITING","ACCEPTED", "REFUSED"));
    /* Guilds */
    private final Guild gStaff;
    private final Guild gPublic;
    /* Roles */
    private final Role rWaiting;
    private final Role rValid;
    private final Role rAdmin;
    /* Staff Channels */
    private final TextChannel cCandid;
    private final TextChannel cAccept;
    private final TextChannel cDeny;
    /* Public Channels */
    private final TextChannel cRegister;

    public RegisterEvent(BotManager manager, JDA api, JSONObject id, JSONObject msg) {
        this.manager = manager;
        this.api = api;
        this.id = id;
        this.msg = msg;
        this.gStaff = api.getGuildById((Long) id.get("gStaff"));
        this.gPublic = api.getGuildById((Long) id.get("gPublic"));
        this.rWaiting = api.getRoleById((Long) id.get("rWaiting"));
        this.rValid = api.getRoleById((Long) id.get("rValid"));
        this.rAdmin = api.getRoleById((Long) id.get("rAdmin"));
        this.cRegister = api.getTextChannelById((Long) id.get("cRegister"));
        this.cCandid = api.getTextChannelById((Long) id.get("cCandid"));
        this.cAccept = api.getTextChannelById((Long) id.get("cAccept"));
        this.cDeny = api.getTextChannelById((Long) id.get("cDeny"));
        loadSqlStepsForm();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && event.getChannel().getType().equals(ChannelType.PRIVATE)) {
            try {
                Player player = PlayersManager.getPlayer(event.getAuthor().getIdLong());
                if (!endSteps.contains(player.getStep())) {
                    formStepReceive(event.getMessage(), event.getAuthor(), event.getPrivateChannel(), null);
                }
            } catch (SQLException ignore) {}
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        User user = event.getUser();
        event.retrieveMessage().queue(message -> {
            try {
                String emoji = event.getReactionEmote().getEmoji();
                if (event.getChannel().getType().equals(ChannelType.TEXT) && event.getMember() != null) {
                    if (event.getTextChannel().equals(cRegister) && !event.getMember().getRoles().contains(rWaiting)
                            && !event.getMember().getRoles().contains(rValid)) { /* Reg channel + not registered */
                        formStart(user);
                    } else if (event.getTextChannel().equals(cCandid) && (emoji.equals("✅") || emoji.equals("❌"))) {
                        event.getReaction().retrieveUsers().queue(users -> gStaff.retrieveMembers(users).onSuccess(members ->
                                formStaffCheckValidate(members, event.getReaction().getReactionEmote().getEmoji(), message)));
                    }
                } else if (event.getChannel().getType().equals(ChannelType.PRIVATE) && event.getUser() != null &&
                        message.getAuthor().isBot() && message.getEmbeds().size() == 1) {
                    formStepReceive(message, user, event.getPrivateChannel(), emoji);
                }
            } catch (IllegalStateException ignore) {/* Cannot get emoji code for custom emote reaction */}
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
            user.openPrivateChannel().queue(channel -> formStepSend(user, player, channel));
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
    private void formStepReceive(Message message, User user, PrivateChannel channel, String emoji) {
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
                                    formStepSend(user, player, channel);
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
                formStepSend(user, player, channel);
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
    private void formStepSend(User user, Player player, PrivateChannel channel) {
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
                manager.sendPrivate(user, //   Skin exception
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
                manager.sendPrivate(user, builder.build());
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
                gPublic.retrieveMemberById(player.getDiscord_id()).queue(member -> {
                    if (gPublic.getSelfMember().canInteract(member)) {
                        gPublic.modifyNickname(member, player.getUsername()).queue();
                    }
                    gPublic.addRoleToMember(member, rValid).queue();
                    gPublic.removeRoleFromMember(member, rWaiting).queue();
                });
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
            cCandid.sendMessage((String) msg.get("data_error")).queue();
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
