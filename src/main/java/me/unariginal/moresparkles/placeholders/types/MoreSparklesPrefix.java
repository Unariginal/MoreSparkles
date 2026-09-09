package me.unariginal.moresparkles.placeholders.types;

import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.ServerPlaceholder;

import java.util.List;

import static me.unariginal.moresparkles.configs.ConfigManager.MESSAGES;

public class MoreSparklesPrefix implements ServerPlaceholder {
    @Override
    public GenericResult handle(List<String> args) {
        return GenericResult.valid(MESSAGES.prefix);
    }

    @Override
    public List<String> id() {
        return List.of("prefix");
    }
}
