package me.unariginal.moresparkles.spawning.influences;

import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.managers.SpawnsManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.unariginal.moresparkles.managers.BoostManager.getGenericMultiplierTotal;

// Because Cobblemon decided that alphas will spawn through setting the alpha aspect in a spawn_pool_world file...
// We look through the whole spawn pool and increase the weight where we find alpha aspects.
// If I didn't care, I would just boost the weight of the "boss" bucket. But I understand that users will make
// odd choices and decide to implement an alpha pokemon spawn file that's either not in that bucket or not in a herd...
public class AlphaSpawningInfluence implements SpawningInfluence {
    private final ServerPlayerEntity player;

    public AlphaSpawningInfluence(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void affectBucketWeights(@NotNull Map<String, Float> bucketWeights) {
        float multiplier = getGenericMultiplierTotal(player, BoostType.ALPHA);
        if (multiplier == 1) return;

        for (String bucket : SpawnsManager.getAlphaBuckets()) {
            bucketWeights.computeIfPresent(bucket, (b, w) -> w * multiplier);
        }
    }

    @Override
    public float affectWeight(@NotNull SpawnDetail detail, @NotNull SpawnablePosition position, float weight) {
        if (!SpawnsManager.detailHasAlpha(detail)) return weight;
        return weight * getGenericMultiplierTotal(player, BoostType.ALPHA);
    }
}
