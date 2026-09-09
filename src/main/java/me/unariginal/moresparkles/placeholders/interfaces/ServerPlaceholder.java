package me.unariginal.moresparkles.placeholders.interfaces;

import me.unariginal.moresparkles.placeholders.GenericResult;

import java.util.List;

public interface ServerPlaceholder {
    GenericResult handle(List<String> args);
    List<String> id();
}
