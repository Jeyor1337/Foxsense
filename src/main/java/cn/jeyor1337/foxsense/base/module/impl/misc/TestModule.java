package cn.jeyor1337.foxsense.base.module.impl.misc;

import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.ColorValue;
import cn.jeyor1337.foxsense.base.value.ModeValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import net.minecraft.text.Text;

public class TestModule extends Module {
    public final ModeValue modeValue = new ModeValue("Mode", new String[] { "Option1", "Option2", "Option3" },
            "Option1");
    public final BooleanValue booleanValue = new BooleanValue("Boolean", true);
    public final BooleanValue anotherBooleanValue = new BooleanValue("AnotherBoolean", false,
            () -> modeValue.getValue().equals("Option2"));
    public final NumberValue numberValue = new NumberValue("Number", 1, 0, 10, 1,
            () -> modeValue.getValue().equals("Option3"));
    public final ColorValue colorValue = new ColorValue("Color", 255, false);

    public TestModule() {
        super("TestModule", ModuleType.MISC);
        // setKeybind(GLFW.GLFW_KEY_X);
        addValues(modeValue, booleanValue, anotherBooleanValue, numberValue, colorValue);
    }

    @Override
    protected void onEnable() {
        if (mc.player != null) {
            mc.player.sendMessage(Text.of("TestModule enabled!"), false);
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.sendMessage(Text.of("TestModule disabled!"), false);
        }
    }
}
