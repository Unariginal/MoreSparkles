package me.unariginal.moresparkles.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.unariginal.moresparkles.MoreSparkles;
import me.unariginal.moresparkles.data.CylinderArea;
import me.unariginal.moresparkles.data.SparkleArea;
import me.unariginal.moresparkles.data.CuboidArea;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SparkleAreasConfig {
    public List<SparkleArea> areas = new ArrayList<>();

    public SparkleAreasConfig() {
        try {
            loadConfig();
        } catch (IOException e) {
            MoreSparkles.logError("Unable to load boost areas config file. Error: " + e.getMessage());
        }
    }

    public void loadConfig() throws IOException {
        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles").toFile();
        if (!rootFolder.exists())
            rootFolder.mkdir();

        File configFile = FabricLoader.getInstance().getConfigDir().resolve("MoreSparkles/boost_areas.json").toFile();
        JsonObject newRoot = new JsonObject();
        JsonObject root = new JsonObject();
        if (configFile.exists())
            root = JsonParser.parseReader(new FileReader(configFile)).getAsJsonObject();

        areas.clear();
        for (String key : root.keySet()) {
            JsonObject areaObject = root.getAsJsonObject(key);
            if (!(areaObject.has("type") && areaObject.has("shape") && areaObject.has("multiplier"))) continue;
            String type = areaObject.get("type").getAsString();
            float multiplier = areaObject.get("multiplier").getAsFloat();

            boolean particlesEnabled = false;
            String identifier = "minecraft:end_rod";
            float speed = 0;
            int count = 0;
            if (areaObject.has("particles")) {
                JsonObject particlesObject = areaObject.getAsJsonObject("particles");
                if (particlesObject.has("enabled"))
                    particlesEnabled = particlesObject.get("enabled").getAsBoolean();
                if (particlesEnabled) {
                    if (!(particlesObject.has("identifier") && particlesObject.has("speed") && particlesObject.has("count")))
                        particlesEnabled = false;
                    else {
                        identifier = particlesObject.get("identifier").getAsString();
                        speed = particlesObject.get("speed").getAsFloat();
                        count = particlesObject.get("count").getAsInt();
                    }
                }
            }

            JsonObject shapeObject = areaObject.getAsJsonObject("shape");
            if (type.equalsIgnoreCase("cuboid")) {
                if (!(shapeObject.has("world") &&
                        shapeObject.has("x_min") &&
                        shapeObject.has("x_max") &&
                        shapeObject.has("y_min") &&
                        shapeObject.has("y_max") &&
                        shapeObject.has("z_min") &&
                        shapeObject.has("z_max"))) continue;
                String world = shapeObject.get("world").getAsString();
                double minX = shapeObject.get("x_min").getAsDouble();
                double maxX = shapeObject.get("x_max").getAsDouble();
                double minY = shapeObject.get("y_min").getAsDouble();
                double maxY = shapeObject.get("y_max").getAsDouble();
                double minZ = shapeObject.get("z_min").getAsDouble();
                double maxZ = shapeObject.get("z_max").getAsDouble();
                if (particlesEnabled)
                    areas.add(new CuboidArea(key, type, multiplier, world, particlesEnabled, identifier, speed, count, minX, maxX, minY, maxY, minZ, maxZ));
                else
                    areas.add(new CuboidArea(key, type, multiplier, world, minX, maxX, minY, maxY, minZ, maxZ));
            } else if (type.equalsIgnoreCase("cylinder")) {
                if (!(shapeObject.has("world") &&
                        shapeObject.has("center_x") &&
                        shapeObject.has("center_z") &&
                        shapeObject.has("radius") &&
                        shapeObject.has("y_min") &&
                        shapeObject.has("y_max"))) continue;
                String world = shapeObject.get("world").getAsString();
                double centerX = shapeObject.get("center_x").getAsDouble();
                double centerZ = shapeObject.get("center_z").getAsDouble();
                double radius = shapeObject.get("radius").getAsDouble();
                double minY = shapeObject.get("y_min").getAsDouble();
                double maxY = shapeObject.get("y_max").getAsDouble();
                if (particlesEnabled)
                    areas.add(new CylinderArea(key, type, multiplier, world, particlesEnabled, identifier, speed, count, centerX, centerZ, radius, minY, maxY));
                else
                    areas.add(new CylinderArea(key, type, multiplier, world, centerX, centerZ, radius, minY, maxY));
            }
        }

        for (SparkleArea area : areas) {
            JsonObject areaObject = new JsonObject();

            areaObject.addProperty("type", area.type);
            areaObject.addProperty("multiplier", area.multiplier);
            JsonObject shapeObject = new JsonObject();
            shapeObject.addProperty("world", area.world);

            if (area instanceof CuboidArea cuboidArea) {
                shapeObject.addProperty("x_min", cuboidArea.xMin);
                shapeObject.addProperty("x_max", cuboidArea.xMax);
                shapeObject.addProperty("y_min", cuboidArea.yMin);
                shapeObject.addProperty("y_max", cuboidArea.yMax);
                shapeObject.addProperty("z_min", cuboidArea.zMin);
                shapeObject.addProperty("z_max", cuboidArea.zMax);
                areaObject.add("shape", shapeObject);
            } else if (area instanceof CylinderArea cylinderArea) {
                shapeObject.addProperty("center_x", cylinderArea.centerX);
                shapeObject.addProperty("center_z", cylinderArea.centerZ);
                shapeObject.addProperty("radius", cylinderArea.radius);
                shapeObject.addProperty("y_min", cylinderArea.yMin);
                shapeObject.addProperty("y_max", cylinderArea.yMax);
                areaObject.add("shape", shapeObject);
            }

            if (area.particlesEnabled) {
                JsonObject particlesObject = new JsonObject();
                particlesObject.addProperty("enabled", true);
                particlesObject.addProperty("identifier", area.particleIdentifier);
                particlesObject.addProperty("speed", area.particleSpeed);
                particlesObject.addProperty("count", area.particleCount);
                areaObject.add("particles", particlesObject);
            }

            newRoot.add(area.id, areaObject);
        }

        configFile.delete();
        configFile.createNewFile();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Writer writer = new FileWriter(configFile);
        gson.toJson(newRoot, writer);
        writer.close();
    }

    public SparkleArea getBoostArea(String id) {
        for (SparkleArea area : areas) {
            if (area.id.equals(id)) return area;
        }
        return null;
    }
}
