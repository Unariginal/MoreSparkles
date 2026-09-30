package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesCheckRateCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("check-rate")
                .requires(Permissions.require("sparkles.checkrate", true))
                .then(argument("type", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            for (BoostType type : BoostType.values()) {
                                builder.suggest(type.name());
                            }
                            return builder.buildFuture();
                        })
                        .executes(SparklesCheckRateCommand::execute)
                        .then(argument("player", EntityArgumentType.player())
                                .requires(Permissions.require("sparkles.checkrate.others", 4))
                                .executes(SparklesCheckRateCommand::execute)));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        String boostTypeName = StringArgumentType.getString(ctx, "type");
        BoostType boostType = BoostType.valueOf(boostTypeName);

        AtomicReference<ServerPlayerEntity> playerReference = new AtomicReference<>(ctx.getSource().getPlayer());
        try {
            playerReference.set(EntityArgumentType.getPlayer(ctx, "player"));
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {}
        if (playerReference.get() != null) ctx.getSource().sendMessage(TextUtils.deserialize(getMessage(boostType), ParseContext.builder().player(playerReference.get()).build()));
        return 1;
    }

    private static String getMessage(BoostType boostType) {
        String message = MESSAGES.checkRateMessages != null ? MESSAGES.checkRateMessages.get(boostType) : null;
        if (message != null) return message;
        return "%prefix% <gray>" + boostType.getDisplayName() + " | Multiplier: <yellow>%player_" + boostType.name().toLowerCase(Locale.ROOT) + "_multiplier%x";
    }
}
