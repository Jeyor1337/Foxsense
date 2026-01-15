package cn.jeyor1337.foxsense.base.module.impl.player;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.item.ItemUtils;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.mixin.MinecraftClientAccessor;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class AutoMLG extends Module {
    private final NumberValue fallDistance = new NumberValue("Fall Distance", 8, 3, 40, 1);
    private final BooleanValue pickUp = new BooleanValue("Pick Up", true);
    private int stage;
    private int ticks;
    private int storedSlot = -1;
    private float storedPitch;
    private boolean changedPitch;

    public AutoMLG() {
        super("Auto MLG", "Places water before landing", ModuleType.PLAYER);
        this.addValues(fallDistance, pickUp);
    }

    @EventTarget
    private void onTick(EventTick event) {
        if (isNull())
            return;
        if (stage == 0) {
            tryStart();
        } else if (stage == 1) {
            handlePlacement();
        } else if (stage == 2) {
            handlePickup();
        } else if (stage == 3) {
            finishSequence();
        }
    }

    private void tryStart() {
        if (mc.player.isOnGround())
            return;
        if (mc.player.isTouchingWater())
            return;
        if (mc.player.fallDistance < fallDistance.getValue().doubleValue())
            return;
        if (mc.player.getVelocity().y >= -0.6)
            return;
        if (!ItemUtils.hasItem(Items.WATER_BUCKET))
            return;
        storedSlot = ((cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot();
        storedPitch = mc.player.getPitch();
        ItemUtils.swapToSlot(Items.WATER_BUCKET);
        stage = 1;
        ticks = 0;
        changedPitch = false;
    }

    private void handlePlacement() {
        if (!changedPitch) {
            mc.player.setPitch(89.5f);
            changedPitch = true;
            return;
        }
        if (mc.player.isOnGround()) {
            stage = 3;
            return;
        }
        int distance = findGroundDistance();
        ticks++;
        if (distance > 2)
            return;
        if (ticks % 2 != 0)
            return;
        ItemUtils.swapToSlot(Items.WATER_BUCKET);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        stage = pickUp.getValue() ? 2 : 3;
        ticks = 0;
    }

    private void handlePickup() {
        ticks++;
        if (!mc.player.isOnGround() && !mc.player.isTouchingWater())
            return;
        if (ticks < 3)
            return;
        ItemUtils.swapToSlot(Items.BUCKET);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        stage = 3;
    }

    private void finishSequence() {
        if (storedSlot >= 0) {
            ((cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor) mc.player.getInventory())
                    .setSelectedSlot(storedSlot);
        }
        if (changedPitch) {
            mc.player.setPitch(storedPitch);
        }
        stage = 0;
        ticks = 0;
        storedSlot = -1;
        changedPitch = false;
    }

    @Override
    protected void onDisable() {
        finishSequence();
        super.onDisable();
    }

    private int findGroundDistance() {
        if (isNull())
            return 6;
        BlockPos base = mc.player.getBlockPos();
        for (int i = 1; i <= 6; i++) {
            BlockPos check = base.down(i);
            if (!mc.world.getBlockState(check).isAir()) {
                return i;
            }
        }
        return 7;
    }
}
