package me.unariginal.moresparkles.placeholders;

import me.unariginal.moresparkles.placeholders.interfaces.BoostAreaPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.BoostPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.PlayerPlaceholder;
import me.unariginal.moresparkles.placeholders.interfaces.ServerPlaceholder;
import me.unariginal.moresparkles.placeholders.types.BaseAlphaBucketChance;
import me.unariginal.moresparkles.placeholders.types.BaseBucketChance;
import me.unariginal.moresparkles.placeholders.types.BaseShinyRate;
import me.unariginal.moresparkles.placeholders.types.MoreSparklesPrefix;
import me.unariginal.moresparkles.placeholders.types.boost.BoostDuration;
import me.unariginal.moresparkles.placeholders.types.boost.BoostMultiplier;
import me.unariginal.moresparkles.placeholders.types.boost.BoostTimeRemaining;
import me.unariginal.moresparkles.placeholders.types.boost.BoostType;
import me.unariginal.moresparkles.placeholders.types.boostarea.BoostAreaMultiplier;
import me.unariginal.moresparkles.placeholders.types.boostarea.BoostAreaName;
import me.unariginal.moresparkles.placeholders.types.boostarea.BoostAreaType;
import me.unariginal.moresparkles.placeholders.types.player.*;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PlaceholderManager {
    public static final List<ServerPlaceholder> serverPlaceholders = List.of(
            new MoreSparklesPrefix(),
            new BaseShinyRate(),
            new BaseBucketChance(),
            new BaseAlphaBucketChance()
    );

    public static final List<PlayerPlaceholder> playerPlaceholders = Stream.concat(
            Stream.of(
                    new PlayerShinyRate(),
                    new PlayerHiddenAbilityChance(),
                    new PlayerIvStrength(),
                    new PlayerBucketChance(),
                    new PlayerAlphaBucketChance(),
                    new PlayerUuid(),
                    new PlayerName()
            ),
            Arrays.stream(me.unariginal.moresparkles.data.BoostType.values()).map(PlayerBoostMultiplier::new)
    ).toList();

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

    public static boolean usingPlaceholderAPI = false;
    public static PlaceholderAPIService placeholderAPIService;

    public static void registerPlaceholders() {
        usingPlaceholderAPI = FabricLoader.getInstance().isModLoaded("placeholder-api");
        if (usingPlaceholderAPI) placeholderAPIService = new PlaceholderAPIService();

        serverPlaceholders.forEach(placeholder -> {
            if (usingPlaceholderAPI) placeholderAPIService.registerServer(placeholder);
        });

        playerPlaceholders.forEach(placeholder -> {
            if (usingPlaceholderAPI) placeholderAPIService.registerPlayer(placeholder);
        });
    }
}
