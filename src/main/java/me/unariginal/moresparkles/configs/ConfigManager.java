package me.unariginal.moresparkles.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static me.unariginal.moresparkles.MoreSparkles.LOGGER;
import static me.unariginal.moresparkles.utils.GsonUtils.gson;

public class ConfigManager {
    public static File configDir;

    public static Config CONFIG;
    public static MessagesConfig MESSAGES;
    public static ItemsConfig ITEMS_CONFIG;
    public static Map<String, BoostArea> BOOST_AREAS = new HashMap<>();

    public static void load() {
        configDir = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        generateDefaultFiles();

        CONFIG = loadFile("config.json", Config.class);
        MESSAGES = loadFile("messages.json", MessagesConfig.class);
        ITEMS_CONFIG = loadFile("items.json", ItemsConfig.class);
        BOOST_AREAS = loadMapFile("boost_areas.json", BoostArea.class);
    }

    public static void generateDefaultFiles() {
        generateDefaultFile("config.json");
        generateDefaultFile("messages.json");
        generateDefaultFile("items.json");
        generateDefaultFile("boost_areas.json");
    }

    public static <T> Map<String, T> loadMapFile(String fileName, Class<T> clazz) {
        File file = new File(configDir, fileName);
        Type mapType = TypeToken.getParameterized(Map.class, String.class, clazz).getType();

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, mapType);
            } catch (IOException e) {
                LOGGER.error("[MoreSparkles] Failed to load config file {}", fileName, e);
            }
        }
        LOGGER.error("[MoreSparkles] Error loading config file {}. File does not exist!", fileName);
        return Map.of();
    }

    public static <T> T loadFile(String fileName, Class<T> clazz) {
        File file = new File(configDir, fileName);
        if (file.exists()) {
            try {
                String jsonString = JsonParser.parseReader(new FileReader(file)).toString();
                return gson.fromJson(jsonString, clazz);
            } catch (IOException e) {
                LOGGER.error("[MoreSparkles] Failed to load config file {}", fileName, e);
            }
        }
        LOGGER.error("[MoreSparkles] Error loading config file {}. File does not exist!", fileName);
        return null;
    }

    public static void generateDefaultFile(String fileName) {
        File file = new File(configDir, fileName);
        if (file.exists()) return;
        try {
            Files.createDirectories(file.getParentFile().toPath());
            Files.createFile(file.toPath());
            writeFile(file, getDefaultJsonString(fileName));
        } catch (IOException e) {
            LOGGER.error("[MoreSparkles] Failed to create directories for file {}", file.getName(), e);
        }
    }

    public static String getDefaultJsonString(String fileName) {
        InputStream in = MoreSparkles.class.getResourceAsStream("/sparkles_configs/" + fileName);
        assert in != null;
        return gson.toJson(JsonParser.parseReader(new InputStreamReader(in)));
    }

    public static void fillMissingWithDefaults(String fileName, String defaultFileName, boolean isMapFile) {
        try {
            File file = new File(configDir, fileName);
            if (defaultFileName == null) defaultFileName = fileName;
            InputStream in = MoreSparkles.class.getResourceAsStream("/sparkles_configs/" + defaultFileName);
            assert in != null;
            FileReader fileReader = new FileReader(file);
            JsonObject defaultJson = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
            JsonObject targetJson = JsonParser.parseReader(fileReader).getAsJsonObject();
            mergeJsonObjects(targetJson, defaultJson, isMapFile);
            writeFile(file, gson.toJson(targetJson));
            in.close();
            fileReader.close();
        } catch (IOException e) {
            LOGGER.error("[MoreSparkles] Failed to parse json for filling defaults.", e);
        }
    }

    private static void mergeJsonObjects(JsonObject target, JsonObject defaults, boolean isMapFile) {
        if (!isMapFile) {
            for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
                String key = entry.getKey();
                JsonElement defaultValue = entry.getValue();

//                // Special handling for maps
//                if ("raid_balls".equals(key) && target.has(key) && target.get(key).isJsonObject() && defaultValue.isJsonObject()) {
//                    JsonObject targetMap = target.getAsJsonObject(key);
//                    JsonObject defaultMap = defaultValue.getAsJsonObject();
//                    Map.Entry<String, JsonElement> defaultMapEntry = defaultMap.entrySet().stream().findFirst().isPresent() ? defaultMap.entrySet().stream().findFirst().get() : null;
//
//                    // Only merge existing map entries
//                    if (defaultMapEntry != null && defaultMapEntry.getValue().isJsonObject()) {
//                        for (Map.Entry<String, JsonElement> mapEntry : targetMap.entrySet()) {
//                            if (mapEntry.getValue().isJsonObject()) {
//                                mergeJsonObjects(mapEntry.getValue().getAsJsonObject(), defaultMapEntry.getValue().getAsJsonObject(), false);
//                            }
//                        }
//                    }
//
//                    continue;
//                }

                if (!target.has(key)) {
                    target.add(key, defaultValue.deepCopy());
                } else {
                    JsonElement targetValue = target.get(key);
                    if (!targetValue.isJsonArray() && targetValue.isJsonObject() && defaultValue.isJsonObject()) {
                        mergeJsonObjects(targetValue.getAsJsonObject(), defaultValue.getAsJsonObject(), false);
                    }
                }
            }
        } else {
            Map.Entry<String, JsonElement> defaultMapEntry = defaults.entrySet().stream().findFirst().isPresent() ? defaults.entrySet().stream().findFirst().get() : null;
            if (defaultMapEntry != null && defaultMapEntry.getValue().isJsonObject()) {
                for (Map.Entry<String, JsonElement> mapEntry : target.entrySet()) {
                    if (mapEntry.getValue().isJsonObject()) {
                        mergeJsonObjects(mapEntry.getValue().getAsJsonObject(), defaultMapEntry.getValue().getAsJsonObject(), false);
                    }
                }
            }
        }
    }

    public static void writeFile(File file, String content) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            LOGGER.error("[MoreSparkles] Failed to write to file {}", file.getName(), e);
        }
    }
}
