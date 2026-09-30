package me.unariginal.moresparkles.managers;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.configs.GlobalBoostData;
import me.unariginal.moresparkles.configs.GlobalBoostDataManager;
import me.unariginal.moresparkles.configs.ItemsConfig;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import me.unariginal.moresparkles.items.CharmItemsGroup;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Queue;

import static me.unariginal.moresparkles.configs.ConfigManager.*;
import static me.unariginal.moresparkles.configs.ConfigManager.ITEMS_CONFIG;

public class BoostManager {
    public static Map<BoostType, Boost> globalBoosts = Maps.newConcurrentMap();
    public static Map<BoostType, Queue<Boost>> queuedGlobalBoosts = Maps.newConcurrentMap();

    public static void loadGlobalBoosts() {
        if (MoreSparkles.INSTANCE.audiences != null) {
            globalBoosts.values().forEach(boost -> {
                if (boost.bossBar != null) MoreSparkles.INSTANCE.audiences.all().hideBossBar(boost.bossBar);
            });
        }
        globalBoosts.clear();
        queuedGlobalBoosts.clear();

        GlobalBoostData data = GlobalBoostDataManager.loadGlobalBoostData();
        if (data == null) return;

        if (data.activeBoosts != null) {
            data.activeBoosts.forEach((type, boost) -> {
                boost.setupBossbar(true);
                boost.resume();
                globalBoosts.put(type, boost);
                if (boost.bossBar != null && MoreSparkles.INSTANCE.audiences != null) {
                    MoreSparkles.INSTANCE.audiences.all().showBossBar(boost.bossBar);
                }
            });
        }

        if (data.queuedBoosts != null) {
            data.queuedBoosts.forEach((type, queuedBoosts) -> {
                Queue<Boost> queue = Queues.newConcurrentLinkedQueue();
                queuedBoosts.forEach(boost -> {
                    boost.setupBossbar(true);
                    queue.add(boost);
                });
                queuedGlobalBoosts.put(type, queue);
            });
        }
    }

    public static boolean shouldPausePlayerBoosts(BoostType type) {
        return CONFIG.pausePlayerBoostsDuringGlobalBoost && globalBoosts.containsKey(type);
    }

    public static void pausePlayerBoosts(BoostType type) {
        if (MoreSparkles.INSTANCE.server == null) return;
        for (ServerPlayerEntity player : MoreSparkles.INSTANCE.server.getPlayerManager().getPlayerList()) {
            Boost boost = PlayerBoostCache.currentBoost(player, type);
            if (boost != null) boost.pause();
        }
    }

    public static void resumePlayerBoosts(BoostType type) {
        if (MoreSparkles.INSTANCE.server == null) return;
        for (ServerPlayerEntity player : MoreSparkles.INSTANCE.server.getPlayerManager().getPlayerList()) {
            Boost boost = PlayerBoostCache.currentBoost(player, type);
            if (boost != null) boost.resume();
        }
    }

    public static float getGenericMultiplierTotal(ServerPlayerEntity player, BoostType boostType) {
        float multiplier = 0.0f;
        Boost boost = PlayerBoostCache.currentBoost(player, boostType);
        if (boost != null && boost.boostPauseTime == null) {
            multiplier += boost.multiplier;
        }

        if (BoostManager.globalBoosts != null && BoostManager.globalBoosts.get(boostType) != null) {
            multiplier += BoostManager.globalBoosts.get(boostType).multiplier;
        }

        for (BoostArea boostArea : BOOST_AREAS.values()) {
            if (boostArea.boostType == boostType) {
                if (boostArea.isInArea(player.getServerWorld(), player.getX(), player.getY(), player.getZ())) {
                    multiplier += boostArea.multiplier;
                }
            }
        }

        if (ITEMS_CONFIG.charms != null) {
            for (String key : CharmItemsGroup.charmItems.keySet()) {
                ItemsConfig.CharmData charmData = ITEMS_CONFIG.charms.get(key);
                if (charmData == null || charmData.boostType != boostType) continue;

                if (player.getInventory().contains(s -> s.isOf(CharmItemsGroup.charmItems.get(key)))) {
                    multiplier += charmData.multiplier;
                    break;
                }
            }
        }

        return multiplier > 0 ? multiplier : 1.0f;
    }
}
