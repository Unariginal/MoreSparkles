package me.unariginal.moresparkles.data;

import me.unariginal.moresparkles.MoreSparkles;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public abstract class SparkleArea {
    public String id;
    public String type;
    public float multiplier;
    public String world;
    public boolean particlesEnabled;
    public String particleIdentifier;
    public float particleSpeed;
    public int particleCount;

    public SparkleArea(String id, String type, float multiplier, String world) {
        this.id = id;
        this.type = type;
        this.multiplier = multiplier;
        this.world = world;
        this.particlesEnabled = false;
        this.particleIdentifier = "minecraft:end_rod";
        this.particleSpeed = 0;
        this.particleCount = 0;
    }

    public SparkleArea(String id, String type, float multiplier, String world, boolean particlesEnabled, String particleIdentifier, float particleSpeed, int particleCount) {
        this.id = id;
        this.type = type;
        this.multiplier = multiplier;
        this.world = world;
        this.particlesEnabled = particlesEnabled;
        this.particleIdentifier = particleIdentifier;
        this.particleSpeed = particleSpeed;
        this.particleCount = particleCount;
    }

    public abstract boolean isInArea(ServerWorld playerWorld, double x, double y, double z);

    public abstract void spawnRandomParticles();

    public ServerWorld getWorld() {
        ServerWorld serverWorld = MoreSparkles.INSTANCE.getServer().getOverworld();
        for (ServerWorld w : MoreSparkles.INSTANCE.getServer().getWorlds()) {
            String id = w.getRegistryKey().getValue().toString();
            String path = w.getRegistryKey().getValue().getPath();
            if (id.equals(world) || path.equals(world)) {
                serverWorld = w;
                break;
            }
        }
        return serverWorld;
    }
}
