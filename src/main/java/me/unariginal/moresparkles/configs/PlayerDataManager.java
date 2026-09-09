package me.unariginal.moresparkles.configs;

import com.google.common.collect.Maps;
import com.google.gson.*;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.cache.PlayerBoostQueueCache;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static me.unariginal.moresparkles.utils.GsonUtils.gson;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PlayerDataManager {
    public static void loadPlayerBoostData(ServerPlayerEntity player) {
        PlayerData playerData = ConfigManager.loadFile("/players/" + player.getUuidAsString() + ".json", PlayerData.class);
        if (playerData == null) return;
        playerData.activeBoosts.values().forEach(boost -> {
            boost.resume();
            PlayerBoostCache.add(player, boost);
        });
        playerData.queuedBoosts.values().forEach(boostList -> boostList.forEach(boost -> PlayerBoostQueueCache.queueBoost(player, boost)));
    }

    public static void savePlayerBoostData(ServerPlayerEntity player) {
        Map<BoostType, Boost> boostMap = PlayerBoostCache.currentBoosts(player);
        if (boostMap == null || boostMap.isEmpty()) {
            deletePlayerBoostFile(player);
            return;
        }

        Map<BoostType, Queue<Boost>> queuedBoosts = PlayerBoostQueueCache.currentQueuedBoosts(player);
        Map<BoostType, LinkedList<Boost>> listQueuedBoost = Maps.newConcurrentMap();
        if (queuedBoosts != null && !queuedBoosts.isEmpty()) {
            queuedBoosts.forEach((boostType, queue) -> {
                if (!queue.isEmpty()) {
                    if (!listQueuedBoost.containsKey(boostType)) {
                        listQueuedBoost.put(boostType, new LinkedList<>());
                    }
                    queue.forEach(boost -> listQueuedBoost.get(boostType).add(boost));
                }
            });
        }
        PlayerData playerData = new PlayerData(new HashMap<>(boostMap), listQueuedBoost);

        File playerFile = new File(ConfigManager.configDir, "players/" + player.getUuidAsString() + ".json");
        playerFile.delete();
        ConfigManager.writeFile(playerFile, gson.toJson(playerData));
    }

    public static void deletePlayerBoostFile(ServerPlayerEntity player) {
        File playerFile = new File(ConfigManager.configDir, "players/" + player.getUuidAsString() + ".json");
        playerFile.delete();
    }
}
