package cn.jeyor1337.foxsense.base.module.impl.combat;

import org.lwjgl.glfw.GLFW;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventAttack;
import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.keybinding.KeyUtils;
import cn.jeyor1337.foxsense.base.util.timer.TimerUtil;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;

public class WTap extends Module {
    public static final NumberValue chance = new NumberValue("Chance (%)", 100, 1, 100, 1);
    private final NumberValue msDelay = new NumberValue("Ms", 60, 1, 500, 1);
    private final BooleanValue onlyOnGround = new BooleanValue("Only on ground", true);
    boolean wasSprinting;
    TimerUtil timer = new TimerUtil();

    public WTap() {
        super("WTap", "Makes you automatically WTAP", ModuleType.COMBAT);
        this.addValues(msDelay, chance, onlyOnGround);
    }

    @EventTarget
    private void onAttackEvent(EventAttack event) {
        if (isNull())
            return;
        if (Math.random() * 100 > chance.getValue().floatValue())
            return;
        var target = event.getTargetEntity();
        if (!mc.player.isOnGround() && onlyOnGround.getValue())
            return;
        if (target == null)
            return;
        if (!target.isAlive())
            return;
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W))
            return;
        if (mc.player.isSprinting()) {
            wasSprinting = true;
            mc.options.forwardKey.setPressed(false);
        }
    }

    @EventTarget
    private void onTickEvent(EventTick event) {
        if (isNull())
            return;
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W))
            return;

        if (wasSprinting) {
            if (timer.hasElapsedTime(msDelay.getValue().intValue(), true)) {
                mc.options.forwardKey.setPressed(true);
                wasSprinting = false;
            }
        }
    }
}
