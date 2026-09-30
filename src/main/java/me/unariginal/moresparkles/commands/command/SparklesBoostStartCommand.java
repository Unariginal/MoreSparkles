package me.unariginal.moresparkles.commands.command;

import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.cache.PlayerBoostQueueCache;
import me.unariginal.moresparkles.configs.GlobalBoostDataManager;
import me.unariginal.moresparkles.configs.PlayerDataManager;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.managers.BoostManager;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Queue;

import static me.unariginal.moresparkles.configs.ConfigManager.CONFIG;
import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesBoostStartCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("start")
                .requires(Permissions.require("sparkles.boost.start", 4))
                .then(argument("type", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            for (BoostType type : BoostType.values()) {
                                builder.suggest(type.name());
                            }
                            return builder.buildFuture();
                        })
                        .then(argument("multiplier", FloatArgumentType.floatArg(1))
                                .then(argument("duration", IntegerArgumentType.integer(1))
                                        .then(argument("unit", StringArgumentType.string())
                                                .suggests((ctx, builder) -> {
                                                    builder.suggest("seconds");
                                                    builder.suggest("minutes");
                                                    builder.suggest("hours");
                                                    builder.suggest("days");
                                                    return builder.buildFuture();
                                                })
                                                .then(argument("players", EntityArgumentType.players())
                                                        .executes(ctx -> execute(ctx, false)))
                                                .then(literal("global")
                                                        .executes(ctx -> execute(ctx, true)))))));

    }

    private static int execute(CommandContext<ServerCommandSource> ctx, boolean global) throws CommandSyntaxException {
        String boostTypeName = StringArgumentType.getString(ctx, "type");
        float multiplier = FloatArgumentType.getFloat(ctx, "multiplier");
        int duration = IntegerArgumentType.getInteger(ctx, "duration");
        String unit = StringArgumentType.getString(ctx, "unit");

        BoostType boostType = BoostType.valueOf(boostTypeName);

        long totalSeconds = switch (unit) {
            case "minutes" -> duration * 60L;
            case "hours" -> duration * 3600L;
            case "days" -> duration * 86400L;
            default -> duration;
        };

        if (!global) {
            for (ServerPlayerEntity player : EntityArgumentType.getPlayers(ctx, "players")) {
                Boost activeBoost = PlayerBoostCache.currentBoost(player, boostType);
                Boost newBoost = new Boost(false, boostType, multiplier, totalSeconds);
                if (activeBoost != null && CONFIG.allowQueuedBoosts) {
                    PlayerBoostQueueCache.queueBoost(player, newBoost);
                    ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.playerBoostAddedToQueue, ParseContext.builder().player(player).boost(newBoost).build()));
                } else {
                    if (activeBoost != null && activeBoost.bossBar != null) player.hideBossBar(activeBoost.bossBar);
                    PlayerBoostCache.add(player, newBoost);
                    if (BoostManager.shouldPausePlayerBoosts(boostType)) newBoost.pause();
                    if (newBoost.bossBar != null) player.showBossBar(newBoost.bossBar);
                    ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.playerBoostStarted, ParseContext.builder().player(player).boost(newBoost).build()));
                }
                PlayerDataManager.savePlayerBoostData(player);
            }
        } else {
            Boost newBoost = new Boost(true, boostType, multiplier, totalSeconds);
            if (BoostManager.globalBoosts.get(boostType) != null && CONFIG.allowQueuedBoosts) {
                Queue<Boost> globalBoostQueue = BoostManager.queuedGlobalBoosts.get(boostType);
                if (globalBoostQueue == null) globalBoostQueue = Queues.newConcurrentLinkedQueue();
                globalBoostQueue.add(newBoost);
                BoostManager.queuedGlobalBoosts.put(boostType, globalBoostQueue);
                ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.globalBoostAddedToQueue, ParseContext.builder().boost(newBoost).build()));
            } else {
                Boost replacedBoost = BoostManager.globalBoosts.put(boostType, newBoost);
                if (replacedBoost != null && replacedBoost.bossBar != null) MoreSparkles.INSTANCE.audiences.all().hideBossBar(replacedBoost.bossBar);
                if (CONFIG.pausePlayerBoostsDuringGlobalBoost) BoostManager.pausePlayerBoosts(boostType);
                if (newBoost.bossBar != null) MoreSparkles.INSTANCE.audiences.all().showBossBar(newBoost.bossBar);
                ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.globalBoostStarted, ParseContext.builder().boost(newBoost).build()));
            }
            GlobalBoostDataManager.saveGlobalBoostData();
        }

        return 1;
    }
}
