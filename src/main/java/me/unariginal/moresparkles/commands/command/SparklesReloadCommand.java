package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.server.command.ServerCommandSource;

import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesReloadCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("reload")
                .requires(Permissions.require("sparkles.reload", 4))
                .executes(SparklesReloadCommand::execute);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        MoreSparkles.INSTANCE.reload();
        ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.commandReload));
        return 1;
    }
}
