package me.unariginal.moresparkles.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.unariginal.moresparkles.MoreSparkles;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.bossbar.BossBar;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MessagesConfig {
    public String prefix = "<gray>[<light_purple>MoreSparkles<gray>]";
    public Map<String, String> messages = new HashMap<>();
    public BossBar.Color globalBarColor = BossBar.Color.PINK;
    public BossBar.Color playerBarColor = BossBar.Color.PINK;
    public BossBar.Overlay globalBarOverlay = BossBar.Overlay.PROGRESS;
    public BossBar.Overlay playerBarOverlay = BossBar.Overlay.PROGRESS;
    public String globalBarText = "<gray>GLOBAL %multiplier%x Shiny Boost | %time_remaining% Remaining";
    public String playerBarText = "<gray>%multiplier%x Shiny Boost | %time_remaining% Remaining";
    public String globalBarTextPaused = "<gray>GLOBAL %multiplier%x Shiny Boost | PAUSED";
    public String playerBarTextPaused = "<gray>%multiplier%x Shiny Boost | PAUSED";

    public MessagesConfig() {
        try {
            loadConfig();
        } catch (IOException e) {
            MoreSparkles.logError("Unable to load messages config file. Error: " + e.getMessage());
        }
    }

    private void loadConfig() throws IOException {
        fillMessages();

        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        if (!rootFolder.exists())
            rootFolder.mkdir();

        File configFile = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/messages.json").toFile();
        JsonObject newRoot = new JsonObject();
        JsonObject root = new JsonObject();
        if (configFile.exists())
            root = JsonParser.parseReader(new FileReader(configFile)).getAsJsonObject();

        if (root.has("prefix"))
            prefix = root.get("prefix").getAsString();
        newRoot.addProperty("prefix", prefix);

        JsonObject messagesObject = new JsonObject();
        if (root.has("messages"))
            messagesObject = root.get("messages").getAsJsonObject();

        for (String key : messagesObject.keySet()) {
            messages.put(key, messagesObject.get(key).getAsString());
        }
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            messagesObject.addProperty(entry.getKey(), entry.getValue());
        }
        newRoot.add("messages", messagesObject);

        JsonObject bossBarObject = new JsonObject();
        if (root.has("bossbar_settings"))
            bossBarObject = root.get("bossbar_settings").getAsJsonObject();

        JsonObject globalBarObject = new JsonObject();
        if (bossBarObject.has("global_boost"))
            globalBarObject = bossBarObject.get("global_boost").getAsJsonObject();

        if (globalBarObject.has("bar_color")) {
            String globalBarColorString = globalBarObject.get("bar_color").getAsString();
            if (Arrays.stream(BossBar.Color.values()).anyMatch(value -> value.name().equalsIgnoreCase(globalBarColorString))) {
                globalBarColor = BossBar.Color.valueOf(globalBarColorString.toUpperCase());
            }
        }
        globalBarObject.addProperty("bar_color", globalBarColor.toString());

        if (globalBarObject.has("bar_overlay")) {
            String globalBarOverlayString = globalBarObject.get("bar_overlay").getAsString();
            if (Arrays.stream(BossBar.Overlay.values()).anyMatch(value -> value.name().equalsIgnoreCase(globalBarOverlayString))) {
                globalBarOverlay = BossBar.Overlay.valueOf(globalBarOverlayString.toUpperCase());
            }
        }
        globalBarObject.addProperty("bar_overlay", globalBarOverlay.toString());

        if (globalBarObject.has("bar_text"))
            globalBarText = globalBarObject.get("bar_text").getAsString();
        globalBarObject.addProperty("bar_text", globalBarText);

        if (globalBarObject.has("bar_text_paused"))
            globalBarTextPaused = globalBarObject.get("bar_text_paused").getAsString();
        globalBarObject.addProperty("bar_text_paused", globalBarTextPaused);

        bossBarObject.add("global_boost", globalBarObject);

        JsonObject playerBarObject = new JsonObject();
        if (bossBarObject.has("player_boost"))
            playerBarObject = bossBarObject.get("player_boost").getAsJsonObject();

        if (playerBarObject.has("bar_color")) {
            String playerBarColorString = playerBarObject.get("bar_color").getAsString();
            if (Arrays.stream(BossBar.Color.values()).anyMatch(value -> value.name().equalsIgnoreCase(playerBarColorString))) {
                playerBarColor = BossBar.Color.valueOf(playerBarColorString.toUpperCase());
            }
        }
        playerBarObject.addProperty("bar_color", playerBarColor.toString());

        if (playerBarObject.has("bar_overlay")) {
            String playerBarOverlayString = playerBarObject.get("bar_overlay").getAsString();
            if (Arrays.stream(BossBar.Overlay.values()).anyMatch(value -> value.name().equalsIgnoreCase(playerBarOverlayString))) {
                playerBarOverlay = BossBar.Overlay.valueOf(playerBarOverlayString.toUpperCase());
            }
        }
        playerBarObject.addProperty("bar_overlay", playerBarOverlay.toString());

        if (playerBarObject.has("bar_text"))
            playerBarText = playerBarObject.get("bar_text").getAsString();
        playerBarObject.addProperty("bar_text", playerBarText);

        if (playerBarObject.has("bar_text_paused"))
            playerBarTextPaused = playerBarObject.get("bar_text_paused").getAsString();
        playerBarObject.addProperty("bar_text_paused", playerBarTextPaused);

        bossBarObject.add("player_boost", playerBarObject);
        newRoot.add("bossbar_settings", bossBarObject);

        configFile.delete();
        configFile.createNewFile();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Writer writer = new FileWriter(configFile);
        gson.toJson(newRoot, writer);
        writer.close();
    }

    private void fillMessages() {
        messages.clear();
        messages.put("command_reload", "%prefix% <green>Reloaded!");
        messages.put("player_boost_started", "%prefix% <green>Started a %multiplier%x boost for %duration% for %player.name%!");
        messages.put("player_boost_added_to_queue", "%prefix% <green>Added a %multiplier%x boost with a %duration% duration to %player.name%'s queue!");
        messages.put("global_boost_started", "%prefix% <green>Started a global %multiplier%x boost for %duration%!");
        messages.put("global_boost_added_to_queue", "%prefix% <green>Added a global %multiplier%x boost with a %duration% duration to queue!");
        messages.put("player_boost_stopped", "%prefix% <green>Stopped %player.name%'s current boost!");
        messages.put("global_boost_stopped", "%prefix% <green>Stopped the current global boost!");
        messages.put("command_check_rate", "%prefix% <gray>Base: <yellow>1/%base_shiny_rate%<gray> | Effective: <yellow>1/%player.shiny_rate%");
        messages.put("player_queue_cleared", "%prefix% <green>Cleared %player.name%'s queued boosts!");
        messages.put("global_queue_cleared", "%prefix% <green>Cleared the global queued boosts!");
        messages.put("player_boost_information", "%prefix% <gray>Player: %player.name% | Multiplier: %multiplier%x | Timer: %time_remaining% / %duration%");
        messages.put("global_boost_information", "%prefix% <gray>Multiplier: %multiplier%x | Timer: %time_remaining% / %duration%");
        messages.put("no_active_boosts", "%prefix% <red>No Active Boosts");
        messages.put("no_queued_boosts", "%prefix% <red>No Queued boosts");
        messages.put("area_info", "%prefix% <gray>Area \"%area%\": %area.type% type | %multiplier%x multiplier");
        messages.put("not_in_area", "%prefix% <red>You're not in an area!");
    }

    public String getMessage(String id) {
        if (messages.containsKey(id)) {
            return messages.get(id);
        }
        return "null";
    }
}
