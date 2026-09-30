package me.unariginal.moresparkles.placeholders.types.player;

import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.PlayerPlaceholder;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Locale;

import static me.unariginal.moresparkles.managers.BoostManager.getGenericMultiplierTotal;

public class PlayerBoostMultiplier implements PlayerPlaceholder {
    private final BoostType boostType;

    public PlayerBoostMultiplier(BoostType boostType) {
        this.boostType = boostType;
    }

    @Override
    public GenericResult handle(ServerPlayerEntity player, List<String> args) {
        return GenericResult.valid(getGenericMultiplierTotal(player, boostType));
    }

    @Override
    public List<String> id() {
        return List.of("player_" + boostType.name().toLowerCase(Locale.ROOT) + "_multiplier");
    }
}
