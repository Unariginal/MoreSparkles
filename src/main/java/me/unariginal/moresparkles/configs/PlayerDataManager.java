package me.unariginal.moresparkles.configs;

import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.cache.PlayerBoostQueueCache;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static me.unariginal.moresparkles.utils.GsonUtils.gson;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PlayerDataManager {
    public static void loadPlayerBoostData(ServerPlayerEntity player) {
        File playerFile = getPlayerFile(player);
        if (!playerFile.exists()) return;

        PlayerData playerData = ConfigManager.loadFile("players/" + playerFile.getName(), PlayerData.class);
        if (playerData == null) {
            ConfigManager.backupBrokenFile(playerFile);
            return;
        }
        if (playerData.activeBoosts != null) {
            playerData.activeBoosts.values().forEach(boost -> {
                boost.setupBossbar(false);
                boost.resume();
                PlayerBoostCache.add(player, boost);
            });
        }
        if (playerData.queuedBoosts != null) {
            playerData.queuedBoosts.values().forEach(boostList -> boostList.forEach(boost -> {
                boost.setupBossbar(false);
                PlayerBoostQueueCache.queueBoost(player, boost);
            }));
        }
    }

    public static void savePlayerBoostData(ServerPlayerEntity player) {
        Map<BoostType, Boost> boostMap = PlayerBoostCache.currentBoosts(player);
        Map<BoostType, LinkedList<Boost>> listQueuedBoost = ConfigManager.toSerializableQueues(PlayerBoostQueueCache.currentQueuedBoosts(player));
        if ((boostMap == null || boostMap.isEmpty()) && listQueuedBoost.isEmpty()) {
            deletePlayerBoostFile(player);
            return;
        }

        PlayerData playerData = new PlayerData(boostMap != null ? new HashMap<>(boostMap) : new HashMap<>(), listQueuedBoost);

        File playerFile = getPlayerFile(player);
        if (!playerFile.exists()) {
            try {
                Files.createDirectories(playerFile.getParentFile().toPath());
                Files.createFile(playerFile.toPath());
            } catch (IOException e) {
                MoreSparkles.LOGGER.error("[MoreSparkles] Failed to create player data file", e);
            }
        }
        ConfigManager.writeFile(playerFile, gson.toJson(playerData));
    }

    public static void deletePlayerBoostFile(ServerPlayerEntity player) {
        getPlayerFile(player).delete();
    }

    private static File getPlayerFile(ServerPlayerEntity player) {
        return new File(ConfigManager.configDir, "players/" + player.getUuidAsString() + ".json");
    }
}
