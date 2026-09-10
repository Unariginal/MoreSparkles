package me.unariginal.moresparkles.items;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.configs.ItemsConfig;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.LinkedHashMap;
import java.util.Map;

import static me.unariginal.moresparkles.configs.ConfigManager.ITEMS_CONFIG;

public class CharmItemsGroup {
    public static LinkedHashMap<String, CharmItem> charmItems = new LinkedHashMap<>();

    public static void registerItemGroup() {
        if (ITEMS_CONFIG.charms == null) return;
        for (Map.Entry<String, ItemsConfig.CharmData> charmEntry : ITEMS_CONFIG.charms.entrySet()) {
            String id = charmEntry.getKey();
            charmItems.put(id, Registry.register(
                    Registries.ITEM,
                    Identifier.of(MoreSparkles.MOD_ID, id),
                    new CharmItem(
                            new Item.Settings().rarity(Rarity.RARE).maxCount(1),
                            Items.GLOWSTONE_DUST,
                            PolymerResourcePackUtils.requestModel(Items.GLOWSTONE_DUST, Identifier.of(MoreSparkles.MOD_ID, "item/" + id)),
                            id,
                            charmEntry.getValue().lore
                    )
            ));
        }

        Item icon = Items.GLOWSTONE_DUST;
        if (!charmItems.isEmpty() && charmItems.firstEntry() != null) {
            icon = charmItems.firstEntry().getValue();
        }

        final ItemGroup CHARM_ITEMS = FabricItemGroup.builder()
                .icon(icon::getDefaultStack)
                .displayName(Text.literal("Charm Items"))
                .entries(((displayContext, entries) -> {
                    for (CharmItem charmItem : charmItems.values()) {
                        entries.add(charmItem);
                    }
                }))
                .build();

        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MoreSparkles.MOD_ID, "charm_items"), CHARM_ITEMS);
    }
}
