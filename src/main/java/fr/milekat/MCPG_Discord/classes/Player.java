package fr.milekat.MCPG_Discord.classes;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Player {
    //  Minecraft Username
    private String username;
    //  Minecraft UUID
    private UUID uuid;
    private final long discord_id;
    //  Register step
    private String step;
    private LinkedHashMap<String, String> register;
    //  Team ID
    private int team;
    /* Bans / Mutes */
    private Date muted;
    private Date banned;
    private String reason;

    /**
     * For new user
     */
    public Player(long discord_id, String step) {
        this.discord_id = discord_id;
        this.step = step;
    }

    /**
     * For users from SQL
     */
    public Player(String username, UUID uuid, long discord_id, String step, String register , int team, Date muted, Date banned, String reason) {
        this.username = username;
        this.uuid = uuid;
        this.discord_id = discord_id;
        this.step = step;
        LinkedHashMap<String, String> mapRegister = new LinkedHashMap<>();
        if (register!=null) {
            for (String loop : register.split("\\|\\|")) {
                if (loop.contains("|")) mapRegister.put(loop.split("\\|")[0], loop.split("\\|")[1]);
            }
        }
        this.register = mapRegister;
        this.team = team;
        this.muted = muted;
        this.banned = banned;
        this.reason = reason;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public long getDiscord_id() {
        return discord_id;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public LinkedHashMap<String, String> getRegister() {
        return register;
    }

    public String getStringRegister() {
        StringBuilder sRegister = new StringBuilder();
        if (register!=null) {
            for (Map.Entry<String, String> loop : register.entrySet()) {
                sRegister.append(loop.getKey()).append("|").append(loop.getValue()).append("||");
            }
            return sRegister.toString();
        }
        return null;
    }

    public void setRegister(LinkedHashMap<String, String> register) {
        this.register = register;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public Date getMuted() {
        return muted;
    }

    public void setMuted(Date muted) {
        this.muted = muted;
    }

    public Date getBanned() {
        return banned;
    }

    public void setBanned(Date banned) {
        this.banned = banned;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
