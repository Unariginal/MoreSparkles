package me.unariginal.moresparkles.placeholders.types.player;

import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.PlayerPlaceholder;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class PlayerUuid implements PlayerPlaceholder {
    @Override
    public GenericResult handle(ServerPlayerEntity player, List<String> args) {
        return GenericResult.valid(player.getUuidAsString());
    }

    @Override
    public List<String> id() {
        return List.of("player_uuid");
    }
}
