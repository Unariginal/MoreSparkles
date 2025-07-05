package me.unariginal.moresparkles.configs;

import com.google.gson.*;
import me.unariginal.moresparkles.MoreSparkles;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemsConfig {
    public Map<String, List<String>> shinyLore = new HashMap<>(
            Map.of("shiny_charm", List.of("<gray>Having one of these mysterious shining charms makes it more likely you'll encounter Shiny Pokémon in the wild."))
    );
    public Map<String, Float> shinyMultipliers = new HashMap<>(
            Map.of("shiny_charm", 2.0F)
    );

    public ItemsConfig() {
        try {
            loadConfig();
        } catch (IOException e) {
            MoreSparkles.logError("Unable to load items config file. Error: " + e.getMessage());
        }
    }

    private void loadConfig() throws IOException {
        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        if (!rootFolder.exists())
            rootFolder.mkdir();

        File configFile = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/items.json").toFile();
        JsonObject root = new JsonObject();
        JsonObject newRoot = new JsonObject();
        if (configFile.exists())
            root = JsonParser.parseReader(new FileReader(configFile)).getAsJsonObject();

        JsonObject shinyCharmsObject = new JsonObject();
        if (root.has("shiny_charms")) {
            shinyCharmsObject = root.getAsJsonObject("shiny_charms");
            shinyMultipliers.clear();
            shinyLore.clear();
        }

        for (String key : shinyCharmsObject.keySet()) {
            JsonObject shinyCharmItem = shinyCharmsObject.getAsJsonObject(key);
            if (!shinyCharmItem.has("multiplier")) continue;
            if (shinyCharmItem.has("lore")) {
                List<String> lore = shinyCharmItem.getAsJsonArray("lore").asList().stream().map(JsonElement::getAsString).toList();
                if (!lore.isEmpty())
                    shinyLore.put(key, lore);
            }
            float multiplier = shinyCharmItem.get("multiplier").getAsFloat();
            shinyMultipliers.put(key, multiplier);
        }

        for (String key : shinyMultipliers.keySet()) {
            JsonObject shinyCharmItem = new JsonObject();
            List<String> lore = new ArrayList<>();
            if (shinyLore.containsKey(key))
                lore = shinyLore.get(key);
            JsonArray shinyLore = new JsonArray();
            for (String line : lore) {
                shinyLore.add(line);
            }
            shinyCharmItem.addProperty("multiplier", shinyMultipliers.get(key));
            shinyCharmItem.add("lore", shinyLore);
            shinyCharmsObject.add(key, shinyCharmItem);
        }
        newRoot.add("shiny_charms", shinyCharmsObject);

        configFile.delete();
        configFile.createNewFile();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Writer writer = new FileWriter(configFile);
        gson.toJson(newRoot, writer);
        writer.close();
    }
}
