package me.unariginal.moresparkles.managers;

import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.cobblemon.mod.common.api.spawning.detail.PokemonHerdSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool;
import com.cobblemon.mod.common.pokemon.aspects.PokemonAspectsKt;

import java.util.HashSet;
import java.util.Set;

public class SpawnsManager {
    private static volatile Set<String> alphaBuckets = Set.of();
    private static boolean subscribed = false;

    public static void register() {
        SpawnPool pool = CobblemonSpawnPools.INSTANCE.getWORLD_SPAWN_POOL();
        refreshAlphaBuckets(pool);
        if (subscribed) return;
        pool.getObservable().subscribe(SpawnsManager::refreshAlphaBuckets);
        subscribed = true;
    }

    private static void refreshAlphaBuckets(SpawnPool pool) {
        Set<String> buckets = new HashSet<>();
        for (SpawnDetail detail : pool) {
            if (detailHasAlpha(detail)) {
                buckets.add(detail.getBucket());
            }
        }
        alphaBuckets = Set.copyOf(buckets);
    }

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
        return alphaBuckets;
    }
}
