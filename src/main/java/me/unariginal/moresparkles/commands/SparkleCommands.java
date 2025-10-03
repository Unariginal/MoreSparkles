package me.unariginal.moresparkles.commands;

import com.cobblemon.mod.common.Cobblemon;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.configs.PlayerDataManager;
import me.unariginal.moresparkles.data.SparkleArea;
import me.unariginal.moresparkles.data.ShinyBoost;
import me.unariginal.moresparkles.utils.TextUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class SparkleCommands {
    public SparkleCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            dispatcher.register(
                    CommandManager.literal("sparkles")
                            .then(
                                    CommandManager.literal("reload")
                                            .requires(Permissions.require("sparkles.reload", 4))
                                            .executes(ctx -> {
                                                MoreSparkles.INSTANCE.reload(true);
                                                ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("command_reload"))));
                                                return 1;
                                            })
                            )
                            .then(
                                    CommandManager.literal("boost")
                                            .requires(Permissions.require("sparkles.boost", true))
                                            .then(
                                                    CommandManager.literal("start")
                                                            .requires(Permissions.require("sparkles.boost.start", 4))
                                                            .then(
                                                                    CommandManager.argument("players", EntityArgumentType.players())
                                                                            .then(
                                                                                    CommandManager.argument("multiplier", FloatArgumentType.floatArg(1, Cobblemon.config.getShinyRate()))
                                                                                            .then(
                                                                                                    CommandManager.argument("duration", IntegerArgumentType.integer(1))
                                                                                                            .then(
                                                                                                                    CommandManager.argument("unit", StringArgumentType.string())
                                                                                                                            .suggests((ctx, builder) -> {
                                                                                                                                builder.suggest("seconds");
                                                                                                                                builder.suggest("minutes");
                                                                                                                                builder.suggest("hours");
                                                                                                                                builder.suggest("days");
                                                                                                                                return builder.buildFuture();
                                                                                                                            })
                                                                                                                            .executes(ctx -> {
                                                                                                                                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(ctx, "players");
                                                                                                                                float multiplier = FloatArgumentType.getFloat(ctx, "multiplier");
                                                                                                                                int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                                                                                                                String unit = StringArgumentType.getString(ctx, "unit");

                                                                                                                                int totalSeconds;
                                                                                                                                switch (unit) {
                                                                                                                                    case "minutes" -> totalSeconds = duration * 60;
                                                                                                                                    case "hours" -> totalSeconds = duration * 3600;
                                                                                                                                    case "days" -> totalSeconds = duration * 86400;
                                                                                                                                    default -> totalSeconds = duration;
                                                                                                                                }

                                                                                                                                for (ServerPlayerEntity player : players) {
                                                                                                                                    ShinyBoost shinyBoost = MoreSparkles.INSTANCE.getActiveBoost(player);
                                                                                                                                    ShinyBoost newShinyBoost = new ShinyBoost(player, multiplier, totalSeconds);
                                                                                                                                    if (shinyBoost != null && MoreSparkles.INSTANCE.getConfig().allowQueuedBoosts) {
                                                                                                                                        MoreSparkles.INSTANCE.queuedBoosts.add(newShinyBoost);
                                                                                                                                        ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("player_boost_added_to_queue"), newShinyBoost)));
                                                                                                                                    } else {
                                                                                                                                        MoreSparkles.INSTANCE.activeBoosts.add(newShinyBoost);
                                                                                                                                        player.showBossBar(newShinyBoost.bossBar);
                                                                                                                                        ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("player_boost_started"), newShinyBoost)));
                                                                                                                                    }
                                                                                                                                    PlayerDataManager.savePlayerBoostData(player);
                                                                                                                                }
                                                                                                                                return 1;
                                                                                                                            })
                                                                                                            )
                                                                                            )
                                                                            )
                                                            )
                                                            .then(
                                                                    CommandManager.literal("global")
                                                                            .then(
                                                                                    CommandManager.argument("multiplier", FloatArgumentType.floatArg(1, Cobblemon.config.getShinyRate()))
                                                                                            .then(
                                                                                                    CommandManager.argument("duration", IntegerArgumentType.integer(1))
                                                                                                            .then(
                                                                                                                    CommandManager.argument("unit", StringArgumentType.string())
                                                                                                                            .suggests((ctx, builder) -> {
                                                                                                                                builder.suggest("seconds");
                                                                                                                                builder.suggest("minutes");
                                                                                                                                builder.suggest("hours");
                                                                                                                                builder.suggest("days");
                                                                                                                                return builder.buildFuture();
                                                                                                                            })
                                                                                                                            .executes(ctx -> {
                                                                                                                                float multiplier = FloatArgumentType.getFloat(ctx, "multiplier");
                                                                                                                                int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                                                                                                                String unit = StringArgumentType.getString(ctx, "unit");

                                                                                                                                int totalSeconds;
                                                                                                                                switch (unit) {
                                                                                                                                    case "minutes" -> totalSeconds = duration * 60;
                                                                                                                                    case "hours" -> totalSeconds = duration * 3600;
                                                                                                                                    case "days" -> totalSeconds = duration * 86400;
                                                                                                                                    default -> totalSeconds = duration;
                                                                                                                                }

                                                                                                                                if (MoreSparkles.INSTANCE.globalBoost == null) {
                                                                                                                                    MoreSparkles.INSTANCE.globalBoost = new ShinyBoost(null, multiplier, totalSeconds);
                                                                                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("global_boost_started"), MoreSparkles.INSTANCE.globalBoost)));
                                                                                                                                    MoreSparkles.INSTANCE.getAudiences().all().showBossBar(MoreSparkles.INSTANCE.globalBoost.bossBar);
                                                                                                                                } else if (MoreSparkles.INSTANCE.getConfig().allowQueuedBoosts) {
                                                                                                                                    ShinyBoost boost = new ShinyBoost(null, multiplier, totalSeconds);
                                                                                                                                    MoreSparkles.INSTANCE.queuedGlobalBoosts.add(boost);
                                                                                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("global_boost_added_to_queue"), boost)));
                                                                                                                                }
                                                                                                                                MoreSparkles.INSTANCE.getConfig().saveGlobalBoostData();
                                                                                                                                return 1;
                                                                                                                            })
                                                                                                            )
                                                                                            )
                                                                            )
                                                            )
                                            )
                                            .then(
                                                    CommandManager.literal("stop")
                                                            .requires(Permissions.require("sparkles.boost.stop", 4))
                                                            .then(
                                                                    CommandManager.argument("players", EntityArgumentType.players())
                                                                            .executes(ctx -> {
                                                                                for (ServerPlayerEntity player : EntityArgumentType.getPlayers(ctx, "players")) {
                                                                                    ShinyBoost boost = MoreSparkles.INSTANCE.getActiveBoost(player);
                                                                                    if (boost != null) {
                                                                                        player.hideBossBar(boost.bossBar);
                                                                                        ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("player_boost_stopped"), boost)));
                                                                                        MoreSparkles.INSTANCE.activeBoosts.remove(boost);
                                                                                        ShinyBoost nextBoost = MoreSparkles.INSTANCE.getNextQueuedBoost(player);
                                                                                        if (nextBoost != null) {
                                                                                            MoreSparkles.INSTANCE.activeBoosts.add(nextBoost);
                                                                                            MoreSparkles.INSTANCE.queuedBoosts.remove(nextBoost);
                                                                                            player.showBossBar(nextBoost.bossBar);
                                                                                        }
                                                                                    }
                                                                                    PlayerDataManager.savePlayerBoostData(player);
                                                                                }
                                                                                return 1;
                                                                            })
                                                            )
                                                            .then(
                                                                    CommandManager.literal("global")
                                                                            .executes(ctx -> {
                                                                                ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("global_boost_stopped"), MoreSparkles.INSTANCE.globalBoost)));
                                                                                MoreSparkles.INSTANCE.globalBoost.timeRemaining = 1;
                                                                                return 1;
                                                                            })
                                                            )
                                            )
                                            .then(
                                                    CommandManager.literal("status")
                                                            .requires(Permissions.require("sparkles.status", true))
                                                            .executes(ctx -> {
                                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                                if (player != null) {
                                                                    ShinyBoost boost = MoreSparkles.INSTANCE.getActiveBoost(player);
                                                                    if (boost != null)
                                                                        ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("player_boost_information"), boost)));
                                                                    else
                                                                        ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("no_active_boosts"))));
                                                                }
                                                                return 1;
                                                            })
                                                            .then(
                                                                    CommandManager.literal("global")
                                                                            .requires(Permissions.require("sparkles.status.global", true))
                                                                            .executes(ctx -> {
                                                                                if (MoreSparkles.INSTANCE.globalBoost != null)
                                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("global_boost_information"), MoreSparkles.INSTANCE.globalBoost)));
                                                                                else
                                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("no_active_boosts"))));
                                                                                return 1;
                                                                            })
                                                            )
                                                            .then(
                                                                    CommandManager.argument("player", EntityArgumentType.player())
                                                                            .requires(Permissions.require("sparkles.status.others", 4))
                                                                            .executes(ctx -> {
                                                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                                                                                ShinyBoost boost = MoreSparkles.INSTANCE.getActiveBoost(player);
                                                                                if (boost != null)
                                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("player_boost_information"), boost)));
                                                                                else
                                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("no_active_boosts"))));
                                                                                return 1;
                                                                            })
                                                            )
                                            )
                            )
                            .then(
                                    CommandManager.literal("check-rate")
                                            .requires(Permissions.require("sparkles.checkrate", true))
                                            .executes(ctx -> {
                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                if (player != null)
                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("command_check_rate"), player)));
                                                return 1;
                                            })
                                            .then(
                                                    CommandManager.argument("player", EntityArgumentType.player())
                                                            .requires(Permissions.require("sparkles.checkrate.others", 4))
                                                            .executes(ctx -> {
                                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                                                                if (player != null)
                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("command_check_rate"), player)));
                                                                return 1;
                                                            })
                                            )
                            )
                            .then(
                                    CommandManager.literal("clear-queue")
                                            .requires(Permissions.require("sparkles.clearqueue", 4))
                                            .then(
                                                    CommandManager.literal("global")
                                                            .executes(ctx -> {
                                                                MoreSparkles.INSTANCE.queuedGlobalBoosts.clear();
                                                                ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("global_queue_cleared"))));
                                                                MoreSparkles.INSTANCE.getConfig().saveGlobalBoostData();
                                                                return 1;
                                                            })
                                            )
                                            .then (
                                                    CommandManager.argument("players", EntityArgumentType.players())
                                                            .executes(ctx -> {
                                                                for (ServerPlayerEntity player : EntityArgumentType.getPlayers(ctx, "players")) {
                                                                    MoreSparkles.INSTANCE.clearQueue(player);
                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("player_queue_cleared"), player)));
                                                                    PlayerDataManager.savePlayerBoostData(player);
                                                                }
                                                                return 1;
                                                            })
                                            )
                            )
                            .then(
                                    CommandManager.literal("check-queue")
                                            .requires(Permissions.require("sparkles.checkqueue", true))
                                            .executes(ctx -> {
                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                if (player != null) {
                                                    if (MoreSparkles.INSTANCE.getQueuedBoosts(player).isEmpty()) {
                                                        ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("no_queued_boosts"), player)));
                                                        return 1;
                                                    }
                                                    for (ShinyBoost queuedBoost : MoreSparkles.INSTANCE.getQueuedBoosts(player)) {
                                                        player.sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("player_boost_information"), queuedBoost)));
                                                    }
                                                }
                                                return 1;
                                            })
                                            .then(
                                                    CommandManager.literal("global")
                                                            .requires(Permissions.require("sparkles.checkqueue.global", 4))
                                                            .executes(ctx -> {
                                                                if (MoreSparkles.INSTANCE.queuedGlobalBoosts.isEmpty()) {
                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("no_queued_boosts"))));
                                                                    return 1;
                                                                }
                                                                for (ShinyBoost queuedBoost : MoreSparkles.INSTANCE.queuedGlobalBoosts) {
                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("global_boost_information"), queuedBoost)));
                                                                }
                                                                return 1;
                                                            })
                                            )
                                            .then(
                                                    CommandManager.argument("player", EntityArgumentType.player())
                                                            .requires(Permissions.require("sparkles.checkqueue.others", 4))
                                                            .executes(ctx -> {
                                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                                                                if (MoreSparkles.INSTANCE.getQueuedBoosts(player).isEmpty()) {
                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("no_queued_boosts"), player)));
                                                                    return 1;
                                                                }
                                                                for (ShinyBoost queuedBoost : MoreSparkles.INSTANCE.getQueuedBoosts(player)) {
                                                                    ctx.getSource().sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("player_boost_information"), queuedBoost)));
                                                                }
                                                                return 1;
                                                            })
                                            )
                            )
                            .then(
                                    CommandManager.literal("area")
                                            .requires(Permissions.require("sparkles.area", 4))
                                            .then(
                                                    CommandManager.literal("info")
                                                            .requires(Permissions.require("sparkles.area.info", 4))
                                                            .executes(ctx -> {
                                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                                if (player != null) {
                                                                    boolean inArea = false;
                                                                    for (SparkleArea area : MoreSparkles.INSTANCE.getBoostAreasConfig().areas) {
                                                                        if (area.isInArea(player.getServerWorld(), player.getX(), player.getY(), player.getZ())) {
                                                                            player.sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("area_info"), area)));
                                                                            inArea = true;
                                                                        }
                                                                    }
                                                                    if (!inArea) {
                                                                        player.sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("not_in_area"))));
                                                                    }
                                                                }
                                                                return 1;
                                                            })
                                                            .then(
                                                                    CommandManager.argument("id", StringArgumentType.string())
                                                                            .suggests((ctx, builder) -> {
                                                                                for (SparkleArea area : MoreSparkles.INSTANCE.getBoostAreasConfig().areas) {
                                                                                    builder.suggest(area.id);
                                                                                }
                                                                                return builder.buildFuture();
                                                                            })
                                                                            .executes(ctx -> {
                                                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                                                String id = StringArgumentType.getString(ctx, "id");
                                                                                if (player != null) {
                                                                                    SparkleArea area = MoreSparkles.INSTANCE.getBoostAreasConfig().getBoostArea(id);
                                                                                    if (area != null) {
                                                                                        player.sendMessage(TextUtils.deserialize(TextUtils.parse(MoreSparkles.INSTANCE.getMessages().getMessage("area_info"), area)));
                                                                                    }
                                                                                }
                                                                                return 1;
                                                                            })
                                                            )
                                            )
                            )
            );
        });
    }
}
