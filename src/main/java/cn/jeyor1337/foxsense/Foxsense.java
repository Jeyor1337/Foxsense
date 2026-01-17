package cn.jeyor1337.foxsense;

import java.io.File;

import com.cubk.event.EventManager;
import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.config.Config;
import cn.jeyor1337.foxsense.base.event.EventChat;
import cn.jeyor1337.foxsense.base.event.EventKey;
import cn.jeyor1337.foxsense.base.manager.CommandManager;
import cn.jeyor1337.foxsense.base.manager.ConfigManager;
import cn.jeyor1337.foxsense.base.manager.ModuleManager;
import net.minecraft.client.MinecraftClient;

public class Foxsense {
    public static final String NAME = "Foxsense";
    private static Foxsense instance;
    private static ModuleManager moduleManager;
    private static CommandManager commandManager;
    private static ConfigManager configManager;
    private static EventManager eventManager;
    private static MinecraftClient mc;

    public void onInitializeClient() {
        instance = this;
        mc = MinecraftClient.getInstance();

        moduleManager = new ModuleManager();
        eventManager = new EventManager();
        commandManager = new CommandManager();
        configManager = new ConfigManager(new File(mc.runDirectory, "foxsense"), moduleManager);

        Config defaultConfig = configManager.createConfig("default");
        configManager.setCurrentConfig(defaultConfig);

        eventManager.register(this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            configManager.saveCurrentConfig();
        }));
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (mc.currentScreen != null) {
            return;
        }

        if (event.getAction() == 1) {
            moduleManager.handleKeybind(event.getKey());
        }
    }

    @EventTarget
    public void onChat(EventChat event) {
        if (commandManager.handleCommand(event.getMessage())) {
            event.setCancelled(true);
        }
    }

    public static Foxsense getInstance() {
        return instance;
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static EventManager getEventManager() {
        return eventManager;
    }

    public static MinecraftClient getMc() {
        return mc;
    }
}
