package me.unariginal.moresparkles.commands.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import me.unariginal.moresparkles.placeholders.ParseContext;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static me.unariginal.moresparkles.configs.ConfigManager.BOOST_AREAS;
import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SparklesAreaInfoCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return literal("info")
                .requires(Permissions.require("sparkles.area.info", 4))
                .executes(SparklesAreaInfoCommand::execute)
                .then(argument("id", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            BOOST_AREAS.keySet().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(SparklesAreaInfoCommand::execute));
    }

    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String id = null;
        try {
            id = StringArgumentType.getString(ctx, "id");
        }
        catch (IllegalArgumentException ignored) {}

        if (player != null && id == null) {
            for (BoostArea area : BOOST_AREAS.values()) {
                if (area.isInArea(player.getServerWorld(), player.getX(), player.getY(), player.getZ())) {
                    player.sendMessage(TextUtils.deserialize(MESSAGES.messages.areaInfo, ParseContext.builder().player(player).boostArea(area).build()));
                    return 1;
                }
            }
        } else if (id != null) {
            BoostArea area = BOOST_AREAS.get(id);
            if (area != null) {
                ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.areaInfo, ParseContext.builder().player(player).boostArea(area).build()));
                return 1;
            }
        }
        ctx.getSource().sendMessage(TextUtils.deserialize(MESSAGES.messages.notInArea, ParseContext.builder().player(player).build()));
        return 1;
    }
}
