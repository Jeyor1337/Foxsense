package cn.jeyor1337.foxsense.base.module.impl.movement;

import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;

public class Flight extends Module {

    public Flight() {
        super("Flight", ModuleType.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        if (isNull()) {
            return;
        }

        if (!mc.player.isSpectator()) {
            mc.player.getAbilities().flying = true;

            if (mc.player.getAbilities().creativeMode)
                return;

            mc.player.getAbilities().allowFlying = true;
        }
    }

    @Override
    protected void onDisable() {
        if (isNull()) {
            return;
        }

        if (!mc.player.isSpectator()) {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().setFlySpeed(0.05f);
            if (mc.player.getAbilities().creativeMode)
                return;
            mc.player.getAbilities().allowFlying = false;
        }
        super.onDisable();
    }
}
