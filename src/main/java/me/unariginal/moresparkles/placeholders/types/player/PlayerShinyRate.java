package me.unariginal.moresparkles.placeholders.types.player;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import kotlin.Unit;
import me.unariginal.moresparkles.placeholders.GenericResult;
import me.unariginal.moresparkles.placeholders.interfaces.PlayerPlaceholder;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerShinyRate implements PlayerPlaceholder {
    @Override
    public GenericResult handle(ServerPlayerEntity player, List<String> args) {
        AtomicReference<Float> shinyRate = new AtomicReference<>(Cobblemon.config.getShinyRate());
        CobblemonEvents.SHINY_CHANCE_CALCULATION.post(new ShinyChanceCalculationEvent[] {
                new ShinyChanceCalculationEvent(Cobblemon.config.getShinyRate(), new PokemonProperties().create())
        }, event -> {
            shinyRate.set(event.calculate(player));
            return Unit.INSTANCE;
        });

        return GenericResult.valid(shinyRate.get());
    }

    @Override
    public List<String> id() {
        return List.of("player_shiny_rate");
    }
}
