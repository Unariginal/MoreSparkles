package me.unariginal.moresparkles.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.unariginal.moresparkles.commands.command.*;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class SparkleCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("sparkles")
                .then(SparklesReloadCommand.register())
                .then(SparklesBoostCommand.register())
                .then(SparklesCheckRateCommand.register())
                .then(SparklesClearQueueCommand.register())
                .then(SparklesCheckQueueCommand.register())
                .then(SparklesAreaCommand.register()));
    }
}
