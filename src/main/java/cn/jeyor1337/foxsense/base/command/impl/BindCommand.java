package cn.jeyor1337.foxsense.base.command.impl;

import org.lwjgl.glfw.GLFW;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.command.Command;
import cn.jeyor1337.foxsense.base.module.Module;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "为模块绑定按键", "!bind <模块名> <按键>", "b");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendError("用法: " + getSyntax());
            return;
        }

        String moduleName = args[0];
        Module module = Foxsense.getModuleManager().getModule(moduleName);

        if (module == null) {
            sendError("找不到模块: " + moduleName);
            return;
        }

        String keyName = args[1].toUpperCase();
        int keyCode = getKeyCode(keyName);

        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            sendError("无效的按键: " + keyName);
            return;
        }

        module.setKeybind(keyCode);
        sendMessage("模块 §b" + module.getName() + " §f已绑定到按键 §b" + keyName);
    }

    private int getKeyCode(String keyName) {
        try {
            java.lang.reflect.Field field = GLFW.class.getField("GLFW_KEY_" + keyName);
            return field.getInt(null);
        } catch (Exception e) {
            return GLFW.GLFW_KEY_UNKNOWN;
        }
    }
}
