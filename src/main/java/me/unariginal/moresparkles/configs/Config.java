package me.unariginal.moresparkles.configs;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.common.collect.Maps;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.managers.BoostManager;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static me.unariginal.moresparkles.configs.ConfigManager.CONFIG;
import static me.unariginal.moresparkles.utils.GsonUtils.gson;

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
    @Nullable
    public Map<BoostType, List<String>> persistentDataKeyBlacklist;
    @Nullable
    public Map<BoostType, Boost> activeGlobalBoosts;
    @Nullable
    public Map<BoostType, LinkedList<Boost>> queuedGlobalBoosts;

    public static boolean canBeBoosted(Pokemon pokemon, BoostType boostType) {
        if (CONFIG.persistentDataKeyBlacklist != null && CONFIG.persistentDataKeyBlacklist.containsKey(boostType)) {
            return CONFIG.persistentDataKeyBlacklist.get(boostType).stream().noneMatch(key -> pokemon.getPersistentData().contains(key));
        }
        return true;
    }

    public static void saveGlobalBoostData() {
        CONFIG.activeGlobalBoosts = BoostManager.globalBoosts;
        if (CONFIG.activeGlobalBoosts != null && CONFIG.activeGlobalBoosts.isEmpty()) CONFIG.activeGlobalBoosts = null;

        Map<BoostType, Queue<Boost>> queuedBoosts = BoostManager.queuedGlobalBoosts;
        Map<BoostType, LinkedList<Boost>> listQueuedBoost;
        if (queuedBoosts != null && !queuedBoosts.isEmpty()) {
            listQueuedBoost = Maps.newConcurrentMap();
            queuedBoosts.forEach((boostType, queue) -> {
                if (!queue.isEmpty()) {
                    if (!listQueuedBoost.containsKey(boostType)) {
                        listQueuedBoost.put(boostType, new LinkedList<>());
                    }
                    queue.forEach(boost -> listQueuedBoost.get(boostType).add(boost));
                }
            });
        }
        else if (queuedBoosts != null) listQueuedBoost = null;
        else listQueuedBoost = Maps.newConcurrentMap();
        CONFIG.queuedGlobalBoosts = listQueuedBoost;

        File configFile = new File(ConfigManager.configDir, "config.json");
        configFile.delete();
        ConfigManager.writeFile(configFile, gson.toJson(CONFIG));
    }
}
