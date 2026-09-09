package me.unariginal.moresparkles.placeholders.types;

import com.cobblemon.mod.common.Cobblemon;
import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.ServerPlaceholder;

import java.util.List;

public class BaseShinyRate implements ServerPlaceholder {
    @Override
    public GenericResult handle(List<String> args) {
        return GenericResult.valid(Cobblemon.config.getShinyRate());
    }

    @Override
    public List<String> id() {
        return List.of("base_shiny_rate");
    }
}
