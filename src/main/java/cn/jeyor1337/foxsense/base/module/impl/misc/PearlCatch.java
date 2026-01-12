package cn.jeyor1337.foxsense.base.module.impl.misc;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.timer.TimerUtil;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.mixin.MinecraftClientAccessor;
import cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class PearlCatch extends Module {
    private final NumberValue windDelay = new NumberValue("Wind Delay", 200, 0, 2000, 1);
    private final NumberValue switchDelay = new NumberValue("Switch Delay", 50, 0, 500, 10);

    private final TimerUtil pearlDelayTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();
    private boolean pearlThrown = false;
    private int originalSlot = -1;
    private boolean needsSwitchBack = false;

    public PearlCatch() {
        super("PearlCatch", "Throws pearl then windcharge", ModuleType.MISC);
        this.addValues(windDelay, switchDelay);
    }

    @EventTarget
    private void onTick(EventTick event) {
        if (isNull() || mc.currentScreen != null)
            return;

        if (pearlThrown && pearlDelayTimer.hasElapsedTime(windDelay.getValue().longValue())) {
            throwWindCharge();
            pearlThrown = false;
        }

        if (needsSwitchBack && switchTimer.hasElapsedTime(switchDelay.getValue().longValue())) {
            ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(originalSlot);
            needsSwitchBack = false;
            originalSlot = -1;
            setEnabled(false);
        }
    }

    private void throwPearl() {
        int pearlSlot = findPearlSlot();
        if (pearlSlot == -1) {
            setEnabled(false);
            return;
        }

        if (mc.player.getItemCooldownManager().isCoolingDown(mc.player.getInventory().getStack(pearlSlot))) {
            setEnabled(false);
            return;
        }

        originalSlot = ((PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot();
        ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(pearlSlot);
        mc.player.swingHand(Hand.MAIN_HAND);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        needsSwitchBack = true;
        switchTimer.reset();

        pearlThrown = true;
        pearlDelayTimer.reset();
    }

    private void throwWindCharge() {
        int windChargeSlot = findWindChargeSlot();
        if (windChargeSlot == -1)
            return;

        if (mc.player.getItemCooldownManager().isCoolingDown(mc.player.getInventory().getStack(windChargeSlot))) {
            return;
        }

        originalSlot = ((PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot();
        ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(windChargeSlot);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        needsSwitchBack = true;
        switchTimer.reset();
    }

    private int findPearlSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
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
        pearlThrown = false;
        originalSlot = -1;
        needsSwitchBack = false;
        pearlDelayTimer.reset();
        switchTimer.reset();
        throwPearl();
    }

    @Override
    protected void onDisable() {
        pearlThrown = false;
    }
}
