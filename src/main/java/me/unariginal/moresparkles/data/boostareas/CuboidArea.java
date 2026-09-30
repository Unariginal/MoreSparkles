package me.unariginal.moresparkles.data.boostareas;

import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class CuboidArea extends BoostArea {
    public CuboidShapeSettings shape;

    @Override
    public boolean isInArea(ServerWorld playerWorld, double x, double y, double z) {
        return ((playerWorld.getRegistryKey().getValue().toString().equalsIgnoreCase(getWorld(shape).getRegistryKey().getValue().toString())) && (shape.xMin <= x && x <= shape.xMax) && (shape.yMin <= y && y <= shape.yMax) && (shape.zMin <= z && z <= shape.zMax));
    }

    @Override
    public void spawnRandomParticles() {
        if (particles.enabled) {
            Optional<ParticleType<?>> particleType = Registries.PARTICLE_TYPE.getOrEmpty(Identifier.of(particles.identifier));
            if (particleType.isPresent() && particleType.get() instanceof SimpleParticleType simpleParticleType) {
                ServerWorld world = getWorld(shape);
                int totalParticles = randomParticleCount(Math.max(Math.abs(shape.xMax - shape.xMin), Math.abs(shape.zMax - shape.zMin)));
                for (int i = 0; i < totalParticles; i++) {
                    double cX = randomBetween(shape.xMin, shape.xMax);
                    double cY = randomBetween(shape.yMin, shape.yMax);
                    double cZ = randomBetween(shape.zMin, shape.zMax);
                    world.spawnParticles(simpleParticleType, cX, cY, cZ, particles.count, 1, 1, 1, particles.speed);
                }
            }
        }
    }
}
