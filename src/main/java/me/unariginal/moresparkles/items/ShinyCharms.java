package me.unariginal.moresparkles.items;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShinyCharms {
    public static final ShinyCharms INSTANCE = new ShinyCharms();

    public ItemStack getShinyCharm(String id) {
        if (!shinyCharmPolymerItems.containsKey(id)) return ItemStack.EMPTY;
        return shinyCharmPolymerItems.get(id).getDefaultStack();
    }


    public Map<String, ShinyCharmPolymerItem> shinyCharmPolymerItems = new HashMap<>();
    public Map<String, PolymerModelData> shinyCharmPolymerModelData = new HashMap<>();

    public void fillPolymerItems() {
        for (String key : MoreSparkles.INSTANCE.getItemsConfig().shinyMultipliers.keySet()) {
            shinyCharmPolymerItems.put(
                    key,
                    Registry.register(
                            Registries.ITEM,
                            Identifier.of(MoreSparkles.MOD_ID, key),
                            new ShinyCharmPolymerItem(
                                    new Item.Settings().rarity(Rarity.RARE).maxCount(1),
                                    Items.GLOWSTONE_DUST,
                                    key,
                                    MoreSparkles.INSTANCE.getItemsConfig().shinyMultipliers.get(key)
                            )
                    )
            );
        }
    }

    public void fillPolymerModelData() {
        for (String key : MoreSparkles.INSTANCE.getItemsConfig().shinyMultipliers.keySet()) {
            shinyCharmPolymerModelData.put(key, PolymerResourcePackUtils.requestModel(Items.GLOWSTONE_DUST, Identifier.of(MoreSparkles.MOD_ID, "item/" + key)));
        }
    }

    public static class ShinyCharmPolymerItem extends SimplePolymerItem {
        private final PolymerModelData modelData;
        private final String id;
        private final float multiplier;

        public ShinyCharmPolymerItem(Settings settings, Item polymerItem, String id, float multiplier) {
            super(settings, polymerItem);
            this.id = id;
            this.multiplier = multiplier;
            this.modelData = ShinyCharms.INSTANCE.shinyCharmPolymerModelData.get(id);
        }

        public String getId() {
            return this.id;
        }

        public float getMultiplier() {
            return this.multiplier;
        }

        @Override
        public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
            return this.modelData.value();
        }

        @Override
        public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
            super.appendTooltip(stack, context, tooltip, type);
            if (MoreSparkles.INSTANCE.getItemsConfig().shinyLore.containsKey(id)) {
                for (String line : MoreSparkles.INSTANCE.getItemsConfig().shinyLore.get(id)) {
                    tooltip.add(TextUtils.deserialize(line));
                }
            }
        }
    }
}
