package me.unariginal.moresparkles;

import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawnerFactory;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import kotlin.jvm.functions.Function1;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.cache.PlayerBoostQueueCache;
import me.unariginal.moresparkles.commands.SparkleCommands;
import me.unariginal.moresparkles.configs.*;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.items.CharmItemsGroup;
import me.unariginal.moresparkles.managers.*;
import me.unariginal.moresparkles.spawning.influences.AlphaSpawningInfluence;
import me.unariginal.moresparkles.spawning.influences.SpawnBucketInfluence;
import me.unariginal.moresparkles.utils.Threading;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.unariginal.moresparkles.configs.ConfigManager.*;

public class MoreSparkles implements ModInitializer {
    public static final String MOD_ID = "moresparkles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static MoreSparkles INSTANCE;

    public MinecraftServer server;
    public FabricServerAudiences audiences;

    @Override
    public void onInitialize() {
        INSTANCE = this;
        load();
        CommandRegistrationCallback.EVENT.register(SparkleCommands::register);

        if (isPolymerEnabled()) {
            PolymerResourcePackUtils.markAsRequired();
            PolymerResourcePackUtils.addModAssets(MOD_ID);

            CharmItemsGroup.registerItemGroup();
        } else {
            LOGGER.warn("[MoreSparkles] Polymer is not found; Items will not be loaded!");
        }

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.server = server;
            this.audiences = FabricServerAudiences.of(server);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WebhookManager.connectWebhook();
            BoostManager.loadFromConfig();
            new ScheduledTimerHandler();
            EventManager.register();

            List<Function1<ServerPlayerEntity, SpawningInfluence>> builders = new ArrayList<>(PlayerSpawnerFactory.INSTANCE.getInfluenceBuilders());
            builders.add(AlphaSpawningInfluence::new);
            builders.add(SpawnBucketInfluence::new);
            PlayerSpawnerFactory.INSTANCE.setInfluenceBuilders(builders);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> TickManager.tickParticles());

        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, minecraftServer) -> {
            ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
            if (CONFIG.pausePlayerBoostsOnDisconnect) PlayerBoostCache.pauseAll(player);
            PlayerDataManager.savePlayerBoostData(player);
            PlayerBoostCache.removeAll(player);
            PlayerBoostQueueCache.remove(player);
            if (BoostManager.globalBoosts != null) {
                for (Boost boost : BoostManager.globalBoosts.values()) {
                    if (boost.bossBar != null) player.hideBossBar(boost.bossBar);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, sender, minecraftServer) -> {
            ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
            PlayerDataManager.loadPlayerBoostData(player);
            Map<BoostType, Boost> boostMap = PlayerBoostCache.currentBoosts(player);
            if (boostMap != null) {
                boostMap.values().forEach(boost -> {
                    if (boost.bossBar != null) player.showBossBar(boost.bossBar);
                });
            }

            if (BoostManager.globalBoosts != null) {
                BoostManager.globalBoosts.values().forEach(boost -> {
                    Boost playerBoost = PlayerBoostCache.currentBoost(player, boost.boostType);
                    if (playerBoost != null) playerBoost.pause();
                    if (boost.bossBar != null) player.showBossBar(boost.bossBar);
                });
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (CONFIG.pauseGlobalBoostsOnShutdown && BoostManager.globalBoosts != null) {
                BoostManager.globalBoosts.values().forEach(Boost::pause);
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (CONFIG.pausePlayerBoostsOnShutdown) PlayerBoostCache.pauseAll(player);
                PlayerDataManager.savePlayerBoostData(player);
            }
            Config.saveGlobalBoostData();
            Threading.shutdown();
            if (WebhookManager.webhook != null) WebhookManager.webhook.close();
        });
    }

    public boolean isPolymerEnabled() {
        return (FabricLoader.getInstance().isModLoaded("polymer-bundled") ||
                (FabricLoader.getInstance().isModLoaded("polymer-common") && FabricLoader.getInstance().isModLoaded("polymer-core") && FabricLoader.getInstance().isModLoaded("polymer-autohost") && FabricLoader.getInstance().isModLoaded("polymer-resource-pack")));
    }

    public static void logInfo(String info) {
        if (CONFIG.debug) {
            LOGGER.info("[MoreSparkles] {}", info);
        }
    }

    public static void logError(String error) {
        LOGGER.error("[MoreSparkles] {}", error);
    }

    public void reload() {
        load();
        WebhookManager.connectWebhook();
        Config.saveGlobalBoostData();
    }
}
