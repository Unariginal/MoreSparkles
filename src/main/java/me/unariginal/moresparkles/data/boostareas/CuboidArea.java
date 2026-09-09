package me.unariginal.moresparkles.data.boostareas;

import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.Random;

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
                int totalParticles = new Random().nextInt(Math.max((int) (shape.xMax - shape.xMin), (int) (shape.zMax - shape.zMin)), (int) Math.max((int) shape.xMin + ((shape.xMax - shape.xMin) / 2), (int) shape.zMax + ((shape.zMax - shape.zMin) / 2)));
                for (int i = 0; i < totalParticles; i++) {
                    double cX = new Random().nextDouble(shape.xMin, shape.xMax);
                    double cY = new Random().nextDouble(shape.yMin, shape.yMax);
                    double cZ = new Random().nextDouble(shape.zMin, shape.zMax);
                    getWorld(shape).spawnParticles(simpleParticleType, cX, cY, cZ, particles.count, 1, 1, 1, particles.speed);
                }
            }
        }
    }
}
