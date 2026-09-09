package me.unariginal.moresparkles.placeholders.types.boost;

import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.BoostPlaceholder;

import java.util.List;

public class BoostMultiplier implements BoostPlaceholder {
    @Override
    public GenericResult handle(Boost boost, List<String> args) {
        return GenericResult.valid(boost.multiplier);
    }

    @Override
    public List<String> id() {
        return List.of("boost_multiplier");
    }
}
