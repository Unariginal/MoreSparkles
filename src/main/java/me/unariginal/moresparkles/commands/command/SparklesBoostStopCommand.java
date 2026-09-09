package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import static me.unariginal.moresparkles.configs.ConfigManager.CONFIG;
import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesBoostStopCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("stop")
                .requires(Permissions.require("sparkles.boost.stop", 4))
                .then(argument("type", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            for (BoostType type : BoostType.values()) {
                                builder.suggest(type.name());
                            }
                            return builder.buildFuture();
                        })
                        .then(argument("players", EntityArgumentType.players())
                                .executes(SparklesBoostStopCommand::execute))
                        .then(literal("global")
                                .executes(SparklesBoostStopCommand::execute)));

    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        String boostTypeName = StringArgumentType.getString(ctx, "type");
        BoostType boostType = BoostType.valueOf(boostTypeName);

        AtomicReference<Collection<ServerPlayerEntity>> playersReference = new AtomicReference<>(null);
        try {
            playersReference.set(EntityArgumentType.getPlayers(ctx, "players"));
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {}

        if (playersReference.get() != null) {
            for (ServerPlayerEntity player : playersReference.get()) {
                Boost boost = PlayerBoostCache.currentBoost(player, boostType);
                if (boost != null) {
                    if (boost.bossBar != null) player.hideBossBar(boost.bossBar);
                    PlayerBoostCache.remove(player, boostType);
                    ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.playerBoostStopped, ParseContext.builder().player(player).boost(boost).build()));

                    Boost nextBoost = PlayerBoostQueueCache.poll(player, boostType);
                    if (nextBoost != null) {
                        nextBoost.activate();
                        PlayerBoostCache.add(player, nextBoost);
                        if (nextBoost.bossBar != null) player.showBossBar(nextBoost.bossBar);
                    }
                }
            }
        } else {
            ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.globalBoostStopped, ParseContext.builder().boost(BoostManager.globalBoosts.get(boostType)).build()));
            BoostManager.globalBoosts.get(boostType).boostExpirationTime = LocalDateTime.now().plusSeconds(1).toString();
            if (CONFIG.pausePlayerBoostsDuringGlobalBoost) BoostManager.resumePlayerBoosts(boostType);
        }
        return 1;
    }
}
