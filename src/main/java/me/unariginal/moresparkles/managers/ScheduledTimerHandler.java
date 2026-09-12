package me.unariginal.moresparkles.managers;

import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.cache.PlayerBoostQueueCache;
import me.unariginal.moresparkles.configs.Config;
import me.unariginal.moresparkles.configs.PlayerDataManager;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.utils.Threading;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;

import static me.unariginal.moresparkles.configs.ConfigManager.CONFIG;
import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;

public class ScheduledTimerHandler {
    public ScheduledFuture<?> schedule;
    private int webhookUpdateTracker = 0;
    private final Map<BoostType, Long> webhookTracker = new HashMap<>();

    public ScheduledTimerHandler() {
        schedule = Threading.runDelayedTaskAsyncTimer(this::updateBoosts, 1L, 1L);
    }

    public void updateBoosts() {
        MoreSparkles.INSTANCE.server.execute(() -> {
            try {
                updateBoostsOnServerThread();
            } catch (Throwable throwable) {
                MoreSparkles.LOGGER.error("[MoreSparkles] Error updating boosts and boost displays.", throwable);
            }
        });
    }

    public void updateBoostsOnServerThread() {
        for (ServerPlayerEntity player : MoreSparkles.INSTANCE.server.getPlayerManager().getPlayerList()) {
            Map<BoostType, Boost> boostMap = PlayerBoostCache.currentBoosts(player);
            if (boostMap != null) {
                for (Boost boost : boostMap.values()) {
                    if (boost.getTimeRemaining() <= 0) {
                        if (boost.bossBar != null) player.hideBossBar(boost.bossBar);
                        PlayerBoostCache.remove(player, boost.boostType);

                        Boost nextBoost = PlayerBoostQueueCache.poll(player, boost.boostType);
                        if (nextBoost != null) {
                            nextBoost.activate();
                            PlayerBoostCache.add(player, nextBoost);
                            if (nextBoost.bossBar != null) player.showBossBar(nextBoost.bossBar);
                        }
                        PlayerDataManager.savePlayerBoostData(player);
                        continue;
                    }

                    boost.updateBossbar();
                }
            }
        }

        if (BoostManager.globalBoosts != null) {
            for (Boost boost : BoostManager.globalBoosts.values()) {
                if (boost.getTimeRemaining() <= 0) {
                    if (boost.bossBar != null) MoreSparkles.INSTANCE.audiences.all().hideBossBar(boost.bossBar);
                    BoostManager.globalBoosts.remove(boost.boostType);

                    Long webhookId = webhookTracker.get(boost.boostType);
                    if (CONFIG.webhookSettings != null && CONFIG.webhookSettings.deleteWhenBoostEnds && webhookId != null && webhookId != -1) {
                        WebhookManager.deleteWebhook(webhookId);
                    }
                    webhookTracker.remove(boost.boostType);

                    Queue<Boost> queue = BoostManager.queuedGlobalBoosts != null ? BoostManager.queuedGlobalBoosts.get(boost.boostType) : null;
                    Boost nextBoost = queue != null ? queue.poll() : null;
                    if (nextBoost != null) {
                        nextBoost.activate();
                        BoostManager.globalBoosts.put(nextBoost.boostType, nextBoost);
                        if (nextBoost.bossBar != null) MoreSparkles.INSTANCE.audiences.all().showBossBar(nextBoost.bossBar);
                    } else {
                        BoostManager.resumePlayerBoosts(boost.boostType);
                    }

                    Config.saveGlobalBoostData();
                    continue;
                }

                boost.updateBossbar();
            }
        }

        if (CONFIG.webhookSettings != null && CONFIG.webhookSettings.enabled) {
            if (webhookUpdateTracker <= 0) {
                webhookUpdateTracker = CONFIG.webhookSettings.updateRateSeconds;
                if (BoostManager.globalBoosts != null) {
                    for (Boost boost : BoostManager.globalBoosts.values()) {
                        if (MESSAGES.boostWebhooks == null || MESSAGES.boostWebhooks.get(boost.boostType) == null) continue;

                        Long id = webhookTracker.get(boost.boostType);
                        if (id != null && id != -1) {
                            WebhookManager.editWebhookEmbed(id, boost)
                                    .thenAccept(webhookId -> {
                                        if (webhookId == null) return;
                                        MoreSparkles.INSTANCE.server.execute(() -> webhookTracker.put(boost.boostType, webhookId));
                                    });
                        } else {
                            WebhookManager.sendWebhookEmbed(boost)
                                    .thenAccept(webhookId -> {
                                        if (webhookId == null) return;
                                        MoreSparkles.INSTANCE.server.execute(() -> webhookTracker.put(boost.boostType, webhookId));
                                    });
                        }
                    }
                }
            }
            webhookUpdateTracker--;
        }
    }
}
