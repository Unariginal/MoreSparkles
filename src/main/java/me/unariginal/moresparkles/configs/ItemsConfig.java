package me.unariginal.moresparkles.configs;

import me.unariginal.moresparkles.data.BoostType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ItemsConfig {
    @Nullable
    public Map<String, CharmData> charms;

    public static class CharmData {
        public BoostType boostType;
        public float multiplier;
        @Nullable
        public List<String> lore;
    }
}
