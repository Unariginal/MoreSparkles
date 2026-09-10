package me.unariginal.moresparkles.managers;

import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.cobblemon.mod.common.api.spawning.detail.PokemonHerdSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.pokemon.aspects.PokemonAspectsKt;

import java.util.HashSet;
import java.util.Set;

public class SpawnsManager {
    public static boolean detailHasAlpha(SpawnDetail detail) {
        if (detail instanceof PokemonHerdSpawnDetail herdDetail) {
            return herdDetail.getHerdablePokemon().stream()
                    .anyMatch(herdable -> !PokemonAspectsKt.getALPHA_ASPECT().provide(herdable.getPokemon()).isEmpty());
        }

        if (detail instanceof PokemonSpawnDetail pokemonDetail) {
            return !PokemonAspectsKt.getALPHA_ASPECT().provide(pokemonDetail.getPokemon()).isEmpty();
        }

        return false;
    }

    public static Set<String> getAlphaBuckets() {
        Set<String> alphaBuckets = new HashSet<>();
        for (SpawnDetail detail : CobblemonSpawnPools.INSTANCE.getWORLD_SPAWN_POOL()) {
            if (detailHasAlpha(detail)) {
                alphaBuckets.add(detail.getBucket());
            }
        }
        return alphaBuckets;
    }
}
