package me.unariginal.moresparkles.configs;

import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;

import java.util.LinkedList;
import java.util.Map;

public class PlayerData {
    public Map<BoostType, Boost> activeBoosts;
    public Map<BoostType, LinkedList<Boost>> queuedBoosts;

    public PlayerData(Map<BoostType, Boost> activeBoosts, Map<BoostType, LinkedList<Boost>> queuedBoosts) {
        this.activeBoosts = activeBoosts;
        this.queuedBoosts = queuedBoosts;
    }
}
