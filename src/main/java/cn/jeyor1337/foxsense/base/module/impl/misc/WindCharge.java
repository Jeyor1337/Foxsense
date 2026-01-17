package cn.jeyor1337.foxsense.base.module.impl.misc;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.timer.TimerUtil;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.mixin.MinecraftClientAccessor;
import cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor;
import net.minecraft.item.Items;

public class WindCharge extends Module {
    private final NumberValue switchDelay = new NumberValue("Switch Delay", 50, 0, 500, 10);
    private final BooleanValue autoJump = new BooleanValue("Auto Jump", true);

    private final TimerUtil switchTimer = new TimerUtil();
    private int originalSlot = -1;
    private boolean needsSwitchBack = false;

    public WindCharge() {
        super("WindCharge", "Throws wind charge once", ModuleType.MISC);
        this.addValues(switchDelay, autoJump);
    }

    @EventTarget
    private void onTick(EventTick event) {
        if (isNull() || mc.currentScreen != null)
            return;

        if (needsSwitchBack && switchTimer.hasElapsedTime(switchDelay.getValue().longValue())) {
            ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(originalSlot);
            needsSwitchBack = false;
            originalSlot = -1;
            setEnabled(false);
        }
    }

    private void throwWindCharge() {
        int windChargeSlot = findWindChargeSlot();
        if (windChargeSlot == -1) {
            setEnabled(false);
            return;
        }

        if (mc.player.getItemCooldownManager().isCoolingDown(mc.player.getInventory().getStack(windChargeSlot))) {
            setEnabled(false);
            return;
        }

        if (autoJump.getValue() && mc.player.isOnGround()) {
            mc.player.jump();
        }

        originalSlot = ((PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot();
        ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(windChargeSlot);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        needsSwitchBack = true;
        switchTimer.reset();
    }

    private int findWindChargeSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.WIND_CHARGE) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onEnable() {
        originalSlot = -1;
        needsSwitchBack = false;
        switchTimer.reset();
        throwWindCharge();
    }

    @Override
    protected void onDisable() {
        needsSwitchBack = false;
    }
}
