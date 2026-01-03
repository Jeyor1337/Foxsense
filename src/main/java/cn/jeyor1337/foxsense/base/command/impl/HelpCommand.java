package cn.jeyor1337.foxsense.base.command.impl;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.command.Command;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "显示所有可用命令", "!help", "h");
    }

    @Override
    public void execute(String[] args) {
        sendMessage("§7========== §bFoxsense Commands §7==========");
        for (Command command : Foxsense.getCommandManager().getCommands()) {
            String aliases = command.getAliases().isEmpty() ? ""
                    : " §7[" + String.join(", ", command.getAliases()) + "]";
            sendMessage("§b" + command.getName() + aliases + " §7- §f" + command.getDescription());
            sendMessage("  §7用法: §f" + command.getSyntax());
        }
        sendMessage("§7=====================================");
    }
}
