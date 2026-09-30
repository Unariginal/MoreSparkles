package me.unariginal.moresparkles.placeholders.types;

import me.unariginal.moresparkles.managers.RateManager;
import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.ServerPlaceholder;

import java.util.List;

public class BaseAlphaBucketChance implements ServerPlaceholder {
    @Override
    public GenericResult handle(List<String> args) {
        return GenericResult.valid(RateManager.formatPercent(RateManager.getAlphaBucketChance(null)));
    }

    @Override
    public List<String> id() {
        return List.of("base_alpha_bucket_chance");
    }
}
