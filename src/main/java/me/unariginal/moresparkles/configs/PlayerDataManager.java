package me.unariginal.moresparkles.configs;

import com.google.gson.*;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.data.ShinyBoost;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;

public class PlayerDataManager {
    public static ShinyBoost loadPlayerBoostData(ServerPlayerEntity player) {
        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        if (!rootFolder.exists())
            rootFolder.mkdir();

        File playersFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/playerdata").toFile();
        if (!playersFolder.exists())
            playersFolder.mkdir();

        File playerFile = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/playerdata/" + player.getUuidAsString() + ".json").toFile();
        if (playerFile.exists()) {
            try {
                JsonObject root = JsonParser.parseReader(new FileReader(playerFile)).getAsJsonObject();
                if (!root.has("active_boost")) {
                    MoreSparkles.LOGGER.error("[MoreSparkles] Failed To Load Player Data File: {}", playerFile.getName());
                    MoreSparkles.LOGGER.error(" - File is missing data!");
                    return null;
                }
                JsonObject activeBoost = root.getAsJsonObject("active_boost");
                if (!(activeBoost.has("multiplier") &&
                        activeBoost.has("time_remaining") &&
                        activeBoost.has("duration"))) {
                    MoreSparkles.LOGGER.error("[MoreSparkles] Failed To Load Player Data File: {}", playerFile.getName());
                    MoreSparkles.LOGGER.error(" - File is missing data!");
                    return null;
                }

                float multiplier = activeBoost.get("multiplier").getAsFloat();
                long time_remaining = activeBoost.get("time_remaining").getAsLong();
                int duration = activeBoost.get("duration").getAsInt();

                if (MoreSparkles.INSTANCE.getConfig().allowQueuedBoosts) {
                    if (root.has("queued_boosts")) {
                        MoreSparkles.INSTANCE.clearQueue(player);
                        JsonArray queuedBoostsArray = root.getAsJsonArray("queued_boosts");
                        for (JsonElement queuedBoostElement : queuedBoostsArray) {
                            JsonObject queuedBoost = queuedBoostElement.getAsJsonObject();
                            if (!(queuedBoost.has("multiplier") &&
                                    queuedBoost.has("duration"))) continue;
                            MoreSparkles.INSTANCE.queuedBoosts.add(new ShinyBoost(player, queuedBoost.get("multiplier").getAsFloat(), queuedBoost.get("duration").getAsInt()));
                        }
                    }
                }

                return new ShinyBoost(player, multiplier, duration, time_remaining);
            } catch (FileNotFoundException e) {
                MoreSparkles.LOGGER.error("[MoreSparkles] Failed To Load Player Data File: {}", playerFile.getName());
                MoreSparkles.LOGGER.error(" - {}", e.getMessage());
            }
        }
        return null;
    }

    public static void savePlayerBoostData(ServerPlayerEntity player) {
        ShinyBoost shinyBoost = MoreSparkles.INSTANCE.getActiveBoost(player);
        if (shinyBoost == null) {
            deletePlayerBoostFile(player);
            return;
        }

        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        if (!rootFolder.exists())
            rootFolder.mkdir();

        File playersFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/playerdata").toFile();
        if (!playersFolder.exists())
            playersFolder.mkdir();

        File playerFile = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/playerdata/" + player.getUuidAsString() + ".json").toFile();

        JsonObject root = new JsonObject();
        root.addProperty("uuid", player.getUuidAsString());
        root.addProperty("username", player.getNameForScoreboard());

        JsonObject activeBoost = new JsonObject();
        activeBoost.addProperty("multiplier", shinyBoost.multiplier);
        activeBoost.addProperty("time_remaining", shinyBoost.timeRemaining);
        activeBoost.addProperty("duration", shinyBoost.duration);
        root.add("active_boost", activeBoost);

        JsonArray queuedBoostsArray = new JsonArray();
        for (ShinyBoost queuedBoost : MoreSparkles.INSTANCE.getQueuedBoosts(player)) {
            JsonObject queuedBoostObject = new JsonObject();
            queuedBoostObject.addProperty("multiplier", queuedBoost.multiplier);
            queuedBoostObject.addProperty("duration", queuedBoost.duration);
            queuedBoostsArray.add(queuedBoostObject);
        }
        root.add("queued_boosts", queuedBoostsArray);

        try {
            playerFile.delete();
            playerFile.createNewFile();
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            Writer writer = new FileWriter(playerFile);
            gson.toJson(root, writer);
            writer.close();
        } catch (IOException e) {
            MoreSparkles.LOGGER.error("[MoreSparkles] Failed To Save Player Data File: {}", playerFile.getName());
            MoreSparkles.LOGGER.error(e.getMessage());
        }
    }

    public static void savePlayerBoostData(ShinyBoost shinyBoost) {
        if (shinyBoost.playerUUID == null) return;

        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        if (!rootFolder.exists())
            rootFolder.mkdir();

        File playersFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/playerdata").toFile();
        if (!playersFolder.exists())
            playersFolder.mkdir();

        File playerFile = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/playerdata/" + shinyBoost.playerUUID + ".json").toFile();

        JsonObject root = new JsonObject();
        ServerPlayerEntity player = MoreSparkles.INSTANCE.getServer().getPlayerManager().getPlayer(shinyBoost.playerUUID);
        root.addProperty("uuid", shinyBoost.playerUUID.toString());
        if (player != null) {
            root.addProperty("username", player.getNameForScoreboard());
        }

        JsonObject activeBoost = new JsonObject();
        activeBoost.addProperty("multiplier", shinyBoost.multiplier);
        activeBoost.addProperty("time_remaining", shinyBoost.timeRemaining);
        activeBoost.addProperty("duration", shinyBoost.duration);
        root.add("active_boost", activeBoost);

        JsonArray queuedBoostsArray = new JsonArray();
        for (ShinyBoost queuedBoost : MoreSparkles.INSTANCE.getQueuedBoosts(shinyBoost.playerUUID)) {
            JsonObject queuedBoostObject = new JsonObject();
            queuedBoostObject.addProperty("multiplier", queuedBoost.multiplier);
            queuedBoostObject.addProperty("duration", queuedBoost.duration);
            queuedBoostsArray.add(queuedBoostObject);
        }
        root.add("queued_boosts", queuedBoostsArray);

        try {
            playerFile.delete();
            playerFile.createNewFile();
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            Writer writer = new FileWriter(playerFile);
            gson.toJson(root, writer);
            writer.close();
        } catch (IOException e) {
            MoreSparkles.LOGGER.error("[MoreSparkles] Failed To Save Player Data File: {}", playerFile.getName());
            MoreSparkles.LOGGER.error(e.getMessage());
        }
    }

    public static void deletePlayerBoostFile(ServerPlayerEntity player) {
        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        if (!rootFolder.exists()) return;

        File playersFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/playerdata").toFile();
        if (!playersFolder.exists()) return;

        File playerFile = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/playerdata/" + player.getUuidAsString() + ".json").toFile();
        if (playerFile.exists()) playerFile.delete();
    }
}
