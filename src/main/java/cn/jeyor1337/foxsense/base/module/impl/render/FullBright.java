package cn.jeyor1337.foxsense.base.module.impl.render;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.value.ModeValue;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullBright extends Module {
    private Double previousGamma = null;

    private final ModeValue mode = new ModeValue("Mode", new String[] { "Gamma", "NightVision" }, "Gamma");

    public FullBright() {
        super("FullBright", ModuleType.RENDER);
        addValue(mode);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        if (mode.getValue().equals("Gamma")) {
            applyGamma();
        }
    }

    @Override
    protected void onDisable() {
        restoreGamma();
        removeNightVision();
        super.onDisable();
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (isNull())
            return;

        if (mode.getValue().equals("NightVision")) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION,
                    420,
                    0,
                    false,
                    false,
                    false));
        }
    }

    private void applyGamma() {
        if (mc != null && mc.options != null) {
            try {
                SimpleOption<Double> gamma = mc.options.getGamma();
                previousGamma = gamma.getValue();
                gamma.setValue(1.0);
            } catch (Throwable ignored) {
            }
        }
    }

    private void restoreGamma() {
        if (mc != null && mc.options != null && previousGamma != null) {
            try {
                mc.options.getGamma().setValue(previousGamma);
                previousGamma = null;
            } catch (Throwable ignored) {
            }
        }
    }

    private void removeNightVision() {
        if (mc != null && mc.player != null) {
            try {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            } catch (Throwable ignored) {
            }
        }
    }
}
