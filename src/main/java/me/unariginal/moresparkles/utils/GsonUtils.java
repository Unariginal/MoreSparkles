package me.unariginal.moresparkles.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.unariginal.moresparkles.data.boostareas.BoostArea;
import me.unariginal.moresparkles.data.boostareas.CuboidArea;
import me.unariginal.moresparkles.data.boostareas.CylinderArea;

public class GsonUtils {
    public static Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapterFactory(
                    RuntimeTypeAdapterFactory
                            .of(BoostArea.class, "type")
                            .registerSubtype(CuboidArea.class, "cuboid")
                            .registerSubtype(CylinderArea.class, "cylinder")
            )
            .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
            .create();
}
