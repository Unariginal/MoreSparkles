package me.unariginal.moresparkles.configs;

import com.cobblemon.mod.common.pokemon.Pokemon;
import me.unariginal.moresparkles.data.BoostType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static me.unariginal.moresparkles.configs.ConfigManager.CONFIG;

public class Config {
    public boolean debug;
    public boolean allowQueuedBoosts;
    public boolean pausePlayerBoostsDuringGlobalBoost;
    public boolean pausePlayerBoostsOnDisconnect;
    public boolean pausePlayerBoostsOnShutdown;
    public boolean pauseGlobalBoostsOnShutdown;
    public boolean experienceBoosterIgnoresCandy;
    public boolean experienceBoosterIgnoresSidemodSource;
    public boolean evBoosterIgnoresVitamins;
    public boolean evBoosterIgnoresSidemodSource;
    public float hiddenAbilityBoosterBaseChance;
    public int ivBoosterStrengthConstant;
    @Nullable
    public Map<BoostType, List<String>> persistentDataKeyBlacklist;
    @Nullable
    public WebhookConnectionSettings webhookSettings;

    public static class WebhookConnectionSettings {
        public boolean enabled;
        public String url;
        public int updateRateSeconds;
        public boolean deleteWhenBoostEnds;
    }

    public static boolean canBeBoosted(Pokemon pokemon, BoostType boostType) {
        if (CONFIG.persistentDataKeyBlacklist != null && CONFIG.persistentDataKeyBlacklist.containsKey(boostType)) {
            return CONFIG.persistentDataKeyBlacklist.get(boostType).stream().noneMatch(key -> pokemon.getPersistentData().contains(key));
        }
        return true;
    }
}
