package me.unariginal.moresparkles.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.data.Boost;
import me.unariginal.moresparkles.data.BoostType;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

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

        fillMissingWithDefaults("config.json", null, false);
        fillMissingWithDefaults("messages.json", null, false);
        fillMissingWithDefaults("items.json", null, false);

        CONFIG = loadOrFallback("config.json", Config.class, CONFIG);
        MESSAGES = loadOrFallback("messages.json", MessagesConfig.class, MESSAGES);
        ITEMS_CONFIG = loadOrFallback("items.json", ItemsConfig.class, ITEMS_CONFIG);

        Map<String, BoostArea> boostAreas = loadMapFile("boost_areas.json", BoostArea.class);
        if (boostAreas != null) {
            boostAreas.values().removeIf(Objects::isNull);
            BOOST_AREAS = boostAreas;
        } else {
            LOGGER.error("[MoreSparkles] Keeping the previously loaded boost areas until boost_areas.json is fixed.");
        }
    }

    public static void generateDefaultFiles() {
        generateDefaultFile("config.json");
        generateDefaultFile("messages.json");
        generateDefaultFile("items.json");
        generateDefaultFile("boost_areas.json");
    }

    private static <T> T loadOrFallback(String fileName, Class<T> clazz, @Nullable T previous) {
        T loaded = loadFile(fileName, clazz);
        if (loaded != null) return loaded;
        if (previous != null) {
            LOGGER.error("[MoreSparkles] Keeping the previously loaded {} until it is fixed.", fileName);
            return previous;
        }
        LOGGER.error("[MoreSparkles] Using the default {} until it is fixed.", fileName);
        return gson.fromJson(getDefaultJsonString(fileName), clazz);
    }

    @Nullable
    public static <T> Map<String, T> loadMapFile(String fileName, Class<T> clazz) {
        Type mapType = TypeToken.getParameterized(Map.class, String.class, clazz).getType();
        return loadFile(fileName, mapType);
    }

    @Nullable
    public static <T> T loadFile(String fileName, Class<T> clazz) {
        return loadFile(fileName, (Type) clazz);
    }

    @Nullable
    private static <T> T loadFile(String fileName, Type type) {
        File file = new File(configDir, fileName);
        if (!file.exists()) {
            LOGGER.error("[MoreSparkles] Error loading config file {}. File does not exist!", fileName);
            return null;
        }
        try (FileReader reader = new FileReader(file)) {
            T result = gson.fromJson(reader, type);
            if (result == null) LOGGER.error("[MoreSparkles] Config file {} is empty!", fileName);
            return result;
        } catch (IOException | JsonParseException e) {
            LOGGER.error("[MoreSparkles] Failed to load config file {}", fileName, e);
            return null;
        }
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
        try (InputStream in = MoreSparkles.class.getResourceAsStream("/sparkles_configs/" + fileName)) {
            if (in == null) throw new IllegalStateException("Missing bundled default config " + fileName);
            return gson.toJson(JsonParser.parseReader(new InputStreamReader(in)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void fillMissingWithDefaults(String fileName, String defaultFileName, boolean isMapFile) {
        File file = new File(configDir, fileName);
        if (defaultFileName == null) defaultFileName = fileName;
        JsonObject defaultJson;
        JsonObject targetJson;
        try (InputStream in = MoreSparkles.class.getResourceAsStream("/sparkles_configs/" + defaultFileName);
             FileReader fileReader = new FileReader(file)) {
            if (in == null) throw new IOException("Missing bundled default config " + defaultFileName);
            defaultJson = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
            targetJson = JsonParser.parseReader(fileReader).getAsJsonObject();
        } catch (IOException | JsonParseException | IllegalStateException e) {
            LOGGER.error("[MoreSparkles] Failed to parse {} for filling defaults.", fileName, e);
            return;
        }
        mergeJsonObjects(targetJson, defaultJson, isMapFile);
        writeFile(file, gson.toJson(targetJson));
    }

    private static void mergeJsonObjects(JsonObject target, JsonObject defaults, boolean isMapFile) {
        if (!isMapFile) {
            for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
                String key = entry.getKey();
                JsonElement defaultValue = entry.getValue();

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

    public static void backupBrokenFile(File file) {
        File backup = new File(file.getParentFile(), file.getName() + ".broken");
        try {
            Files.move(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.error("[MoreSparkles] Could not read {}. It has been moved to {}", file.getName(), backup.getName());
        } catch (IOException e) {
            LOGGER.error("[MoreSparkles] Could not read {} or move it aside", file.getName(), e);
        }
    }

    public static Map<BoostType, LinkedList<Boost>> toSerializableQueues(@Nullable Map<BoostType, Queue<Boost>> queues) {
        Map<BoostType, LinkedList<Boost>> lists = new HashMap<>();
        if (queues != null) {
            queues.forEach((boostType, queue) -> {
                if (!queue.isEmpty()) lists.put(boostType, new LinkedList<>(queue));
            });
        }
        return lists;
    }
}
