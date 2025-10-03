package me.unariginal.moresparkles.configs;

import com.google.gson.*;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.data.ShinyBoost;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class Config {
    public boolean pausePlayerBoostsDuringGlobalBoost = true;
    public boolean allowQueuedBoosts = true;
    public boolean pausePlayerBoostsOnDisconnect = true;

    public Config() {
        try {
            loadConfig();
        } catch (IOException e) {
            MoreSparkles.logError("Unable to load config file. Error: " + e.getMessage());
        }
    }

    public void loadConfig() throws IOException {
        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        if (!rootFolder.exists())
            rootFolder.mkdir();

        File configFile = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/config.json").toFile();
        JsonObject newRoot = new JsonObject();
        JsonObject root = new JsonObject();
        if (configFile.exists())
            root = JsonParser.parseReader(new FileReader(configFile)).getAsJsonObject();

        if (root.has("debug"))
            MoreSparkles.DEBUG = root.get("debug").getAsBoolean();
        newRoot.addProperty("debug", MoreSparkles.DEBUG);

        if (root.has("pause_player_boosts_during_global_boost"))
            pausePlayerBoostsDuringGlobalBoost = root.get("pause_player_boosts_during_global_boost").getAsBoolean();
        newRoot.addProperty("pause_player_boosts_during_global_boost", pausePlayerBoostsDuringGlobalBoost);

        if (root.has("allow_queued_boosts"))
            allowQueuedBoosts = root.get("allow_queued_boosts").getAsBoolean();
        newRoot.addProperty("allow_queued_boosts", allowQueuedBoosts);

        if (root.has("pause_player_boosts_on_disconnect"))
            pausePlayerBoostsOnDisconnect = root.get("pause_player_boosts_on_disconnect").getAsBoolean();
        newRoot.addProperty("pause_player_boosts_on_disconnect", pausePlayerBoostsOnDisconnect);

        JsonObject activeGlobalBoost = new JsonObject();
        if (root.has("active_global_boost"))
            activeGlobalBoost = root.get("active_global_boost").getAsJsonObject();

        if (activeGlobalBoost.has("multiplier") &&
            activeGlobalBoost.has("time_remaining") &&
            activeGlobalBoost.has("duration")) {
            float multiplier = activeGlobalBoost.get("multiplier").getAsFloat();
            long timeRemaining = activeGlobalBoost.get("time_remaining").getAsLong();
            int duration = activeGlobalBoost.get("duration").getAsInt();
            MoreSparkles.INSTANCE.globalBoost = new ShinyBoost(null, multiplier, duration, timeRemaining);
        }
        if (MoreSparkles.INSTANCE.globalBoost != null) {
            activeGlobalBoost.addProperty("multiplier", MoreSparkles.INSTANCE.globalBoost.multiplier);
            activeGlobalBoost.addProperty("time_remaining", MoreSparkles.INSTANCE.globalBoost.timeRemaining);
            activeGlobalBoost.addProperty("duration", MoreSparkles.INSTANCE.globalBoost.duration);
        }
        newRoot.add("active_global_boost", activeGlobalBoost);

        JsonArray queuedGlobalBoostsArray = new JsonArray();
        if (root.has("queued_global_boosts"))
            queuedGlobalBoostsArray = root.get("queued_global_boosts").getAsJsonArray();

        for (JsonElement queueElement : queuedGlobalBoostsArray) {
            JsonObject queuedGlobalBoostObject = queueElement.getAsJsonObject();
            if (!(queuedGlobalBoostObject.has("multiplier") && queuedGlobalBoostObject.has("duration")))
                continue;
            float multiplier = queuedGlobalBoostObject.get("multiplier").getAsFloat();
            int duration = queuedGlobalBoostObject.get("duration").getAsInt();
            MoreSparkles.INSTANCE.queuedGlobalBoosts.add(new ShinyBoost(null, multiplier, duration));
        }

        queuedGlobalBoostsArray = new JsonArray();
        for (ShinyBoost queuedBoost : MoreSparkles.INSTANCE.queuedGlobalBoosts) {
            JsonObject queuedBoostObject = new JsonObject();
            queuedBoostObject.addProperty("multiplier", queuedBoost.multiplier);
            queuedBoostObject.addProperty("duration", queuedBoost.duration);
            queuedGlobalBoostsArray.add(queuedBoostObject);
        }
        newRoot.add("queued_global_boosts", queuedGlobalBoostsArray);

        configFile.delete();
        configFile.createNewFile();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Writer writer = new FileWriter(configFile);
        gson.toJson(newRoot, writer);
        writer.close();
    }

    public void saveGlobalBoostData() {
        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        if (!rootFolder.exists())
            rootFolder.mkdir();

        File configFile = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/config.json").toFile();
        if (!configFile.exists()) {
            MoreSparkles.logError("Unable to save global boost data. File does not exist.");
            return;
        }
        JsonObject root = null;
        try {
            root = JsonParser.parseReader(new FileReader(configFile)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            MoreSparkles.logError("Unable to save global boost data. Error: " + e.getMessage());
        }

        if (root == null) {
            MoreSparkles.logError("Unable to save global boost data. Root is null");
            return;
        }

        JsonObject activeGlobalBoost = new JsonObject();

        if (MoreSparkles.INSTANCE.globalBoost != null) {
            activeGlobalBoost.addProperty("multiplier", MoreSparkles.INSTANCE.globalBoost.multiplier);
            activeGlobalBoost.addProperty("time_remaining", MoreSparkles.INSTANCE.globalBoost.timeRemaining);
            activeGlobalBoost.addProperty("duration", MoreSparkles.INSTANCE.globalBoost.duration);
        }
        root.add("active_global_boost", activeGlobalBoost);

        JsonArray queuedGlobalBoostsArray = new JsonArray();
        for (ShinyBoost queuedBoost : MoreSparkles.INSTANCE.queuedGlobalBoosts) {
            JsonObject queuedBoostObject = new JsonObject();
            queuedBoostObject.addProperty("multiplier", queuedBoost.multiplier);
            queuedBoostObject.addProperty("duration", queuedBoost.duration);
            queuedGlobalBoostsArray.add(queuedBoostObject);
        }
        root.add("queued_global_boosts", queuedGlobalBoostsArray);

        try {
            configFile.delete();
            configFile.createNewFile();
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            Writer writer = new FileWriter(configFile);
            gson.toJson(root, writer);
            writer.close();
        } catch (IOException e) {
            MoreSparkles.logError("Unable to save global boost data. Error: " + e.getMessage());
        }
    }
}
