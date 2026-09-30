package me.unariginal.moresparkles.data.boostareas;

import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class CylinderArea extends BoostArea {
    public CylinderShapeSettings shape;

    @Override
    public boolean isInArea(ServerWorld playerWorld, double x, double y, double z) {
        return ((playerWorld.getRegistryKey().getValue().toString().equalsIgnoreCase(getWorld(shape).getRegistryKey().getValue().toString())) && ((Math.pow(x - shape.centerX, 2) + Math.pow(z - shape.centerZ, 2)) <= Math.pow(shape.radius, 2)) && (shape.yMin <= y && y <= shape.yMax));
    }

    @Override
    public void spawnRandomParticles() {
        if (particles.enabled) {
            Optional<ParticleType<?>> particleType = Registries.PARTICLE_TYPE.getOrEmpty(Identifier.of(particles.identifier));
            if (particleType.isPresent() && particleType.get() instanceof SimpleParticleType simpleParticleType) {
                ServerWorld world = getWorld(shape);
                int totalParticles = randomParticleCount(Math.abs(shape.radius) * 2);
                for (int i = 0; i < totalParticles; i++) {
                    double angle = randomBetween(0, 2 * Math.PI);
                    double distance = randomBetween(0, shape.radius);

                    double cX = shape.centerX + distance * Math.cos(angle);
                    double cY = randomBetween(shape.yMin, shape.yMax);
                    double cZ = shape.centerZ + distance * Math.sin(angle);
                    world.spawnParticles(simpleParticleType, cX, cY, cZ, particles.count, 1, 1, 1, particles.speed);
                }
            }
        }
    }
}
