package me.unariginal.moresparkles.commands.command;

import com.cobblemon.mod.common.Cobblemon;
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
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.managers.BoostManager;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

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
                        .then(argument("multiplier", FloatArgumentType.floatArg(1, Cobblemon.config.getShinyRate()))
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
                                                        .executes(SparklesBoostStartCommand::execute))
                                                .then(literal("global")
                                                        .executes(SparklesBoostStartCommand::execute))))));

    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        String boostTypeName = StringArgumentType.getString(ctx, "type");
        float multiplier = FloatArgumentType.getFloat(ctx, "multiplier");
        int duration = IntegerArgumentType.getInteger(ctx, "duration");
        String unit = StringArgumentType.getString(ctx, "unit");

        BoostType boostType = BoostType.valueOf(boostTypeName);

        int totalSeconds = switch (unit) {
            case "minutes" -> duration * 60;
            case "hours" -> duration * 3600;
            case "days" -> duration * 86400;
            default -> duration;
        };

        AtomicReference<Collection<ServerPlayerEntity>> playersReference = new AtomicReference<>(null);
        try {
            playersReference.set(EntityArgumentType.getPlayers(ctx, "players"));
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {}

        if (playersReference.get() != null) {
            for (ServerPlayerEntity player : playersReference.get()) {
                Boost activeBoost = PlayerBoostCache.currentBoost(player, boostType);
                Boost newBoost = new Boost(false, boostType, multiplier, totalSeconds);
                if (activeBoost != null && CONFIG.allowQueuedBoosts) {
                    PlayerBoostQueueCache.queueBoost(player, newBoost);
                    ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.playerBoostAddedToQueue, ParseContext.builder().player(player).boost(newBoost).build()));
                } else {
                    // We override the current boost if we don't allow queues
                    PlayerBoostCache.add(player, newBoost);
                    if (newBoost.bossBar != null) player.showBossBar(newBoost.bossBar);
                    ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.playerBoostStarted, ParseContext.builder().player(player).boost(newBoost).build()));
                }
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
                BoostManager.globalBoosts.put(boostType, newBoost);
                if (CONFIG.pausePlayerBoostsDuringGlobalBoost) BoostManager.pausePlayerBoosts(boostType);
                if (newBoost.bossBar != null) MoreSparkles.INSTANCE.audiences.all().showBossBar(newBoost.bossBar);
                ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.globalBoostStarted, ParseContext.builder().boost(newBoost).build()));
            }
        }

        return 1;
    }
}
