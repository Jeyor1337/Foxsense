package cn.jeyor1337.foxsense.base.module.impl.combat;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventAttack;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import net.minecraft.util.hit.HitResult;

public class AntiMiss extends Module {

    public AntiMiss() {
        super("AntiMiss", ModuleType.COMBAT);
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        if (isNull()) {
            return;
        }

        assert mc.crosshairTarget != null;
        if (mc.crosshairTarget.getType().equals(HitResult.Type.MISS)) {
            event.setCancelled(true);
        }
    }
}
