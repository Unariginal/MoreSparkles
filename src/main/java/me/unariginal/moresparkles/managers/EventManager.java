package me.unariginal.moresparkles.managers;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.configs.ItemsConfig;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import me.unariginal.moresparkles.items.CharmItemsGroup;

import static me.unariginal.moresparkles.configs.ConfigManager.*;

public class EventManager {
    public static void register() {
        CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe(EventManager::shinyChanceCalculation);
    }

    private static void shinyChanceCalculation(ShinyChanceCalculationEvent event) {
        event.addModificationFunction(((rate, player, pokemon) -> {
            if (player != null) {
                Boost boost = PlayerBoostCache.currentBoost(player, BoostType.SHINY);
                if (boost != null && boost.boostPauseTime == null) {
                    return Math.max(rate / boost.multiplier, 1);
                }
            }
            return rate;
        }));

        if (BoostManager.globalBoosts != null && BoostManager.globalBoosts.get(BoostType.SHINY) != null) {
            event.addModificationFunction(((rate, player, pokemon) -> Math.max(rate / BoostManager.globalBoosts.get(BoostType.SHINY).multiplier, 1)));
        }

        for (BoostArea boostArea : BOOST_AREAS.values()) {
            if (boostArea.boostType == BoostType.SHINY) {
                event.addModificationFunction((rate, player, pokemon) -> {
                    if (player != null) {
                        if (boostArea.isInArea(player.getServerWorld(), player.getX(), player.getY(), player.getZ())) {
                            return Math.max(rate / boostArea.multiplier, 1);
                        }
                    }
                    return rate;
                });
            }
        }

        event.addModificationFunction((rate, player, pokemon) -> {
            if (player != null && ITEMS_CONFIG.charms != null) {
                for (String key : CharmItemsGroup.charmItems.keySet()) {
                    ItemsConfig.CharmData charmData = ITEMS_CONFIG.charms.get(key);
                    if (charmData == null || charmData.boostType != BoostType.SHINY) continue;

                    if (player.getInventory().contains(CharmItemsGroup.charmItems.get(key).getDefaultStack())) {
                        rate = Math.max(rate / charmData.multiplier, 1);
                    }
                }
            }
            return rate;
        });
    }
}
