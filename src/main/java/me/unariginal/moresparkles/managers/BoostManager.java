package me.unariginal.moresparkles.managers;

import com.google.common.collect.Maps;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Queue;

public class BoostManager {
    public static Map<BoostType, Boost> globalBoosts = Maps.newConcurrentMap();
    public static Map<BoostType, Queue<Boost>> queuedGlobalBoosts = Maps.newConcurrentMap();

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
