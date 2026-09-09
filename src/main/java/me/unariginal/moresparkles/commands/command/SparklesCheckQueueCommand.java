package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.cache.PlayerBoostQueueCache;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.managers.BoostManager;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesCheckQueueCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("check-queue")
                .requires(Permissions.require("sparkles.checkqueue", true))
                .executes(ctx -> execute(ctx, false))
                .then(literal("global")
                        .requires(Permissions.require("sparkles.checkqueue.global", 4))
                        .executes(ctx -> execute(ctx, true))
                )
                .then(argument("player", EntityArgumentType.player())
                        .requires(Permissions.require("sparkles.checkqueue.others", 4))
                        .executes(ctx -> execute(ctx, false))
                );
    }

    private static int execute(CommandContext<ServerCommandSource> ctx, boolean global) {
        if (global) {
            if (BoostManager.queuedGlobalBoosts == null || BoostManager.queuedGlobalBoosts.isEmpty()) {
                ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.noQueuedBoosts));
            } else {
                for (Map.Entry<BoostType, Queue<Boost>> queueEntry : BoostManager.queuedGlobalBoosts.entrySet()) {
                    for (Boost queuedBoost : queueEntry.getValue()) {
                        ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.globalBoostInformation, ParseContext.builder().boost(queuedBoost).build()));
                    }
                }
            }
        } else {
            AtomicReference<ServerPlayerEntity> playerReference = new AtomicReference<>(ctx.getSource().getPlayer());
            try {
                playerReference.set(EntityArgumentType.getPlayer(ctx, "player"));
            } catch (IllegalArgumentException | CommandSyntaxException ignored) {
            }
            if (playerReference.get() != null) {
                Map<BoostType, Queue<Boost>> queuedBoostsMap = PlayerBoostQueueCache.currentQueuedBoosts(playerReference.get());
                if (queuedBoostsMap != null && !queuedBoostsMap.isEmpty()) {
                    for (Map.Entry<BoostType, Queue<Boost>> queueEntry : queuedBoostsMap.entrySet()) {
                        for (Boost queuedBoost : queueEntry.getValue()) {
                            ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.playerBoostInformation, ParseContext.builder().player(playerReference.get()).boost(queuedBoost).build()));
                        }
                    }
                } else {
                    ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.noQueuedBoosts, ParseContext.builder().player(playerReference.get()).build()));
                }
            }
        }
        return 1;
    }
}
