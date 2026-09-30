package me.unariginal.moresparkles.configs;

import me.unariginal.moresparkles.managers.BoostManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

import static me.unariginal.moresparkles.utils.GsonUtils.gson;

public class GlobalBoostDataManager {
    private static final String FILE_NAME = "global_boosts.json";

    @Nullable
    public static GlobalBoostData loadGlobalBoostData() {
        File file = new File(ConfigManager.configDir, FILE_NAME);
        if (!file.exists()) return null;

        GlobalBoostData data = ConfigManager.loadFile(FILE_NAME, GlobalBoostData.class);
        if (data == null) ConfigManager.backupBrokenFile(file);
        return data;
    }

    public static void saveGlobalBoostData() {
        GlobalBoostData data = new GlobalBoostData(
                new HashMap<>(BoostManager.globalBoosts),
                ConfigManager.toSerializableQueues(BoostManager.queuedGlobalBoosts)
        );
        ConfigManager.writeFile(new File(ConfigManager.configDir, FILE_NAME), gson.toJson(data));
    }
}
