package org.gamma02.waterfixmod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    public static Gson json = new GsonBuilder().setPrettyPrinting().create();
    public static final ConfigFile DEFAULT_CONFIG = new ConfigFile(
            false,
            true,
            true,
            true
            );

    public static ConfigFile CONFIG;

    public static void reloadConfig(){
        Path configDir = FabricLoader.getInstance().getConfigDir();

        Path waterFixConfigFile = configDir.resolve("waterfix.json");

        File configFile = waterFixConfigFile.toFile();

        if(!configFile.exists()){
            try {
                Files.writeString(waterFixConfigFile, json.toJson(DEFAULT_CONFIG));
            } catch (IOException e) {
                System.out.println("Writing config file failed!");
            }
        }

        try {
            String config = new String(Files.readAllBytes(waterFixConfigFile));
            CONFIG = json.fromJson(config, ConfigFile.class);
            Files.write(waterFixConfigFile, json.toJson(CONFIG).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public record ConfigFile(
            boolean useSpecialWaterTesselationForNormalWater,
            boolean renderBackfacesForFluids,
            boolean renderBlockEntitesInFallingBlocks,
            boolean replaceFallingBlockRendering
    ){}



}
