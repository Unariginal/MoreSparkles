package me.unariginal.moresparkles.data.boostareas;

import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.data.BoostType;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

public abstract class BoostArea {
    public String displayName;
    public BoostType boostType;
    public float multiplier;
    public ParticleSettings particles;

    public static class ParticleSettings {
        public boolean enabled;
        public String identifier;
        public float speed;
        public int count;
    }

    public abstract boolean isInArea(ServerWorld playerWorld, double x, double y, double z);

    public abstract void spawnRandomParticles();

    protected static final Random RANDOM = new Random();

    protected static double randomBetween(double min, double max) {
        return min + RANDOM.nextDouble() * (max - min);
    }

    protected static int randomParticleCount(double base) {
        int min = Math.max(1, (int) base);
        return RANDOM.nextInt(min, min * 2 + 1);
    }

    public ServerWorld getWorld(ShapeSettings shape) {
        ServerWorld serverWorld = MoreSparkles.INSTANCE.server.getOverworld();
        for (ServerWorld w : MoreSparkles.INSTANCE.server.getWorlds()) {
            String id = w.getRegistryKey().getValue().toString();
            String path = w.getRegistryKey().getValue().getPath();
            if (id.equals(shape.world) || path.equals(shape.world)) {
                serverWorld = w;
                break;
            }
        }
        return serverWorld;
    }
}
