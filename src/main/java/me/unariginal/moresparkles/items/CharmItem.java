package me.unariginal.moresparkles.items;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import me.unariginal.moresparkles.configs.ItemsConfig;
import me.unariginal.moresparkles.utils.TextUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.unariginal.moresparkles.configs.ConfigManager.ITEMS_CONFIG;

public class CharmItem extends SimplePolymerItem {
    private final PolymerModelData modelData;
    protected final String charmId;
    private final List<String> lore;

    public CharmItem(Settings settings, Item polymerItem, PolymerModelData modelData, String charmId, List<String> lore) {
        super(settings, polymerItem);
        this.modelData = modelData;
        this.charmId = charmId;
        this.lore = lore;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
        return this.modelData.value();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        for (String line : lore) {
            tooltip.add(TextUtils.deserialize(line));
        }
    }

    public String getCharmId() {
        return charmId;
    }

    public float getMultiplier() {
        if (ITEMS_CONFIG.charms != null) {
            ItemsConfig.CharmData charmData = ITEMS_CONFIG.charms.get(charmId);
            if (charmData != null) {
                return charmData.multiplier;
            }
        }
        return 1.0F;
    }
}
