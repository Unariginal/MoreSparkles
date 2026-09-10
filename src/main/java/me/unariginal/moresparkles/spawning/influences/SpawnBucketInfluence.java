package me.unariginal.moresparkles.spawning.influences;

import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import me.unariginal.moresparkles.data.BoostType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.unariginal.moresparkles.managers.BoostManager.getGenericMultiplierTotal;

public class SpawnBucketInfluence implements SpawningInfluence {
    private final ServerPlayerEntity player;

    public SpawnBucketInfluence(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void affectBucketWeights(@NotNull Map<String, Float> bucketWeights) {
        float multiplier = getGenericMultiplierTotal(player, BoostType.SPAWN_BUCKET);
        if (multiplier == 1F) return;

        Float bossWeight = bucketWeights.get("boss");
        float originalNonBossWeightTotal = (float) bucketWeights.entrySet().stream()
                .filter(e -> !e.getKey().equals("boss"))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        float exponent = 1.0F / multiplier;
        bucketWeights.replaceAll((bucket, weight) ->
                bucket.equals("boss") ? weight : (float) Math.pow(weight, exponent));

        if (bossWeight != null && originalNonBossWeightTotal > 0F) {
            float newNonBossTotal = (float) bucketWeights.entrySet().stream()
                    .filter(e -> !e.getKey().equals("boss"))
                    .mapToDouble(Map.Entry::getValue)
                    .sum();
            bucketWeights.put("boss", bossWeight * (newNonBossTotal / originalNonBossWeightTotal));
        }
    }
}
