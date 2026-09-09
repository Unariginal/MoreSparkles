package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.cache.PlayerBoostQueueCache;
import me.unariginal.moresparkles.configs.Config;
import me.unariginal.moresparkles.configs.PlayerDataManager;
import me.unariginal.moresparkles.managers.BoostManager;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesClearQueueCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("clear-queue")
                .requires(Permissions.require("sparkles.clearqueue", 4))
                .then(literal("global")
                        .executes(SparklesClearQueueCommand::execute))
                .then(argument("players", EntityArgumentType.players())
                        .executes(SparklesClearQueueCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        AtomicReference<Collection<ServerPlayerEntity>> playersReference = new AtomicReference<>(null);
        try {
            playersReference.set(EntityArgumentType.getPlayers(ctx, "players"));
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {}
        if (playersReference.get() != null) {
            for (ServerPlayerEntity player : playersReference.get()) {
                PlayerBoostQueueCache.remove(player);
                ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.playerQueueCleared, ParseContext.builder().player(player).build()));
                PlayerDataManager.savePlayerBoostData(player);
            }
        } else {
            BoostManager.queuedGlobalBoosts.clear();
            ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.globalQueueCleared));
            Config.saveGlobalBoostData();
        }
        return 1;
    }
}
