package me.unariginal.moresparkles.configs;

import me.unariginal.moresparkles.data.BoostType;
import net.kyori.adventure.bossbar.BossBar;

import java.util.Map;

public class MessagesConfig {
    public String prefix = "<gray>[<light_purple>MoreSparkles<gray>]";
    public Messages messages;

    public static class Messages {
        public String commandReload = "%prefix% <green>Reloaded!";
        public String commandCheckRate = "%prefix% <gray>Base: <yellow>1/%base_shiny_rate%<gray> | Effective: <yellow>1/%player.shiny_rate%";
        public String playerBoostStarted = "%prefix% <green>Started a %multiplier%x boost for %duration% for %player.name%!";
        public String playerBoostAddedToQueue = "%prefix% <green>Added a %multiplier%x boost with a %duration% duration to %player.name%'s queue!";
        public String playerBoostStopped = "%prefix% <green>Stopped %player.name%'s current boost!";
        public String playerQueueCleared = "%prefix% <green>Cleared %player.name%'s queued boosts!";
        public String playerBoostInformation = "%prefix% <gray>Player: %player.name% | Multiplier: %multiplier%x | Timer: %time_remaining% / %duration%";
        public String globalBoostStarted = "%prefix% <green>Started a global %multiplier%x boost for %duration%!";
        public String globalBoostAddedToQueue = "%prefix% <green>Added a global %multiplier%x boost with a %duration% duration to queue!";
        public String globalBoostStopped = "%prefix% <green>Stopped the current global boost!";
        public String globalQueueCleared = "%prefix% <green>Cleared the global queued boosts!";
        public String globalBoostInformation = "%prefix% <gray>Multiplier: %multiplier%x | Timer: %time_remaining% / %duration%";
        public String noActiveBoosts = "%prefix% <red>No Active Boosts";
        public String noQueuedBoosts = "%prefix% <red>No Queued Boosts";
        public String areaInfo = "%prefix% <gray>Area \"%area%\": %area.type% type | %multiplier%x multiplier";
        public String notInArea = "%prefix% <red>This is not a boost area!";
    }

    public Map<String, BossbarSettings> globalBoostBossbars;
    public Map<String, BossbarSettings> playerBoostBossbars;

    public static class BossbarSettings {
        public BoostType boostType;
        public BossBar.Color barColor = BossBar.Color.PINK;
        public BossBar.Overlay barOverlay = BossBar.Overlay.PROGRESS;
        public String barText = "<gray>GLOBAL %multiplier%x Boost | %time_remaining% Remaining";
        public String barTextPaused = "<gray>GLOBAL %multiplier%x Boost | PAUSED";
    }
}
