package me.unariginal.moresparkles.data;

import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Random;

public class CylinderArea extends SparkleArea {
    public double centerX;
    public double centerZ;
    public double radius;
    public double yMin;
    public double yMax;

    public CylinderArea(String id, String type, float multiplier, String world, boolean particlesEnabled, String particleIdentifier, float particleSpeed, int particleCount, double centerX, double centerZ, double radius, double yMin, double yMax) {
        super(id, type, multiplier, world, particlesEnabled, particleIdentifier, particleSpeed, particleCount);
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public CylinderArea(String id, String type, float multiplier, String world, double centerX, double centerZ, double radius, double yMin, double yMax) {
        super(id, type, multiplier, world);
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    @Override
    public boolean isInArea(ServerWorld playerWorld, double x, double y, double z) {
        return ((playerWorld.getRegistryKey().getValue().toString().equalsIgnoreCase(getWorld().getRegistryKey().getValue().toString())) && ((Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2)) <= Math.pow(radius, 2)) && (yMin <= y && y <= yMax));
    }

    @Override
    public void spawnRandomParticles() {
        if (particlesEnabled) {
            ParticleType<?> particle = Registries.PARTICLE_TYPE.get(Identifier.of(particleIdentifier));
            if (particle instanceof SimpleParticleType simpleParticleType) {
                int totalParticles = new Random().nextInt((int) radius * 2, (int) (radius * 4));
                for (int i = 0; i < totalParticles; i++) {
                    double angle = new Random().nextDouble(0, 2 * Math.PI);
                    double distance = new Random().nextDouble(0, radius);

                    double cX = distance * Math.cos(angle);
                    double cY = new Random().nextDouble(yMin, yMax);
                    double cZ = distance * Math.sin(angle);
                    getWorld().spawnParticles(simpleParticleType, cX, cY, cZ, particleCount, 1, 1, 1, particleSpeed);
                }
            }
        }
    }
}
