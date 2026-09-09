package me.unariginal.moresparkles.placeholders.types.boostarea;

import me.unariginal.moresparkles.data.boostareas.BoostArea;
import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.BoostAreaPlaceholder;

import java.util.List;

public class BoostAreaType implements BoostAreaPlaceholder {
    @Override
    public GenericResult handle(BoostArea boostArea, List<String> args) {
        return GenericResult.valid(boostArea.boostType.toString());
    }

    @Override
    public List<String> id() {
        return List.of("boost_area_type");
    }
}
