package me.unariginal.moresparkles.placeholders;

import me.unariginal.moresparkles.placeholders.interfaces.BoostAreaPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.BoostPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.PlayerPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.ServerPlaceholder;
import me.unariginal.moresparkles.placeholders.types.BaseShinyRate;
import me.unariginal.moresparkles.placeholders.types.MoreSparklesPrefix;
import me.unariginal.moresparkles.placeholders.types.boost.BoostDuration;
import me.unariginal.moresparkles.placeholders.types.boost.BoostMultiplier;
import me.unariginal.moresparkles.placeholders.types.boost.BoostTimeRemaining;
import me.unariginal.moresparkles.placeholders.types.boost.BoostType;
import me.unariginal.moresparkles.placeholders.types.boostarea.BoostAreaMultiplier;
import me.unariginal.moresparkles.placeholders.types.boostarea.BoostAreaName;
import me.unariginal.moresparkles.placeholders.types.boostarea.BoostAreaType;
import me.unariginal.moresparkles.placeholders.types.player.PlayerName;
import me.unariginal.moresparkles.placeholders.types.player.PlayerShinyRate;
import me.unariginal.moresparkles.placeholders.types.player.PlayerUuid;

import java.util.List;

public class PlaceholderManager {
    public static final List<ServerPlaceholder> serverPlaceholders = List.of(
            new MoreSparklesPrefix(),
            new BaseShinyRate()
    );

    public static final List<PlayerPlaceholder> playerPlaceholders = List.of(
            new PlayerShinyRate(),
            new PlayerUuid(),
            new PlayerName()
    );

    public static final List<BoostPlaceholder> boostPlaceholders = List.of(
            new BoostMultiplier(),
            new BoostDuration(),
            new BoostTimeRemaining(),
            new BoostType()
    );

    public static final List<BoostAreaPlaceholder> boostAreaPlaceholders = List.of(
            new BoostAreaMultiplier(),
            new BoostAreaType(),
            new BoostAreaName()
    );
}
