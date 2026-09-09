package me.unariginal.moresparkles.placeholders.interfaces;

import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.placeholders.GenericResult;

import java.util.List;

public interface BoostPlaceholder {
    GenericResult handle(Boost boost, List<String> args);
    List<String> id();
}
