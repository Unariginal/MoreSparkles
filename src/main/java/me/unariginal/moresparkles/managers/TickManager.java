package me.unariginal.moresparkles.managers;

import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.configs.PlayerDataManager;
import me.unariginal.moresparkles.data.SparkleArea;
import me.unariginal.moresparkles.data.ShinyBoost;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TickManager {
    private static long particleCooldown = 3 * 20;

    public static void tickBoosts() {
        List<ShinyBoost> toRemove = new ArrayList<>();
        if (MoreSparkles.INSTANCE.globalBoost == null || !MoreSparkles.INSTANCE.getConfig().pausePlayerBoostsDuringGlobalBoost) {
            for (ShinyBoost boost : MoreSparkles.INSTANCE.activeBoosts) {
                ServerPlayerEntity player = MoreSparkles.INSTANCE.getServer().getPlayerManager().getPlayer(boost.player_uuid);
                if (player != null || !MoreSparkles.INSTANCE.getConfig().pausePlayerBoostsOnDisconnect) {
                    boost.time_remaining--;
                }
                if (boost.time_remaining <= 0) {
                    toRemove.add(boost);
                }
            }
        }

        if (MoreSparkles.INSTANCE.globalBoost != null) {
            MoreSparkles.INSTANCE.globalBoost.time_remaining--;
            if (MoreSparkles.INSTANCE.globalBoost.time_remaining <= 0) {
                MoreSparkles.INSTANCE.getAudiences().all().hideBossBar(MoreSparkles.INSTANCE.globalBoost.bossBar);
                MoreSparkles.INSTANCE.globalBoost = MoreSparkles.INSTANCE.queuedGlobalBoosts.poll();
                if (MoreSparkles.INSTANCE.globalBoost != null) {
                    MoreSparkles.INSTANCE.getAudiences().all().showBossBar(MoreSparkles.INSTANCE.globalBoost.bossBar);
                }
                MoreSparkles.INSTANCE.getConfig().saveGlobalBoostData();
            }
        }

        for (ShinyBoost boost : toRemove) {
            MoreSparkles.INSTANCE.activeBoosts.remove(boost);
            ServerPlayerEntity player = MoreSparkles.INSTANCE.getServer().getPlayerManager().getPlayer(boost.player_uuid);
            if (player != null) {
                player.hideBossBar(boost.bossBar);
                ShinyBoost nextBoost = MoreSparkles.INSTANCE.getNextQueuedBoost(player);
                if (nextBoost != null) {
                    MoreSparkles.INSTANCE.activeBoosts.add(nextBoost);
                    MoreSparkles.INSTANCE.queuedBoosts.remove(nextBoost);
                    player.showBossBar(nextBoost.bossBar);
                    PlayerDataManager.savePlayerBoostData(player);
                }
            }
        }
    }

    public static void updateBossbars() {
        for (ShinyBoost boost : MoreSparkles.INSTANCE.activeBoosts) {
            ServerPlayerEntity player = MoreSparkles.INSTANCE.getServer().getPlayerManager().getPlayer(boost.player_uuid);
            if (player != null) {
                if (MoreSparkles.INSTANCE.globalBoost == null || !MoreSparkles.INSTANCE.getConfig().pausePlayerBoostsDuringGlobalBoost) {
                    float progressRate = 1.0F / (boost.duration * 20L);
                    float total = progressRate * boost.time_remaining;

                    if (total < 0F)
                        total = 0F;
                    if (total > 1F)
                        total = 1F;

                    try {
                        if (boost.bossBar != null) {
                            boost.bossBar.progress(total);
                            boost.bossBar.name(boost.getBossBarText());
                        }
                    } catch (Exception e) {
                        MoreSparkles.INSTANCE.logError("[MoreSparkles] BossBar update failed: " + e.getMessage());
                        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                            MoreSparkles.INSTANCE.logError("  " + stackTraceElement.toString());
                        }
                    }
                } else {
                    boost.bossBar.name(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().playerBarTextPaused, boost)));
                }
            }
        }
        if (MoreSparkles.INSTANCE.globalBoost != null) {
            float progressRate = 1.0F / (MoreSparkles.INSTANCE.globalBoost.duration * 20L);
            float total = progressRate * MoreSparkles.INSTANCE.globalBoost.time_remaining;

            if (total < 0F)
                total = 0F;
            if (total > 1F)
                total = 1F;

            MoreSparkles.INSTANCE.globalBoost.bossBar.progress(total);
            MoreSparkles.INSTANCE.globalBoost.bossBar.name(MoreSparkles.INSTANCE.globalBoost.getBossBarText());
        }
    }

    public static void tickParticles() {
        particleCooldown--;
        if (particleCooldown <= 0) {
            particleCooldown = new Random().nextLong(2*20, 5*20);
            for (SparkleArea area : MoreSparkles.INSTANCE.getBoostAreasConfig().areas) {
                area.spawnRandomParticles();
            }
        }
    }
}
