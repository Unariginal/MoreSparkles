package me.unariginal.moresparkles.managers;

import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.data.boostareas.BoostArea;

import java.util.Random;

import static me.unariginal.moresparkles.configs.ConfigManager.BOOST_AREAS;

public class TickManager {
    private static long particleCooldown = 3 * 20;

    public static void tickParticles() {
        particleCooldown--;
        if (particleCooldown <= 0) {
            particleCooldown = new Random().nextLong(2*20, 5*20);
            for (BoostArea area : BOOST_AREAS.values()) {
                // An exception here would escape the server tick and crash the server
                try {
                    area.spawnRandomParticles();
                } catch (Exception e) {
                    MoreSparkles.LOGGER.error("[MoreSparkles] Failed to spawn particles for boost area \"{}\"", area.displayName, e);
                }
            }
        }
    }
}
