package me.unariginal.moresparkles.configs;

import com.google.common.collect.Maps;
import com.google.gson.*;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.managers.BoostManager;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static me.unariginal.moresparkles.configs.ConfigManager.CONFIG;
import static me.unariginal.moresparkles.utils.GsonUtils.gson;

public class Config {
    public boolean debug;
    public boolean pausePlayerBoostsDuringGlobalBoost;
    public boolean allowQueuedBoosts;
    public boolean pausePlayerBoostsOnDisconnect;
    public boolean pausePlayerBoostsOnShutdown;
    public boolean pauseGlobalBoostsOnShutdown;
    @Nullable
    public Map<BoostType, Boost> activeGlobalBoost;
    @Nullable
    public Map<BoostType, LinkedList<Boost>> queuedGlobalBoosts;

    public static void saveGlobalBoostData() {
        CONFIG.activeGlobalBoost = BoostManager.globalBoosts;
        if (CONFIG.activeGlobalBoost != null && CONFIG.activeGlobalBoost.isEmpty()) CONFIG.activeGlobalBoost = null;

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
