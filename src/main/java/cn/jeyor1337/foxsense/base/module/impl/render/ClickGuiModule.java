package cn.jeyor1337.foxsense.base.module.impl.render;

import org.lwjgl.glfw.GLFW;

import cn.jeyor1337.foxsense.base.gui.ClickGui;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import net.minecraft.client.MinecraftClient;

public class ClickGuiModule extends Module {
    private static ClickGuiModule instance;
    private final ClickGui clickGui;

    public ClickGuiModule() {
        super("ClickGUI", "打开ClickGUI界面", ModuleType.RENDER);
        this.setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        this.clickGui = new ClickGui();
        instance = this;
    }

    @Override
    public void onEnable() {
        MinecraftClient.getInstance().setScreen(clickGui);
        this.toggle();
    }

    public static ClickGuiModule getInstance() {
        return instance;
    }

    public int getKeybind() {
        return this.keybind;
    }
}
