package me.unariginal.moresparkles.data;

import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Random;

public class CuboidArea extends SparkleArea {
    public double xMin, xMax, yMin, yMax, zMin, zMax;

    public CuboidArea(String id, String type, float multiplier, String world, double xMin, double xMax, double yMin, double yMax, double zMin, double zMax) {
        super(id, type, multiplier, world);
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
    }

    public CuboidArea(String id, String type, float multiplier, String world, boolean particlesEnabled, String particleIdentifier, float particleSpeed, int particleCount, double xMin, double xMax, double yMin, double yMax, double zMin, double zMax) {
        super(id, type, multiplier, world, particlesEnabled, particleIdentifier, particleSpeed, particleCount);
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
    }

    @Override
    public boolean isInArea(ServerWorld playerWorld, double x, double y, double z) {
        return ((playerWorld.getRegistryKey().getValue().toString().equalsIgnoreCase(getWorld().getRegistryKey().getValue().toString())) && (xMin <= x && x <= xMax) && (yMin <= y && y <= yMax) && (zMin <= z && z <= zMax));
    }

    @Override
    public void spawnRandomParticles() {
        if (particlesEnabled) {
            ParticleType<?> particle = Registries.PARTICLE_TYPE.get(Identifier.of(particleIdentifier));
            if (particle instanceof SimpleParticleType simpleParticleType) {
                int totalParticles = new Random().nextInt(Math.max((int) (xMax - xMin), (int) (zMax - zMin)), (int) Math.max((int) xMin + ((xMax - xMin) / 2), (int) zMax + ((zMax - zMin) / 2)));
                for (int i = 0; i < totalParticles; i++) {
                    double cX = new Random().nextDouble(xMin, xMax);
                    double cY = new Random().nextDouble(yMin, yMax);
                    double cZ = new Random().nextDouble(zMin, zMax);
                    getWorld().spawnParticles(simpleParticleType, cX, cY, cZ, particleCount, 1, 1, 1, particleSpeed);
                }
            }
        }
    }
}
