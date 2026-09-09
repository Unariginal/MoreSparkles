package me.unariginal.moresparkles.cache;

import com.google.common.collect.Maps;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class PlayerBoostCache {
    private static final Map<UUID, Map<BoostType, Boost>> playerBoosts = Maps.newConcurrentMap();

    @Nullable
    public static Map<BoostType, Boost> currentBoosts(UUID uuid) {
        return playerBoosts.get(uuid);
    }

    @Nullable
    public static Map<BoostType, Boost> currentBoosts(ServerPlayerEntity player) {
        return currentBoosts(player.getUuid());
    }

    @Nullable
    public static Boost currentBoost(UUID uuid, BoostType type) {
        Map<BoostType, Boost> boostMap = currentBoosts(uuid);
        if (boostMap != null) return boostMap.get(type);
        return null;
    }

    @Nullable
    public static Boost currentBoost(ServerPlayerEntity player, BoostType type) {
        return currentBoost(player.getUuid(), type);
    }

    public static boolean hasBoost(UUID uuid, BoostType type) {
        Map<BoostType, Boost> boostMap = currentBoosts(uuid);
        if (boostMap != null) return boostMap.containsKey(type);
        return false;
    }

    public static boolean hasBoost(ServerPlayerEntity player, BoostType type) {
        return hasBoost(player.getUuid(), type);
    }

    public static void add(UUID uuid, Boost boost) {
        Map<BoostType, Boost> boostMap = currentBoosts(uuid);
        if (boostMap == null) boostMap = Maps.newConcurrentMap();
        boostMap.put(boost.boostType, boost);
        playerBoosts.put(uuid, boostMap);
    }

    public static void add(ServerPlayerEntity player, Boost boost) {
        add(player.getUuid(), boost);
    }

    public static void remove(UUID uuid, BoostType type) {
        Map<BoostType, Boost> boostMap = currentBoosts(uuid);
        if (boostMap != null) boostMap.remove(type);
        if ((boostMap == null && playerBoosts.containsKey(uuid)) || (boostMap != null && boostMap.isEmpty())) playerBoosts.remove(uuid);
    }

    public static void remove(ServerPlayerEntity player, BoostType type) {
        remove(player.getUuid(), type);
    }

    public static void removeAll(UUID uuid) {
        playerBoosts.remove(uuid);
    }

    public static void removeAll(ServerPlayerEntity player) {
        removeAll(player.getUuid());
    }

    public static void pauseAll(UUID uuid) {
        Map<BoostType, Boost> boostMap = currentBoosts(uuid);
        if (boostMap != null) boostMap.values().forEach(Boost::pause);
    }

    public static void pauseAll(ServerPlayerEntity player) {
        pauseAll(player.getUuid());
    }
}
