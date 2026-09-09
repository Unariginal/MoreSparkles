package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class SparklesAreaCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("area")
                .requires(Permissions.require("sparkles.area", 4))
                .then(SparklesAreaInfoCommand.register());
    }
}
