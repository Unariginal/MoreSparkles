package me.unariginal.moresparkles.placeholders.interfaces;

import me.unariginal.moresparkles.data.boostareas.BoostArea;
import me.unariginal.moresparkles.placeholders.GenericResult;

import java.util.List;

public interface BoostAreaPlaceholder {
    GenericResult handle(BoostArea boostArea, List<String> args);
    List<String> id();
}
