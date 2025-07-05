package me.unariginal.moresparkles;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.api.reactive.ObservableSubscription;
import com.cobblemon.mod.common.pokemon.helditem.CobblemonHeldItemManager;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import kotlin.Unit;
import me.unariginal.moresparkles.commands.SparkleCommands;
import me.unariginal.moresparkles.configs.*;
import me.unariginal.moresparkles.data.SparkleArea;
import me.unariginal.moresparkles.data.ShinyBoost;
import me.unariginal.moresparkles.items.ShinyCharms;
import me.unariginal.moresparkles.managers.TickManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MoreSparkles implements ModInitializer {
    public static final String MOD_ID = "moresparkles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean DEBUG = false;

    public static MoreSparkles INSTANCE;

    private MinecraftServer server;
    private FabricServerAudiences audiences;

    private Config config;
    private MessagesConfig messages = new MessagesConfig();
    private SparkleAreasConfig boostAreasConfig;
    private ItemsConfig itemsConfig = new ItemsConfig();

    public ShinyBoost globalBoost = null;
    public Queue<ShinyBoost> queuedGlobalBoosts = new ConcurrentLinkedQueue<>();
    public List<ShinyBoost> activeBoosts = new ArrayList<>();
    public List<ShinyBoost> queuedBoosts = new ArrayList<>();

    private ObservableSubscription<ShinyChanceCalculationEvent> shinySubscription = null;

    @Override
    public void onInitialize() {
        INSTANCE = this;

        new SparkleCommands();

        if (isPolymerEnabled()) {
            PolymerResourcePackUtils.markAsRequired();
            PolymerResourcePackUtils.addModAssets(MOD_ID);

            ShinyCharms.INSTANCE.fillPolymerModelData();
            ShinyCharms.INSTANCE.fillPolymerItems();

            Item icon = Items.NETHER_STAR;
            if (ShinyCharms.INSTANCE.shinyCharmPolymerItems.containsKey("shiny_charm"))
                icon = ShinyCharms.INSTANCE.shinyCharmPolymerItems.get("shiny_charm");

            ItemGroup moreSparklesGroup = FabricItemGroup.builder()
                    .icon(icon::getDefaultStack)
                    .displayName(Text.literal("More Sparkles"))
                    .entries((displayContext, entries) -> {
                        for (String key : ShinyCharms.INSTANCE.shinyCharmPolymerItems.keySet()) {
                            entries.add(ShinyCharms.INSTANCE.shinyCharmPolymerItems.get(key));
                        }
                    }).build();

            PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID, "more_sparkles"), moreSparklesGroup);
        } else {
            logError("[MoreSparkles] Polymer is not found. Items will not be loaded!");
        }

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.server = server;
            this.audiences = FabricServerAudiences.of(server);
            reload(false);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
             shinySubscription = CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe(Priority.NORMAL, event -> {
                if (globalBoost == null || !config.pausePlayerBoostsDuringGlobalBoost) {
                    for (ShinyBoost shinyBoost : activeBoosts) {
                        event.addModificationFunction(((rate, player, pokemon) -> {
                            if (player != null) {
                                if (shinyBoost.player_uuid.equals(player.getUuid())) {
                                    return Math.max(rate / shinyBoost.multiplier, 1);
                                }
                            }
                            return rate;
                        }));
                    }
                }

                if (globalBoost != null) {
                    event.addModificationFunction(((rate, player, pokemon) -> Math.max(rate / globalBoost.multiplier, 1)));
                }

                for (SparkleArea boostArea : boostAreasConfig.areas) {
                    event.addModificationFunction((rate, player, pokemon) -> {
                        if (player != null) {
                            if (boostArea.isInArea(player.getServerWorld(), player.getX(), player.getY(), player.getZ())) {
                                return Math.max(rate / boostArea.multiplier, 1);
                            }
                        }
                        return rate;
                    });
                }

                 event.addModificationFunction((rate, player, pokemon) -> {
                     if (player != null) {
                         for (String key : ShinyCharms.INSTANCE.shinyCharmPolymerItems.keySet()) {
                             if (player.getInventory().contains(ShinyCharms.INSTANCE.getShinyCharm(key))) {
                                 rate = Math.max(rate / ShinyCharms.INSTANCE.shinyCharmPolymerItems.get(key).getMultiplier(), 1);
                             }
                         }
                     }
                     return rate;
                 });

                return Unit.INSTANCE;
            });
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TickManager.tickBoosts();
            TickManager.updateBossbars();
            TickManager.tickParticles();
        });

        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, minecraftServer) -> {
            ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
            ShinyBoost boost = getActiveBoost(player);
            if (boost != null)
                player.hideBossBar(boost.bossBar);
            if (globalBoost != null)
                player.hideBossBar(globalBoost.bossBar);
            PlayerDataManager.savePlayerBoostData(player);
        });

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, sender, minecraftServer) -> {
            ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
            ShinyBoost boost = PlayerDataManager.loadPlayerBoostData(player);
            if (boost != null) {
                activeBoosts.add(boost);
            }
            if (globalBoost != null) {
                player.showBossBar(globalBoost.bossBar);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (ShinyBoost shinyBoost : activeBoosts) {
                PlayerDataManager.savePlayerBoostData(shinyBoost);
            }
            activeBoosts.clear();
            queuedBoosts.clear();
            globalBoost = null;
            queuedGlobalBoosts.clear();
            if (shinySubscription != null)
                shinySubscription.unsubscribe();
        });
    }

    public boolean isPolymerEnabled() {
        return (FabricLoader.getInstance().isModLoaded("polymer-bundled") ||
                (FabricLoader.getInstance().isModLoaded("polymer-common") && FabricLoader.getInstance().isModLoaded("polymer-core") && FabricLoader.getInstance().isModLoaded("polymer-autohost") && FabricLoader.getInstance().isModLoaded("polymer-resource-pack")));
    }

    public static void logInfo(String info) {
        if (DEBUG) {
            LOGGER.info(info);
        }
    }

    public static void logError(String error) {
        LOGGER.error(error);
    }

    public void reload(boolean fromCommand) {
        if (fromCommand) {
            for (ShinyBoost shinyBoost : activeBoosts) {
                PlayerDataManager.savePlayerBoostData(shinyBoost);
            }
            config.saveGlobalBoostData();
        }

        this.messages = new MessagesConfig();
        this.boostAreasConfig = new SparkleAreasConfig();
        this.itemsConfig = new ItemsConfig();
        this.config = new Config();
    }

    public Config getConfig() {
        return this.config;
    }

    public MessagesConfig getMessages() {
        return this.messages;
    }

    public SparkleAreasConfig getBoostAreasConfig() {
        return this.boostAreasConfig;
    }

    public ItemsConfig getItemsConfig() {
        return this.itemsConfig;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public FabricServerAudiences getAudiences() {
        return audiences;
    }

    public ShinyBoost getActiveBoost(ServerPlayerEntity player) {
        for (ShinyBoost boost : activeBoosts) {
            if (boost.player_uuid.equals(player.getUuid())) {
                return boost;
            }
        }
        return null;
    }

    public ShinyBoost getNextQueuedBoost(ServerPlayerEntity player) {
        for (ShinyBoost boost : queuedBoosts) {
            if (boost.player_uuid.equals(player.getUuid())) {
                return boost;
            }
        }
        return null;
    }

    public List<ShinyBoost> getQueuedBoosts(ServerPlayerEntity player) {
        List<ShinyBoost> boosts = new ArrayList<>();
        for (ShinyBoost boost : queuedBoosts) {
            if (boost.player_uuid.equals(player.getUuid())) {
                boosts.add(boost);
            }
        }
        return boosts;
    }

    public List<ShinyBoost> getQueuedBoosts(UUID uuid) {
        List<ShinyBoost> boosts = new ArrayList<>();
        for (ShinyBoost boost : queuedBoosts) {
            if (boost.player_uuid.equals(uuid)) {
                boosts.add(boost);
            }
        }
        return boosts;
    }

    public void clearQueue(ServerPlayerEntity player) {
        List<ShinyBoost> toRemove = new ArrayList<>();
        for (ShinyBoost boost : queuedBoosts) {
            if (boost.player_uuid.equals(player.getUuid())) {
                toRemove.add(boost);
            }
        }
        for (ShinyBoost boost : toRemove) {
            queuedBoosts.remove(boost);
        }
    }
}
