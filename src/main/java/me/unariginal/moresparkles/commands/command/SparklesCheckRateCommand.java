package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.atomic.AtomicReference;

import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesCheckRateCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("check-rate")
                .requires(Permissions.require("sparkles.checkrate", true))
                .executes(SparklesCheckRateCommand::execute)
                .then(argument("player", EntityArgumentType.player())
                        .requires(Permissions.require("sparkles.checkrate.others", 4))
                        .executes(SparklesCheckRateCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        AtomicReference<ServerPlayerEntity> playerReference = new AtomicReference<>(ctx.getSource().getPlayer());
        try {
            playerReference.set(EntityArgumentType.getPlayer(ctx, "player"));
        } catch (IllegalArgumentException | CommandSyntaxException ignored) {}
        if (playerReference.get() != null) ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.commandCheckRate, ParseContext.builder().player(playerReference.get()).build()));
        return 1;
    }
}
