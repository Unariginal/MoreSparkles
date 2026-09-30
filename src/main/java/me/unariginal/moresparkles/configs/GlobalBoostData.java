package me.unariginal.moresparkles.configs;

import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Map;

public class GlobalBoostData {
    @Nullable
    public Map<BoostType, Boost> activeBoosts;
    @Nullable
    public Map<BoostType, LinkedList<Boost>> queuedBoosts;

    public GlobalBoostData(@Nullable Map<BoostType, Boost> activeBoosts, @Nullable Map<BoostType, LinkedList<Boost>> queuedBoosts) {
        this.activeBoosts = activeBoosts;
        this.queuedBoosts = queuedBoosts;
    }
}
