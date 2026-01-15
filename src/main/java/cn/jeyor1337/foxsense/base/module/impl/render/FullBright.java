package cn.jeyor1337.foxsense.base.module.impl.render;

import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import net.minecraft.client.option.SimpleOption;

public class FullBright extends Module {
    private Double previousGamma = null;

    public FullBright() {
        super("FullBright", ModuleType.RENDER);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        if (mc != null && mc.options != null) {
            try {
                SimpleOption<Double> gamma = mc.options.getGamma();
                previousGamma = gamma.getValue();
                gamma.setValue(1.0);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    protected void onDisable() {
        if (mc != null && mc.options != null && previousGamma != null) {
            try {
                mc.options.getGamma().setValue(previousGamma);
            } catch (Throwable ignored) {
            }
        }
        super.onDisable();
    }
}
