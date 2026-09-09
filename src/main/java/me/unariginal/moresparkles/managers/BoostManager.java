package me.unariginal.moresparkles.managers;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Queue;

import static me.unariginal.moresparkles.configs.ConfigManager.CONFIG;

public class BoostManager {
    public static Map<BoostType, Boost> globalBoosts = Maps.newConcurrentMap();
    public static Map<BoostType, Queue<Boost>> queuedGlobalBoosts = Maps.newConcurrentMap();

    public static void loadFromConfig() {
        if (MoreSparkles.INSTANCE.audiences != null) {
            globalBoosts.values().forEach(boost -> {
                if (boost.bossBar != null) MoreSparkles.INSTANCE.audiences.all().hideBossBar(boost.bossBar);
            });
        }
        globalBoosts.clear();
        queuedGlobalBoosts.clear();

        if (CONFIG.activeGlobalBoosts != null) {
            CONFIG.activeGlobalBoosts.forEach((type, boost) -> {
                boost.setupBossbar(true);
                boost.resume();
                globalBoosts.put(type, boost);
                if (boost.bossBar != null && MoreSparkles.INSTANCE.audiences != null) {
                    MoreSparkles.INSTANCE.audiences.all().showBossBar(boost.bossBar);
                }
            });
        }

        if (CONFIG.queuedGlobalBoosts != null) {
            CONFIG.queuedGlobalBoosts.forEach((type, queuedBoosts) -> {
                Queue<Boost> queue = Queues.newConcurrentLinkedQueue();
                queuedBoosts.forEach(boost -> {
                    boost.setupBossbar(true);
                    queue.add(boost);
                });
                queuedGlobalBoosts.put(type, queue);
            });
        }
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
}
