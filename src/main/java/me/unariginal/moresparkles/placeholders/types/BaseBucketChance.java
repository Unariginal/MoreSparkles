package me.unariginal.moresparkles.placeholders.types;

import me.unariginal.moresparkles.managers.RateManager;
import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.ServerPlaceholder;

import java.util.List;
import java.util.Map;

public class BaseBucketChance implements ServerPlaceholder {
    @Override
    public GenericResult handle(List<String> args) {
        if (args.isEmpty()) return GenericResult.invalid("Missing bucket");
        Map<String, Float> chances = RateManager.getBucketChances(null);
        Float chance = chances.get(args.getFirst());
        if (chance == null) return GenericResult.invalid("Unknown bucket: " + args.getFirst());
        return GenericResult.valid(RateManager.formatPercent(chance));
    }

    @Override
    public List<String> id() {
        return List.of("base_bucket_chance");
    }
}
