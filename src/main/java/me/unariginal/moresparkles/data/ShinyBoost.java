package me.unariginal.moresparkles.data;

import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.utils.TextUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class ShinyBoost {
    public UUID player_uuid = null;
    public float multiplier;
    public int duration;
    public long time_remaining;
    public BossBar bossBar;

    public ShinyBoost(ServerPlayerEntity player, float multiplier, int duration) {
        if (player != null)
            this.player_uuid = player.getUuid();
        this.multiplier = multiplier;
        this.duration = duration;
        this.time_remaining = duration * 20L;
        if (player_uuid == null)
            this.bossBar = BossBar.bossBar(getBossBarText(), 1F, MoreSparkles.INSTANCE.getMessages().globalBarColor, MoreSparkles.INSTANCE.getMessages().globalBarOverlay);
        else
            this.bossBar = BossBar.bossBar(getBossBarText(), 1F, MoreSparkles.INSTANCE.getMessages().playerBarColor, MoreSparkles.INSTANCE.getMessages().playerBarOverlay);
    }

    public ShinyBoost(ServerPlayerEntity player, float multiplier, int duration, long time_remaining) {
        if (player != null)
            this.player_uuid = player.getUuid();
        this.multiplier = multiplier;
        this.duration = duration;
        this.time_remaining = time_remaining;
        if (player_uuid == null)
            this.bossBar = BossBar.bossBar(getBossBarText(), 1F, MoreSparkles.INSTANCE.getMessages().globalBarColor, MoreSparkles.INSTANCE.getMessages().globalBarOverlay);
        else
            this.bossBar = BossBar.bossBar(getBossBarText(), 1F, MoreSparkles.INSTANCE.getMessages().playerBarColor, MoreSparkles.INSTANCE.getMessages().playerBarOverlay);
    }

    public Text getBossBarText() {
        if (player_uuid == null)
            return TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().globalBarText, this));
        else
            return TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().playerBarText, this));
    }
}
