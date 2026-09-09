package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class SparklesBoostCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("boost")
                .requires(Permissions.require("sparkles.boost", true))
                .then(SparklesBoostStartCommand.register())
                .then(SparklesBoostStopCommand.register())
                .then(SparklesBoostStatusCommand.register());
    }
}
