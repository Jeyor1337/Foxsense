package cn.jeyor1337.foxsense.base.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jeyor1337.foxsense.base.config.Config;

public class ConfigManager {
    private static ConfigManager instance;
    private final File configDir;
    private final ModuleManager moduleManager;
    private final List<Config> configs;
    private Config currentConfig;

    public ConfigManager(File configDir, ModuleManager moduleManager) {
        this.configDir = configDir;
        this.moduleManager = moduleManager;
        this.configs = new ArrayList<>();
        instance = this;

        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        loadExistingConfigs();
    }

    private void loadExistingConfigs() {
        File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String configName = file.getName().replace(".json", "");
                Config config = new Config(configDir, configName, moduleManager);
                configs.add(config);
            }
        }
    }

    public void setCurrentConfig(Config config) {
        this.currentConfig = config;
        if (!configs.contains(config)) {
            configs.add(config);
        }
    }

    public Config createConfig(String name) {
        return createConfig(name, true);
    }

    public Config createConfig(String name, boolean autoLoad) {
        Config config = new Config(configDir, name, moduleManager);
        configs.add(config);
        if (autoLoad) {
            config.load();
        }
        return config;
    }

    public void saveConfig(Config config) {
        config.save();
    }

    public void loadConfig(Config config) {
        config.load();
    }

    public void saveCurrentConfig() {
        if (currentConfig != null) {
            currentConfig.save();
        }
    }

    public void loadCurrentConfig() {
        if (currentConfig != null) {
            currentConfig.load();
        }
    }

    public void deleteConfig(Config config) {
        File configFile = config.getConfigFile();
        if (configFile.exists()) {
            configFile.delete();
        }
        configs.remove(config);
    }

    public Config getConfig(String name) {
        for (Config config : configs) {
            if (config.getName().equals(name)) {
                return config;
            }
        }
        return null;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public Config getCurrentConfig() {
        return currentConfig;
    }

    public File getConfigDir() {
        return configDir;
    }

    public static ConfigManager getInstance() {
        return instance;
    }
}
