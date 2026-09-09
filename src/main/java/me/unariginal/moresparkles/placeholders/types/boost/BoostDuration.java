package me.unariginal.moresparkles.placeholders.types.boost;

import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.BoostPlaceholder;

import java.util.List;

import static me.unariginal.moresparkles.utils.TextUtils.hms;

public class BoostDuration implements BoostPlaceholder {
    @Override
    public GenericResult handle(Boost boost, List<String> args) {
        return GenericResult.valid(hms(boost.totalSeconds));
    }

    @Override
    public List<String> id() {
        return List.of("boost_duration");
    }
}
