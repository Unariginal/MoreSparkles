package me.unariginal.moresparkles.data;

import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.configs.MessagesConfig;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;

public class Boost {
    public transient boolean isGlobal;
    @Nullable
    public transient BossBar bossBar = null;
    @Nullable
    public transient MessagesConfig.BossbarSettings bossbarSettings = null;
    public transient long totalSeconds;
    public BoostType boostType;
    public float multiplier;
    public String boostStartTime;
    public String boostExpirationTime;
    @Nullable
    public String boostPauseTime = null;

    public Boost(boolean isGlobal, BoostType boostType, float multiplier, int duration) {
        this.isGlobal = isGlobal;
        this.boostType = boostType;
        this.multiplier = multiplier;
        this.boostStartTime = LocalDateTime.now().toString();
        this.boostExpirationTime = LocalDateTime.now().plusSeconds(duration).toString();
        this.totalSeconds = Duration.between(LocalDateTime.parse(boostStartTime), LocalDateTime.parse(boostExpirationTime)).toSeconds();

        if (isGlobal) {
            List<MessagesConfig.BossbarSettings> possibleBossbars = MESSAGES.globalBoostBossbars.values().stream().filter(settings -> settings.boostType == boostType).toList();
            if (!possibleBossbars.isEmpty()) bossbarSettings = possibleBossbars.getFirst();
        } else {
            List<MessagesConfig.BossbarSettings> possibleBossbars = MESSAGES.playerBoostBossbars.values().stream().filter(settings -> settings.boostType == boostType).toList();
            if (!possibleBossbars.isEmpty()) bossbarSettings = possibleBossbars.getFirst();
        }

        this.bossBar = BossBar.bossBar(getBossBarText(), 1F, bossbarSettings.barColor, bossbarSettings.barOverlay);
    }

    public void pause() {
        if (boostPauseTime != null) return;
        boostPauseTime = LocalDateTime.now().toString();
    }

    public void activate() {
        long durationSeconds = Duration.between(LocalDateTime.parse(boostStartTime), LocalDateTime.parse(boostExpirationTime)).toSeconds();
        boostStartTime = LocalDateTime.now().toString();
        boostExpirationTime = LocalDateTime.now().plusSeconds(durationSeconds).toString();
        totalSeconds = durationSeconds;
    }

    public void resume() {
        if (boostPauseTime == null) return;
        long pauseSeconds = Duration.between(LocalDateTime.parse(boostPauseTime), LocalDateTime.now()).toSeconds();

        boostStartTime = LocalDateTime.parse(boostStartTime).plusSeconds(pauseSeconds).toString();
        boostExpirationTime = LocalDateTime.parse(boostExpirationTime).plusSeconds(pauseSeconds).toString();
        totalSeconds = Duration.between(LocalDateTime.parse(boostStartTime), LocalDateTime.parse(boostExpirationTime)).toSeconds();

        boostPauseTime = null;
    }

    public long getTimeRemaining() throws DateTimeException {
        return Duration.between(LocalDateTime.now(), LocalDateTime.parse(boostExpirationTime)).toSeconds();
    }

    public void updateBossbar() {
        if (bossBar == null) return;
        float progressRate = 1.0F / totalSeconds;
        float total = progressRate * getTimeRemaining();

        if (total < 0F) total = 0F;
        if (total > 1F) total = 1F;

        try {
            bossBar.progress(total);
            bossBar.name(getBossBarText());
        } catch (IllegalArgumentException e) {
            MoreSparkles.LOGGER.error("[MoreSparkles] Failed to update bossbar", e);
        }
    }

    public Text getBossBarText() {
        if (bossbarSettings == null) return Text.empty();
        String text = boostPauseTime != null ? bossbarSettings.barTextPaused : bossbarSettings.barText;
        return TextUtils.deserialize(text, ParseContext.builder().boost(this).build());
    }
}
