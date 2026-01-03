package cn.jeyor1337.foxsense.base.module.impl.movement;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", ModuleType.MOVEMENT);
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (isNull())
            return;
        if (mc.options.getSprintToggled().getValue())
            mc.options.getSprintToggled().setValue(false);

        mc.options.sprintKey.setPressed(true);
    }
}
