package cn.jeyor1337.foxsense.base.command.impl;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.command.Command;
import cn.jeyor1337.foxsense.base.module.Module;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "开关指定模块", "!toggle <模块名>", "t");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            sendError("用法: " + getSyntax());
            return;
        }

        String moduleName = args[0];
        Module module = Foxsense.getModuleManager().getModule(moduleName);

        if (module == null) {
            sendError("找不到模块: " + moduleName);
            return;
        }

        module.toggle();
        sendMessage("模块 §b" + module.getName() + " §f已" + (module.isEnabled() ? "§a启用" : "§c禁用"));
    }
}
