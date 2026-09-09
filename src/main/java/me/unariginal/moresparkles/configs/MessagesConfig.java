package me.unariginal.moresparkles.configs;

import me.unariginal.moresparkles.data.BoostType;
import net.kyori.adventure.bossbar.BossBar;

import java.util.Map;

public class MessagesConfig {
    public String prefix = "<gray>[<light_purple>MoreSparkles<gray>]";
    public Messages messages;
    public Map<String, BossbarSettings> globalBoostBossbars;
    public Map<String, BossbarSettings> playerBoostBossbars;

    public static class Messages {
        public String commandReload = "%prefix% <green>Reloaded!";
        public String commandCheckRate = "%prefix% <gray>Base: <yellow>1/%base_shiny_rate%<gray> | Effective: <yellow>1/%player_shiny_rate%";
        public String playerBoostStarted = "%prefix% <green>Started a %boost_multiplier%x %boost_type% boost for %boost_duration% for %player_name%!";
        public String playerBoostAddedToQueue = "%prefix% <green>Added a %boost_multiplier%x %boost_type% boost with a %boost_duration% duration to %player_name%'s queue!";
        public String playerBoostStopped = "%prefix% <green>Stopped %player_name%'s current %boost_type% boost!";
        public String playerQueueCleared = "%prefix% <green>Cleared %player_name%'s queued %boost_type% boosts!";
        public String playerBoostInformation = "%prefix% <gray>Player: %player_name% | Boost: %boost_multiplier%x %boost_type% | Timer: %boost_time_remaining% / %boost_duration%";
        public String globalBoostStarted = "%prefix% <green>Started a global %boost_multiplier%x %boost_type% boost for %boost_duration%!";
        public String globalBoostAddedToQueue = "%prefix% <green>Added a global %boost_multiplier%x %boost_type% boost with a %boost_duration% duration to queue!";
        public String globalBoostStopped = "%prefix% <green>Stopped the current global %boost_type% boost!";
        public String globalQueueCleared = "%prefix% <green>Cleared the global queued %boost_type% boosts!";
        public String globalBoostInformation = "%prefix% <gray>Global Boost: %boost_multiplier%x %boost_type% | Timer: %boost_time_remaining% / %boost_duration%";
        public String noActiveBoosts = "%prefix% <red>No Active Boosts";
        public String noQueuedBoosts = "%prefix% <red>No Queued Boosts";
        public String areaInfo = "%prefix% <gray>Area \"%boost_area_name%\": %boost_area_type% type | %boost_area_multiplier%x multiplier";
        public String notInArea = "%prefix% <red>This is not a boost area!";
    }

    public static class BossbarSettings {
        public BoostType boostType;
        public BossBar.Color barColor = BossBar.Color.PINK;
        public BossBar.Overlay barOverlay = BossBar.Overlay.PROGRESS;
        public String barText = "<gray>%boost_multiplier%x %boost_type% Boost | %boost_time_remaining% Remaining";
        public String barTextPaused = "<gray>%boost_multiplier%x %boost_type% Boost | PAUSED";
    }
}
