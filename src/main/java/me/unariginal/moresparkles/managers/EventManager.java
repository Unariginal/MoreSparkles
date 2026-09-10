package me.unariginal.moresparkles.managers;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.berry.BerryHarvestEvent;
import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;
import com.cobblemon.mod.common.api.events.pokemon.EvGainedEvent;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.unariginal.moresparkles.cache.PlayerBoostCache;
import me.unariginal.moresparkles.configs.Config;
import me.unariginal.moresparkles.configs.ItemsConfig;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import me.unariginal.moresparkles.items.CharmItemsGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

import static me.unariginal.moresparkles.configs.ConfigManager.*;
import static me.unariginal.moresparkles.managers.BoostManager.getGenericMultiplierTotal;

public class EventManager {
    public static void register() {
        CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe(EventManager::shinyBoost);
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(EventManager::experienceBoost);
        CobblemonEvents.EV_GAINED_EVENT_PRE.subscribe(EventManager::evBoost);
        CobblemonEvents.POKEMON_CAPTURED.subscribe(EventManager::ivBoost);
        CobblemonEvents.BERRY_HARVEST.subscribe(EventManager::berryHarvestBoost);
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(EventManager::markBoost);
        CobblemonEvents.POKEMON_CATCH_RATE.subscribe(EventManager::catchRateBoost);
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(EventManager::hiddenAbilityBoost);
    }

    private static void shinyBoost(ShinyChanceCalculationEvent event) {
        if (!Config.canBeBoosted(event.getPokemon(), BoostType.SHINY)) return;
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

    private static void experienceBoost(ExperienceGainedEvent.Pre event) {
        if (event.getSource().isCommand()) return;
        if (CONFIG.experienceBoosterIgnoresCandy && event.getSource().isInteraction()) return;
        if (CONFIG.experienceBoosterIgnoresSidemodSource && event.getSource().isSidemod()) return;

        Pokemon pokemon = event.getPokemon();
        if (!Config.canBeBoosted(pokemon, BoostType.EXPERIENCE)) return;

        int experience = event.getExperience();
        float multiplier = 1.0F;
        if (pokemon.isPlayerOwned()) {
            ServerPlayerEntity player = pokemon.getOwnerPlayer();
            if (player != null) {
                multiplier = getGenericMultiplierTotal(player, BoostType.EXPERIENCE);
            }
        }

        experience *= (int) multiplier;
        event.setExperience(experience);
    }

    private static void evBoost(EvGainedEvent.Pre event) {
        if (CONFIG.evBoosterIgnoresVitamins && event.getSource().isInteraction()) return;
        if (CONFIG.evBoosterIgnoresSidemodSource && event.getSource().isSidemod()) return;

        int gainedAmount = event.getAmount();
        float multiplier = 1.0F;
        Pokemon pokemon = event.getPokemon();
        if (!Config.canBeBoosted(pokemon, BoostType.EV)) return;
        if (pokemon.isPlayerOwned()) {
            ServerPlayerEntity player = pokemon.getOwnerPlayer();
            if (player != null) {
                multiplier = getGenericMultiplierTotal(player, BoostType.EV);
            }
        }

        gainedAmount *= (int) multiplier;
        event.setAmount(gainedAmount);
    }

    private static void berryHarvestBoost(BerryHarvestEvent event) {
        List<ItemStack> drops = event.getDrops();
        float multiplier = getGenericMultiplierTotal(event.getPlayer(), BoostType.BERRY);
        for (ItemStack drop : drops) {
            drop.setCount(Math.min(drop.getMaxCount(), drop.getCount() * (int) multiplier));
        }
    }

    private static void ivBoost(PokemonCapturedEvent event) {
        ServerPlayerEntity player = event.getPlayer();
        if (!Config.canBeBoosted(event.getPokemon(), BoostType.IV)) return;
        int multiplier = (int) getGenericMultiplierTotal(player, BoostType.IV);

        int rolls = Math.max(1, multiplier);
        IVs ivs = event.getPokemon().getIvs();
        Random random = new Random();

        List<Stat> stats = new ArrayList<>();
        for (Map.Entry<? extends Stat, ? extends Integer> entry : ivs) {
            stats.add(entry.getKey());
        }

        for (Stat stat : stats) {
            int best = 0;
            for (int i = 0; i < rolls; i++) {
                best = Math.max(best, random.nextInt(IVs.MAX_VALUE + 1));
            }
            ivs.set(stat, best);
        }
    }

    private static void markBoost(SpawnEvent<PokemonEntity> event) {
        if (event.getCause().getEntity() instanceof ServerPlayerEntity player) {
            if (!Config.canBeBoosted(event.getEntity().getPokemon(), BoostType.MARK)) return;
            float multiplier = getGenericMultiplierTotal(player, BoostType.MARK);
            if (multiplier == 1) return;
            event.getEntity().getPokemon().applyPotentialMarks(1.0 + (multiplier / 100));
        }
    }

    private static void catchRateBoost(PokemonCatchRateEvent event) {
        if (event.getThrower() instanceof ServerPlayerEntity player) {
            if (!Config.canBeBoosted(event.getPokemonEntity().getPokemon(), BoostType.CATCH_RATE)) return;
            float multiplier = getGenericMultiplierTotal(player, BoostType.CATCH_RATE);
            float catchRate = event.getCatchRate();
            event.setCatchRate(Math.min(255, multiplier * catchRate));
        }
    }

    private static void hiddenAbilityBoost(SpawnEvent<PokemonEntity> event) {
        if (event.getCause().getEntity() instanceof ServerPlayerEntity player) {
            if (!Config.canBeBoosted(event.getEntity().getPokemon(), BoostType.HIDDEN_ABILITY)) return;
            float multiplier = getGenericMultiplierTotal(player, BoostType.HIDDEN_ABILITY);
            if (multiplier == 1) return;

            float chance = Math.min(1.0F, CONFIG.hiddenAbilityBoosterBaseChance * multiplier);
            if (Math.random() <= chance) {
                FishingSpawnCause.Companion.alterHAAttempt(event.getEntity());
            }
        }
    }
}
