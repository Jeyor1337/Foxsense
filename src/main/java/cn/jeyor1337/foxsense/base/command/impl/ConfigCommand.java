package cn.jeyor1337.foxsense.base.command.impl;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.command.Command;
import cn.jeyor1337.foxsense.base.config.Config;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config", "管理配置文件", "!config <save|load|list> [名称]", "c", "cfg");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            sendError("用法: " + getSyntax());
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "save":
                if (args.length < 2) {
                    Foxsense.getConfigManager().saveCurrentConfig();
                    sendMessage("已保存当前配置");
                } else {
                    String configName = args[1];
                    Config config = Foxsense.getConfigManager().getConfig(configName);
                    if (config == null) {
                        config = Foxsense.getConfigManager().createConfig(configName);
                    }
                    Foxsense.getConfigManager().setCurrentConfig(config);
                    Foxsense.getConfigManager().saveCurrentConfig();
                    sendMessage("已保存配置: §b" + configName);
                }
                break;

            case "load":
                if (args.length < 2) {
                    sendError("用法: !config load <名称>");
                    return;
                }
                String configName = args[1];
                Config config = Foxsense.getConfigManager().getConfig(configName);
                if (config == null) {
                    sendError("找不到配置: " + configName);
                    return;
                }
                Foxsense.getConfigManager().setCurrentConfig(config);
                Foxsense.getConfigManager().loadCurrentConfig();
                sendMessage("已加载配置: §b" + configName);
                break;

            case "list":
                sendMessage("§7========== §b可用配置 §7==========");
                for (Config cfg : Foxsense.getConfigManager().getConfigs()) {
                    String current = cfg == Foxsense.getConfigManager().getCurrentConfig() ? " §a[当前]" : "";
                    sendMessage("§b" + cfg.getName() + current);
                }
                sendMessage("§7==============================");
                break;

            default:
                sendError("未知操作: " + action);
                sendError("用法: " + getSyntax());
                break;
        }
    }
}
