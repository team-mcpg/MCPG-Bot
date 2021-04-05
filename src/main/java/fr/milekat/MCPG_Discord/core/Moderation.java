package fr.milekat.MCPG_Discord.core;

import fr.milekat.MCPG_Discord.Main;
import fr.milekat.MCPG_Discord.classes.Player;
import fr.milekat.MCPG_Discord.classes.PlayersManager;
import fr.milekat.MCPG_Discord.utils.DateMilekat;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.simple.JSONObject;

import java.awt.*;
import java.sql.SQLException;
import java.util.Date;

public class Moderation {
    /* Guilds */
    private final Guild gStaff;
    private final Guild gPublic;
    /* Category
    private final Category ccBan;
    */
    /* Channels */
    private final MessageChannel cLog;
    /* Roles
    private final Role rValid;
    private final Role rTeam;
    private final Role rMute;
    private final Role rBan;
    */

    public Moderation() {
        JDA api = Main.getJda();
        JSONObject id = (JSONObject) Main.getConfig().get("id");
        /* Main */
        this.gStaff = api.getGuildById((Long) id.get("gStaff"));
        this.gPublic = api.getGuildById((Long) id.get("gPublic"));
        //this.ccBan = api.getCategoryById((Long) id.get("ccBan"));
        this.cLog = api.getTextChannelById((Long) id.get("cLog"));
        /*
        this.rValid = api.getRoleById((Long) id.get("rValid"));
        this.rTeam = api.getRoleById((Long) id.get("rTeam"));
        this.rMute = api.getRoleById((Long) id.get("rMute"));
        this.rBan = api.getRoleById((Long) id.get("rBan"));
        */
    }

    /**
     * Arrivée d'une nouvelle sanction, récupération du membre "Ciblé" et du "Modo" qui a fait la sanction
     */
    public void newSanction(String action, String target, String mod, String sTime, String raison) throws SQLException {
        if (gStaff == null) {
            Main.log("Erreur de récupération du serveur.");
            return;
        }
        String time = sTime;
        String expiration = null;
        if (!sTime.equalsIgnoreCase("null")) {
            Date date = new Date(Long.parseLong(sTime));
            if (date.getTime() == Main.DATE_BAN.getTime()) {
                time = "Définitif";
                expiration = "Jusqu'à décision du staff";
            } else {
                time = DateMilekat.getDate(date);
                expiration = DateMilekat.reamingToString(date);
            }
        }
        String finalTime = time;
        String finalExpiration = expiration;
        Player pTarget = PlayersManager.getPlayer(target);
        gPublic.retrieveMemberById(pTarget.getDiscord_id()).queue(targetMember -> {
            if (mod.equalsIgnoreCase("console")) {
                doSanction(action, targetMember.getUser(), gStaff.getSelfMember(), finalTime, finalExpiration, raison);
            } else {
                try {
                    Player pMod = PlayersManager.getPlayer(mod);
                    gStaff.retrieveMemberById(pMod.getDiscord_id()).queue(modMember ->
                            doSanction(action, targetMember.getUser(), modMember, finalTime, finalExpiration, raison));
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    /**
     * Dispatch en fonction du type d'action
     */
    private void doSanction(String action, User target, Member mod, String time, String expiration, String raison) {
        if (action.equalsIgnoreCase("report")) {
            sendLogDiscord("Report", target, mod, null, null, raison);
        } else if (action.equalsIgnoreCase("ban")) {
            sendLogDiscord("Ban", target, mod, time, expiration, raison);
        } else if (action.equalsIgnoreCase("unban")) {
            sendLogDiscord("UnBan", target, mod, null, null, raison);
        } else if (action.equalsIgnoreCase("mute")) {
            sendLogDiscord("Mute", target, mod, time, expiration, raison);
        } else if (action.equalsIgnoreCase("unmute")) {
            sendLogDiscord("UnMute", target, mod, null, null, raison);
        } else if (action.equalsIgnoreCase("kick")) {
            sendLogDiscord("Kick", target, mod, null, null, raison);
        }
    }

    /**
     * Envoi d'un Log sur le cannal de log
     */
    private void sendLogDiscord(String sanction, User target, Member mod, String time, String expiration, String raison) {
        if (cLog == null) {
            Main.log("Log channel undefined");
            return;
        }
        EmbedBuilder embedSanction = new EmbedBuilder();
        embedSanction.setTitle("Nouveau log")
                .setColor(Color.RED)
                .addField(":bust_in_silhouette: Joueur sanctionné", target.getAsMention(), true)
                .addField(":police_officer: Modérateur", mod.getAsMention(), true)
                .addField(":hammer_pick: Action", sanction, true)
                .setTimestamp(new Date().toInstant());
        if (time != null && expiration != null) {
            embedSanction.addField(":stopwatch: Durée", time + System.lineSeparator() + expiration, true);
        }
        embedSanction.addField(":label: Raison", raison, true);
        cLog.sendMessage(embedSanction.build()).queue();
    }
}
