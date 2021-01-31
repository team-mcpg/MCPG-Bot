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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

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
        this.cAccept = api.getTextChannelById((String) id.get("cAccept"));
        this.cDeny = api.getTextChannelById((String) id.get("cDeny"));
        this.cTeamSearch = api.getTextChannelById((String) id.get("cTeamSearch"));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getType().equals(ChannelType.TEXT) && event.getMember() != null) {
            if (event.getTextChannel().equals(cTeamSearch)) {
                if (event.getMessage().getContentRaw().contains("/invite ") && event.getMessage().getMentionedMembers().size() != 0) {
                    teamInvite(event);
                    event.getMessage().delete().queue();
                }
            }
        } else if (event.getChannel().getType().equals(ChannelType.PRIVATE)) {
            try {
                Player player = PlayersManager.getPlayer(event.getAuthor().getIdLong());
                if (player.getDiscord_id() != event.getAuthor().getIdLong()) return;
                registerReceive(event.getMessage(), event.getAuthor(), event.getPrivateChannel(), null);
            } catch (SQLException throwables) {
                manager.sendPrivate(event.getAuthor(), (String) msg.get("data error"));
                if (Main.debugExeptions) throwables.printStackTrace();
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        event.retrieveMessage().queue(message -> {
            if (event.getChannel().getType().equals(ChannelType.TEXT) && event.getMember() != null) {
                if (event.getTextChannel().equals(cRegister)) {
                    if (event.getReactionEmote().getEmoji().equalsIgnoreCase("✅")) {
                        manager.sendPrivate(event.getUser(), (String) msg.get("register_start"));
                        registerInit(event);
                    }
                } else if (event.getTextChannel().equals(cCandid)) {
                    // TODO: 30/01/2021 Réaction sur regMsg, pour lancer le processus de candidature
                } else if (event.getTextChannel().equals(cAccept)) {
                    // TODO: 30/01/2021 Candidature acceptée par le staff
                } else if (event.getTextChannel().equals(cDeny)) {
                    // TODO: 30/01/2021 Candidature refusée par le staff
                }
            } else if (event.getChannel().getType().equals(ChannelType.PRIVATE) &&
                    message.getAuthor().isBot() && message.getEmbeds().size() == 1) {
                registerReceive(message, event.getUser(), event.getPrivateChannel(), event.getReactionEmote().getEmoji());
            }
        });
    }

    /**
     * Call when a member accept rules
     */
    private void registerInit(MessageReactionAddEvent event) {
        try {
            PlayersManager.createPlayer(event.getUserIdLong());
            manager.sendPrivate(event.getUser(), (String) msg.get("register_1"));
        } catch (SQLException throwables) {
            manager.sendPrivate(event.getUser(), (String) msg.get("data error"));
            if (Main.debugExeptions) throwables.printStackTrace();
        }
    }

    /**
     * Execute actions for the current step of user
     */
    private void registerReceive(Message message, User user, PrivateChannel channel, String emoji) {
        try {
            Player player = PlayersManager.getPlayer(user.getIdLong());
            if (BotManager.steps.containsKey(player.getStep())) return;
            Step step = BotManager.steps.get(player.getStep());
            switch (step.getType()) {
                case "TEXT": {
                    if (step.getMin() < message.getContentRaw().length() && step.getMax() > message.getContentRaw().length()) {
                        if (step.getName().equals("username")) { //  Username exception
                            player.setUsername(message.getContentRaw());
                            player.setUuid(UUID.fromString(MojangNames.getUuid(message.getContentRaw())));
                        }
                        player.setStep(step.getNext());
                        if (step.isSave()) {
                            HashMap<String, String> register = player.getRegister();
                            register.put(step.getName(), message.getContentRaw().replaceAll("|", ""));
                            player.setRegister(register);
                        }
                        PlayersManager.updatePlayer(player);
                    }
                    break;
                }
                case "VALID": {
                    if (emoji.equalsIgnoreCase("✅")) {
                        manager.sendPrivate(user, step.getYes());
                        player.setStep(step.getNext());
                        if (step.isSave()) {
                            HashMap<String, String> register = player.getRegister();
                            register.put(step.getName(), "yes");
                            player.setRegister(register);
                        }
                        PlayersManager.updatePlayer(player);
                    } else if (emoji.equalsIgnoreCase("❌")) {
                        manager.sendPrivate(user, step.getNo());
                        player.setStep(step.getBack());
                        PlayersManager.updatePlayer(player);
                    }
                    break;
                }
                case "CHOICES": {
                    String choice = step.getChoices().get(Numbers.getInt(emoji));
                    if (choice==null) break;
                    player.setStep(step.getNext());
                    if (step.isSave()) {
                        HashMap<String, String> register = player.getRegister();
                        register.put(step.getName(), choice);
                        player.setRegister(register);
                    }
                    PlayersManager.updatePlayer(player);
                    break;
                }
                case "END": {
                    if (!message.getEmbeds().get(0).getColor().equals(Color.green)) return;
                    if (emoji.equalsIgnoreCase("✅")) {
                        teamAccept(user, message);
                    } else if (emoji.equalsIgnoreCase("❌")) {
                        //  User deny request, inform request sender
                        manager.sendPrivate(api.getUserById(message.getEmbeds().get(0).getFooter().getText()),
                                BotManager.msgUsername(gPublic.getMember(user), (String) msg.get("request_reply_deny")));
                    }
                    message.delete().queue();
                }
            }
            registerSend(player, user, channel);
        } catch (SQLException throwables) {
            if (Main.debugExeptions) throwables.printStackTrace();
        }
    }

    /**
     * Send the new step message to user in channel
     */
    private void registerSend(Player player, User user, PrivateChannel channel) {
        if (BotManager.steps.containsKey(player.getStep())) return;
        Step step = BotManager.steps.get(player.getStep());
        switch (step.getType()) {
            case "TEXT": {
                manager.sendEmbed(channel, getTEXTEmbed(step).build());
                break;
            }
            case "VALID": {
                manager.sendEmbed(channel, //   Skin exception
                        (step.getName().equals("skin") ? getSKINEmbed(player, step).build() : getVALIDEmbed(step).build()));
                break;
            }
            case "CHOICES": {
                manager.sendEmbed(channel, getCHOICESEmbed(step).build());
                break;
            }
        }
    }

    /**
     * Get an embed for a TEXT step
     */
    private EmbedBuilder getTEXTEmbed(Step step) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE).setDescription(step.getMessage())
                .addField("Min chars", String.valueOf(step.getMin()), true)
                .addField("Max chars", String.valueOf(step.getMax()), true);
        return builder;
    }

    /**
     * Get an embed for a VALID step
     */
    private EmbedBuilder getVALIDEmbed(Step step) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE).setDescription(step.getMessage());
        return builder;
    }

    /**
     * Get an embed for a CHOICES step
     */
    private EmbedBuilder getCHOICESEmbed(Step step) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE).setDescription(step.getMessage());
        int loop = 1;
        for (String choice : step.getChoices()) {
            builder.addField(Numbers.getString(loop), choice, true);
            if (loop==9) break;
            loop++;
        }
        return builder;
    }

    /**
     * Get an embed with skin of player
     */
    private EmbedBuilder getSKINEmbed(Player player, Step step) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE).setDescription(step.getMessage())
                .setImage("https://crafatar.com/renders/body/" + player.getUuid().toString() + "?size=512&overlay&default=MHF_Alex");
        return builder;
    }

    /**
     * Invite a user to the team of player
     */
    private void teamInvite(MessageReceivedEvent event) {
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
        if (!pTarget.getStep().equalsIgnoreCase("END")) {
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
    private void teamAccept(User uTarget, Message message) {
        try {
            //  Init vars
            Team team = TeamsManager.getPlayerTeam(message.getEmbeds().get(0).getFooter().getText());
            Player pTarget = PlayersManager.getPlayer(uTarget.getIdLong());
            Member mTarget = gPublic.getMember(uTarget);
            User uSender = api.getUserById(team.getChief());
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
