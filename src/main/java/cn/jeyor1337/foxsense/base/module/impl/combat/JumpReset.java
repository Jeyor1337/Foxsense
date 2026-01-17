package cn.jeyor1337.foxsense.base.module.impl.combat;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventPacket;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public class JumpReset extends Module {
    public static final NumberValue chance = new NumberValue("Chance (%)", 100, 1, 100, 10);
    public static final BooleanValue ignoreWhenBackwards = new BooleanValue("Ignore S press", true);
    public static final BooleanValue ignoreOnFire = new BooleanValue("Ignore on fire", true);

    public JumpReset() {
        super("JumpReset", ModuleType.COMBAT);
        this.addValues(chance, ignoreWhenBackwards, ignoreOnFire);
    }

    private boolean chanceCheck() {
        return (Math.random() * 100 >= chance.getValue().floatValue());
    }

    @EventTarget
    private void onPacket(EventPacket event) {
        if (isNull())
            return;

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet
                && packet.getEntityId() == mc.player.getId()) {
            if (chanceCheck() && mc.player.isOnGround()) {
                if (ignoreWhenBackwards.getValue() && mc.options.backKey.isPressed())
                    return;
                if (ignoreOnFire.getValue() && mc.player.isOnFire())
                    return;
                if (mc.currentScreen != null)
                    return;
                mc.player.jump();
            }
        }
    }
}
