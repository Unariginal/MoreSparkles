package me.unariginal.moresparkles.placeholders.interfaces;

import me.unariginal.moresparkles.placeholders.GenericResult;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public interface PlayerPlaceholder {
    GenericResult handle(ServerPlayerEntity player, List<String> args);
    List<String> id();
}
