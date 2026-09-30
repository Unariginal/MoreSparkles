package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.cache.PlayerBoostQueueCache;
import me.unariginal.moresparkles.configs.GlobalBoostDataManager;
import me.unariginal.moresparkles.configs.PlayerDataManager;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.managers.BoostManager;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesClearQueueCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("clear-queue")
                .requires(Permissions.require("sparkles.clearqueue", 4))
                .then(argument("type", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            for (BoostType type : BoostType.values()) {
                                builder.suggest(type.name());
                            }
                            return builder.buildFuture();
                        })
                        .then(literal("global")
                                .executes(ctx -> execute(ctx, true)))
                        .then(argument("players", EntityArgumentType.players())
                                .executes(ctx -> execute(ctx, false))));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx, boolean global) throws CommandSyntaxException {
        String boostTypeName = StringArgumentType.getString(ctx, "type");
        BoostType boostType = BoostType.valueOf(boostTypeName);

        if (!global) {
            for (ServerPlayerEntity player : EntityArgumentType.getPlayers(ctx, "players")) {
                PlayerBoostQueueCache.remove(player, boostType);
                ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.playerQueueCleared, ParseContext.builder().player(player).boostType(boostType).build()));
                PlayerDataManager.savePlayerBoostData(player);
            }
        } else {
            BoostManager.queuedGlobalBoosts.remove(boostType);
            ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.globalQueueCleared, ParseContext.builder().boostType(boostType).build()));
            GlobalBoostDataManager.saveGlobalBoostData();
        }
        return 1;
    }
}
