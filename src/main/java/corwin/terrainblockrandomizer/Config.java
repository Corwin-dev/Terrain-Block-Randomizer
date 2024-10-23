package com.example.terrainblockrandomizer.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
    // Static instance for global access
    private static Config INSTANCE;

    // Maps to hold config variables
    private final Map<String, Object> globalConfig = new HashMap<>();
    private final Map<String, Object> worldConfig = new HashMap<>();

    private final File globalConfigFile;
    private final File defaultWorldConfigFile;

    // Private constructor
    private Config() {
        Path configDirPath = FabricLoader.getInstance().getConfigDir().resolve("terrainblockrandomizer");
        File configDir = configDirPath.toFile();
        configDir.mkdirs(); // Ensure the directory exists

        this.globalConfigFile = new File(configDir, "terrainblockrandomizer-global.toml");
        this.defaultWorldConfigFile = new File(configDir, "terrainblockrandomizer-world-defaults.toml");
        loadConfig();
    }

    // Singleton access method
    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Config();
        }
        return INSTANCE;
    }

    // Method to add global config values
    public void addGlobalSetting(String key, Object defaultValue) {
        globalConfig.putIfAbsent(key, defaultValue);
    }

    // Method to add world-specific config values
    public void addWorldSetting(String key, Object defaultValue) {
        worldConfig.putIfAbsent(key, defaultValue);
    }

    // Load or create config files
    private void loadConfig() {
        // Load global config
        if (globalConfigFile.exists()) {
            Toml toml = new Toml().read(globalConfigFile);
            toml.toMap().forEach(globalConfig::put);
        } else {
            saveConfig(globalConfigFile, globalConfig);
        }

        // Load default world config
        if (defaultWorldConfigFile.exists()) {
            Toml toml = new Toml().read(defaultWorldConfigFile);
            toml.toMap().forEach(worldConfig::put);
        } else {
            saveConfig(defaultWorldConfigFile, worldConfig);
        }
    }

    // Save config files
    private void saveConfig(File file, Map<String, Object> configMap) {
        TomlWriter writer = new TomlWriter();
        try {
            writer.write(configMap, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Retrieve a global setting
    public Object getGlobalSetting(String key) {
        return globalConfig.get(key);
    }

    // Retrieve a world setting
    public Object getWorldSetting(String key) {
        return worldConfig.get(key);
    }

    // Method to load or create a config for a specific world
    public void loadOrCreateWorldConfig(File worldDir) {
        File worldConfigFile = new File(worldDir, "terrainblockrandomizer-world.toml");
        Map<String, Object> worldSpecificConfig = new HashMap<>(worldConfig);

        if (worldConfigFile.exists()) {
            Toml toml = new Toml().read(worldConfigFile);
            toml.toMap().forEach(worldSpecificConfig::put);
        } else {
            saveConfig(worldConfigFile, worldSpecificConfig);
        }

        // Update worldConfig with world-specific values
        worldConfig.putAll(worldSpecificConfig);
    }
}
