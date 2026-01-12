package cn.jeyor1337.foxsense.base.module.impl.combat;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.timer.TimerUtil;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.mixin.MinecraftClientAccessor;
import cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AutoMace extends Module {
    private final NumberValue minFallDistance = new NumberValue("Min Fall Distance", 3.0, 1.0, 10.0, 0.5);
    private final NumberValue attackDelay = new NumberValue("Attack Delay", 100.0, 0.0, 500.0, 10.0);
    private final NumberValue densityThreshold = new NumberValue("Density Threshold", 7.0, 1.0, 20.0, 0.5);
    private final BooleanValue targetPlayers = new BooleanValue("Target Players", true);
    private final BooleanValue targetMobs = new BooleanValue("Target Mobs", false);
    private final BooleanValue stunSlam = new BooleanValue("Stun Slam", false);
    private final BooleanValue autoSwitch = new BooleanValue("Auto Switch Mace", true);

    private final TimerUtil attackTimer = new TimerUtil();
    private int savedSlot = -1;
    private double fallStartY = -1;
    private boolean isFalling = false;
    private boolean slamExecuted = false;
    private boolean maceHit = false;
    private int slamTick = 0;

    public AutoMace() {
        super("AutoMace", "Automatically attacks with mace", ModuleType.COMBAT);
        addValues(minFallDistance, attackDelay, densityThreshold, targetPlayers, targetMobs, stunSlam, autoSwitch);
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (isNull())
            return;

        updateFall();
        attack();
    }

    private void updateFall() {
        boolean onGround = mc.player.isOnGround();
        boolean falling = mc.player.getVelocity().y < -0.1;
        double currentY = mc.player.getY();

        if (onGround) {
            if (isFalling) {
                resetFall();
            }
            if (savedSlot != -1) {
                switchToSlot(savedSlot);
                savedSlot = -1;
            }
            return;
        }

        if (!isFalling) {
            isFalling = true;
            fallStartY = currentY;
            slamExecuted = false;
            maceHit = false;
            slamTick = 0;
        } else if (falling && fallStartY != -1 && currentY > fallStartY) {
            fallStartY = currentY;
        }
    }

    private void attack() {
        if (!isFalling || mc.player.getVelocity().y >= -0.1)
            return;

        double fallDist = fallStartY == -1 ? 0 : Math.max(0, fallStartY - mc.player.getY());
        if (fallDist < minFallDistance.getValue().doubleValue())
            return;

        Entity target = mc.targetedEntity;
        if (!isValidTarget(target))
            return;

        if (stunSlam.isEnabled()) {
            handleSlam(target, fallDist);
        }

        if (!stunSlam.isEnabled() || slamExecuted || slamTick == 0) {
            handleMaceAttack(target);
        }
    }

    private void handleSlam(Entity target, double fallDist) {
        boolean targetBlocking = target instanceof PlayerEntity player
                && player.isHolding(Items.SHIELD)
                && player.isBlocking();

        if (targetBlocking && fallDist > minFallDistance.getValue().doubleValue() && !slamExecuted && slamTick == 0) {
            if (savedSlot == -1)
                savedSlot = ((PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot();
            slamTick = 1;
        }

        if (slamTick == 1) {
            int axeSlot = getAxeSlotId();
            if (axeSlot != -1) {
                ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(axeSlot);
                ((MinecraftClientAccessor) mc).invokeDoAttack();
            }
            slamTick = 2;
        } else if (slamTick == 2) {
            switchToMace();
            slamExecuted = true;
            slamTick = 0;
        }
    }

    private void handleMaceAttack(Entity target) {
        double fallDist = fallStartY == -1 ? 0 : Math.max(0, fallStartY - mc.player.getY());

        if (!hasMace()) {
            if (savedSlot == -1)
                savedSlot = ((PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot();
            if (autoSwitch.isEnabled()) {
                switchToAppropriateMace(fallDist);
            } else {
                switchToMace();
            }
        } else if (autoSwitch.isEnabled()) {
            switchToAppropriateMace(fallDist);
        }

        if (hasMace() && attackTimer.hasElapsedTime(attackDelay.getValue().longValue(), true)) {
            ((MinecraftClientAccessor) mc).invokeDoAttack();
            maceHit = true;
        }
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == null || entity == mc.player || entity == mc.getCameraEntity())
            return false;
        if (!(entity instanceof LivingEntity livingEntity))
            return false;
        if (!livingEntity.isAlive() || livingEntity.isDead())
            return false;

        if (entity instanceof PlayerEntity) {
            return targetPlayers.isEnabled();
        }

        if (!targetMobs.isEnabled())
            return false;
        return !(entity instanceof PassiveEntity) && !(entity instanceof Tameable);
    }

    private int getAxeSlotId() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isAxe(stack))
                return i;
        }
        return -1;
    }

    private boolean isAxe(ItemStack stack) {
        return stack.getItem() instanceof AxeItem;
    }

    private boolean hasMace() {
        return mc.player.getMainHandStack().getItem() == Items.MACE;
    }

    private void switchToMace() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.MACE) {
                ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(i);
                return;
            }
        }
    }

    private void switchToAppropriateMace(double fallDistance) {
        boolean useDensity = fallDistance >= densityThreshold.getValue().doubleValue();

        int targetSlot = -1;

        if (targetSlot == -1) {
            targetSlot = findAnyMaceSlot();
        }

        if (targetSlot != -1) {
            ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(targetSlot);
        }
    }

    private int findAnyMaceSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.MACE) {
                return i;
            }
        }
        return -1;
    }

    private void switchToSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(slot);
        }
    }

    private void resetFall() {
        isFalling = false;
        fallStartY = -1;
        slamExecuted = false;
        maceHit = false;
        slamTick = 0;
    }

    @Override
    protected void onEnable() {
        savedSlot = -1;
        fallStartY = -1;
        isFalling = false;
        slamExecuted = false;
        maceHit = false;
        slamTick = 0;
        attackTimer.reset();
    }

    @Override
    protected void onDisable() {
        if (savedSlot != -1) {
            switchToSlot(savedSlot);
        }
        resetAll();
    }

    private void resetAll() {
        savedSlot = -1;
        fallStartY = -1;
        isFalling = false;
        slamExecuted = false;
        maceHit = false;
        slamTick = 0;
        attackTimer.reset();
    }
}
