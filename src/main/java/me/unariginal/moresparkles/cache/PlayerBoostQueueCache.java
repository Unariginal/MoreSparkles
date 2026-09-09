package me.unariginal.moresparkles.cache;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class PlayerBoostQueueCache {
    private static final Map<UUID, Map<BoostType, Queue<Boost>>> queuedPlayerBoosts = Maps.newConcurrentMap();

    @Nullable
    public static Queue<Boost> currentBoostQueue(UUID uuid, BoostType type) {
        Map<BoostType, Queue<Boost>> queuedBoosts = currentQueuedBoosts(uuid);
        if (queuedBoosts != null) return queuedBoosts.get(type);
        return null;
    }

    @Nullable
    public static Queue<Boost> currentBoostQueue(ServerPlayerEntity player, BoostType type) {
        return currentBoostQueue(player.getUuid(), type);
    }

    @Nullable
    public static Map<BoostType, Queue<Boost>> currentQueuedBoosts(UUID uuid) {
        return queuedPlayerBoosts.get(uuid);
    }

    @Nullable
    public static Map<BoostType, Queue<Boost>> currentQueuedBoosts(ServerPlayerEntity player) {
        return currentQueuedBoosts(player.getUuid());
    }

    public static boolean hasQueue(UUID uuid) {
        return queuedPlayerBoosts.containsKey(uuid);
    }

    public static boolean hasQueue(ServerPlayerEntity player) {
        return hasQueue(player.getUuid());
    }

    public static void queueBoost(UUID uuid, Boost boost) {
        Map<BoostType, Queue<Boost>> queuedBoosts = currentQueuedBoosts(uuid);
        if (queuedBoosts == null) queuedBoosts = new HashMap<>();
        Queue<Boost> queue = queuedBoosts.get(boost.boostType);
        if (queue == null) queue = Queues.newConcurrentLinkedQueue();
        queue.add(boost);
        queuedBoosts.put(boost.boostType, queue);
        queuedPlayerBoosts.put(uuid, queuedBoosts);
    }

    public static void queueBoost(ServerPlayerEntity player, Boost boost) {
        queueBoost(player.getUuid(), boost);
    }

    @Nullable
    public static Boost poll(UUID uuid, BoostType type) {
        Map<BoostType, Queue<Boost>> queuedBoosts = currentQueuedBoosts(uuid);

        if (queuedBoosts == null) return null;
        Queue<Boost> boostQueue = queuedBoosts.get(type);

        if (boostQueue == null) return null;
        Boost boost = boostQueue.poll();

        if (boostQueue.isEmpty()) queuedBoosts.remove(type);
        else queuedBoosts.put(type, boostQueue);

        if (queuedBoosts.isEmpty()) queuedPlayerBoosts.remove(uuid);
        else queuedPlayerBoosts.put(uuid, queuedBoosts);

        return boost;
    }

    @Nullable
    public static Boost poll(ServerPlayerEntity player, BoostType type) {
        return poll(player.getUuid(), type);
    }

    public static void remove(UUID uuid) {
        queuedPlayerBoosts.remove(uuid);
    }

    public static void remove(ServerPlayerEntity player) {
        remove(player.getUuid());
    }
}
