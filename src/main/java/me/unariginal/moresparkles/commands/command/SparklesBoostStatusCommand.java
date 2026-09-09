package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.managers.BoostManager;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesBoostStatusCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("status")
                .requires(Permissions.require("sparkles.boost.status", true))
                .executes(ctx -> execute(ctx, false))
                .then(
                        CommandManager.literal("global")
                                .requires(Permissions.require("sparkles.boost.status.global", true))
                                .executes(ctx -> execute(ctx, true))
                )
                .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                                .requires(Permissions.require("sparkles.boost.status.others", 4))
                                .executes(ctx -> execute(ctx, false))
                );
    }

    private static int execute(CommandContext<ServerCommandSource> ctx, boolean global) {
        if (global) {
            Map<BoostType, Boost> boostMap = BoostManager.globalBoosts;
            if (boostMap != null && !boostMap.isEmpty()) {
                for (Map.Entry<BoostType, Boost> boostEntry : boostMap.entrySet()) {
                    ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.globalBoostInformation, ParseContext.builder().boost(boostEntry.getValue()).build()));
                }
            }
            else ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.noActiveBoosts));
        } else {
            AtomicReference<ServerPlayerEntity> playerReference = new AtomicReference<>(ctx.getSource().getPlayer());
            try {
                playerReference.set(EntityArgumentType.getPlayer(ctx, "player"));
            } catch (IllegalArgumentException | CommandSyntaxException ignored) {
            }

            if (playerReference.get() != null) {
                Map<BoostType, Boost> boostMap = PlayerBoostCache.currentBoosts(playerReference.get());
                if (boostMap != null && !boostMap.isEmpty()) {
                    for (Map.Entry<BoostType, Boost> boostEntry : boostMap.entrySet()) {
                        ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.playerBoostInformation, ParseContext.builder().player(playerReference.get()).boost(boostEntry.getValue()).build()));
                    }
                } else
                    ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.noActiveBoosts, ParseContext.builder().player(playerReference.get()).build()));
            }
        }
        return 1;
    }
}
