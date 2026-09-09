package me.unariginal.moresparkles;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.cache.PlayerBoostQueueCache;
import me.unariginal.moresparkles.commands.SparkleCommands;
import me.unariginal.moresparkles.configs.*;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.items.CharmItemsGroup;
import me.unariginal.moresparkles.managers.BoostManager;
import me.unariginal.moresparkles.managers.EventManager;
import me.unariginal.moresparkles.managers.ScheduledTimerHandler;
import me.unariginal.moresparkles.managers.TickManager;
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

        CommandRegistrationCallback.EVENT.register(SparkleCommands::register);

        if (isPolymerEnabled()) {
            PolymerResourcePackUtils.markAsRequired();
            PolymerResourcePackUtils.addModAssets(MOD_ID);

            CharmItemsGroup.registerItemGroup();
        } else {
            LOGGER.warn("[MoreSparkles] Polymer is not found; Items will not be loaded!");
        }

        new ScheduledTimerHandler();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.server = server;
            this.audiences = FabricServerAudiences.of(server);
            reload(false);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> EventManager.register());

        ServerTickEvents.END_SERVER_TICK.register(server -> TickManager.tickParticles());

        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, minecraftServer) -> {
            ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
            if (CONFIG.pausePlayerBoostsOnDisconnect) PlayerBoostCache.pauseAll(player);
            PlayerDataManager.savePlayerBoostData(player);
            PlayerBoostCache.removeAll(player);
            PlayerBoostQueueCache.remove(player);
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

    public void reload(boolean fromCommand) {
        if (fromCommand) {
            Config.saveGlobalBoostData();
        }

        load();
    }
}
