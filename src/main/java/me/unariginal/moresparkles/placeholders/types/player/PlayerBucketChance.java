package me.unariginal.moresparkles.placeholders.types.player;

import me.unariginal.moresparkles.managers.RateManager;
import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.PlayerPlaceholder;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Map;

public class PlayerBucketChance implements PlayerPlaceholder {
    @Override
    public GenericResult handle(ServerPlayerEntity player, List<String> args) {
        if (args.isEmpty()) return GenericResult.invalid("Missing bucket");
        Map<String, Float> chances = RateManager.getBucketChances(player);
        Float chance = chances.get(args.getFirst());
        if (chance == null) return GenericResult.invalid("Unknown bucket: " + args.getFirst());
        return GenericResult.valid(RateManager.formatPercent(chance));
    }

    @Override
    public List<String> id() {
        return List.of("player_bucket_chance");
    }
}
