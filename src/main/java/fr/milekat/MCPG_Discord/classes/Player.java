package fr.milekat.MCPG_Discord.classes;

import java.util.UUID;

public class Player {
    //  Minecraft Username
    private String username;
    //  Minecraft prefix
    private final String prefix;
    //  Minecraft UUID
    private UUID uuid;
    private final long discord_id;
    //  Register step
    private int step;
    //  Team ID
    private int team;
    /* Bans / Mutes */
    private String muted;
    private String banned;
    private String reason;

    /**
     * For new user
     */
    public Player(long discord_id, int step) {
        this.prefix = null;
        this.discord_id = discord_id;
        this.step = step;
    }

    /**
     * For users from SQL
     */
    public Player(String username, String prefix, UUID uuid, long discord_id, int step, int team, String muted, String banned, String reason) {
        this.username = username;
        this.prefix = prefix;
        this.uuid = uuid;
        this.discord_id = discord_id;
        this.step = step;
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

    public String getPrefix() {
        return prefix;
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

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public String getMuted() {
        return muted;
    }

    public void setMuted(String muted) {
        this.muted = muted;
    }

    public String getBanned() {
        return banned;
    }

    public void setBanned(String banned) {
        this.banned = banned;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
