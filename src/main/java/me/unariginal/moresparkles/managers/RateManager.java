package me.unariginal.moresparkles.managers;

import com.cobblemon.mod.common.api.spawning.BestSpawner;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.spawning.influences.AlphaSpawningInfluence;
import me.unariginal.moresparkles.spawning.influences.SpawnBucketInfluence;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static me.unariginal.moresparkles.configs.ConfigManager.CONFIG;
import static me.unariginal.moresparkles.managers.BoostManager.getGenericMultiplierTotal;

public class RateManager {
    public static float getHiddenAbilityChance(ServerPlayerEntity player) {
        float multiplier = getGenericMultiplierTotal(player, BoostType.HIDDEN_ABILITY);
        if (multiplier == 1) return 0F;
        return Math.min(1.0F, CONFIG.hiddenAbilityBoosterBaseChance * multiplier);
    }

    public static double getIvStrength(ServerPlayerEntity player) {
        double multiplier = getGenericMultiplierTotal(player, BoostType.IV);
        return Math.max(0, (multiplier - 1) / (multiplier - 1 + CONFIG.ivBoosterStrengthConstant));
    }

    public static Map<String, Float> getBucketChances(@Nullable ServerPlayerEntity player) {
        Map<String, Float> weights = new HashMap<>(BestSpawner.INSTANCE.getConfig().getWorldBuckets());
        if (player != null) {
            new AlphaSpawningInfluence(player).affectBucketWeights(weights);
            new SpawnBucketInfluence(player).affectBucketWeights(weights);
        }

        float total = (float) weights.values().stream().mapToDouble(Float::doubleValue).sum();
        Map<String, Float> chances = new LinkedHashMap<>();
        weights.forEach((bucket, weight) -> chances.put(bucket, total > 0 ? weight / total : 0F));
        return chances;
    }

    public static float getAlphaBucketChance(@Nullable ServerPlayerEntity player) {
        Map<String, Float> chances = getBucketChances(player);
        float chance = 0F;
        for (String bucket : SpawnsManager.getAlphaBuckets()) {
            chance += chances.getOrDefault(bucket, 0F);
        }
        return chance;
    }

    public static String formatPercent(double fraction) {
        return String.format(Locale.ROOT, "%.2f", fraction * 100);
    }
}
