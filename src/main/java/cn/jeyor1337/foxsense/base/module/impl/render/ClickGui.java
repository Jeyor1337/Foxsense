package cn.jeyor1337.foxsense.base.module.impl.render;

import org.lwjgl.glfw.GLFW;

import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import net.minecraft.client.MinecraftClient;

public class ClickGui extends Module {
    private static ClickGui instance;
    private final cn.jeyor1337.foxsense.base.gui.ClickGui clickGui;

    public ClickGui() {
        super("ClickGUI", "打开ClickGUI界面", ModuleType.RENDER);
        this.setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        this.clickGui = new cn.jeyor1337.foxsense.base.gui.ClickGui();
        instance = this;
    }

    @Override
    public void onEnable() {
        MinecraftClient.getInstance().setScreen(this.clickGui);
        this.toggle();
    }

    public static ClickGui getInstance() {
        return instance;
    }

    public int getKeybind() {
        return this.keybind;
    }
}
