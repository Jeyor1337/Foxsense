package cn.jeyor1337.foxsense.base.module.impl.combat;

import org.lwjgl.glfw.GLFW;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.item.ItemUtils;
import cn.jeyor1337.foxsense.base.util.math.MathUtils;
import cn.jeyor1337.foxsense.base.util.timer.TimerUtil;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.ModeValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.mixin.MinecraftClientAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;

public class TriggerBot extends Module {
    private final NumberValue swordThresholdMin = new NumberValue("Sword Min", 0.90, 0.1, 1.0, 0.01);
    private final NumberValue swordThresholdMax = new NumberValue("Sword Max", 0.95, 0.1, 1.0, 0.01);
    private final NumberValue axeThresholdMin = new NumberValue("Axe Min", 0.90, 0.1, 1.0, 0.01);
    private final NumberValue axeThresholdMax = new NumberValue("Axe Max", 0.95, 0.1, 1.0, 0.01);
    private final NumberValue axePostDelayMin = new NumberValue("Axe Delay Min", 120.0, 1.0, 500.0, 0.5);
    private final NumberValue axePostDelayMax = new NumberValue("Axe Delay Max", 120.0, 1.0, 500.0, 0.5);
    private final NumberValue reactionTimeMin = new NumberValue("Reaction Min", 20.0, 1.0, 350.0, 0.5);
    private final NumberValue reactionTimeMax = new NumberValue("Reaction Max", 95.0, 1.0, 350.0, 0.5);
    private final ModeValue cooldownMode = new ModeValue("Cooldown Mode", new String[] { "Smart", "Strict", "None" },
            "Smart");
    private final ModeValue critMode = new ModeValue("Criticals", new String[] { "None", "Strict" }, "Strict");
    private final BooleanValue ignorePassiveMobs = new BooleanValue("No Passive", true);
    private final BooleanValue ignoreInvisible = new BooleanValue("No Invisible", true);
    private final BooleanValue ignoreCrystals = new BooleanValue("No Crystals", true);
    private final BooleanValue autoUnBlock = new BooleanValue("Auto UnBlock", false);
    private final BooleanValue autoUnSprint = new BooleanValue("Auto UnSprint", false);
    private final BooleanValue respectShields = new BooleanValue("Ignore Shields", false);
    private final BooleanValue ignoreUsing = new BooleanValue("Ignore Using Item", false);
    private final BooleanValue useOnlySwordOrAxe = new BooleanValue("Only Sword or Axe", true);
    private final BooleanValue onlyWhenMouseDown = new BooleanValue("Only Mouse Hold", false);
    private final BooleanValue samePlayer = new BooleanValue("Same Player", false);

    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil samePlayerTimer = new TimerUtil();
    private final TimerUtil timerReactionTime = new TimerUtil();

    private boolean waitingForDelay = false;
    private boolean waitingForReaction = false;
    private long currentReactionDelay = 0;
    private float randomizedPostDelay = 0;
    private float randomizedThreshold = 0;
    private Entity target;
    private String lastTargetUUID = null;

    public TriggerBot() {
        super("TriggerBot", "Makes you automatically attack once aimed at a target", ModuleType.COMBAT);
        addValues(
                swordThresholdMin, swordThresholdMax,
                axeThresholdMin, axeThresholdMax,
                axePostDelayMin, axePostDelayMax,
                reactionTimeMin, reactionTimeMax,
                cooldownMode, critMode,
                autoUnBlock, autoUnSprint,
                ignorePassiveMobs, ignoreCrystals,
                respectShields, ignoreUsing, ignoreInvisible,
                onlyWhenMouseDown, useOnlySwordOrAxe,
                samePlayer);
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (isNull())
            return;

        if (!ignoreUsing.isEnabled() && !autoUnBlock.isEnabled() && mc.player.isUsingItem())
            return;

        if (mc.currentScreen != null)
            return;

        target = mc.targetedEntity;
        if (target == null)
            return;

        if (!isHoldingSwordOrAxe())
            return;

        if (onlyWhenMouseDown.isEnabled() &&
                GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
            return;
        }

        if (!hasTarget(target))
            return;

        if (respectShields.isEnabled()) {
            Item item = mc.player.getMainHandStack().getItem();
            if (target instanceof PlayerEntity playerTarget &&
                    isShieldFacingAway(playerTarget) &&
                    ItemUtils.isSwordItem(item)) {

                return;
            }
        }

        if (target != null && (!target.getUuidAsString().equals(lastTargetUUID))) {
            lastTargetUUID = target.getUuidAsString();
        }

        if (!waitingForReaction) {
            waitingForReaction = true;
            timerReactionTime.reset();

            long delay;
            switch (cooldownMode.getValue()) {
                case "Smart" -> {
                    double distance = mc.player.distanceTo(target);
                    double maxDistance = 3.0;
                    double multiplier = distance < maxDistance / 2 ? 0.66 : 1.0;
                    delay = (long) MathUtils.randomDoubleBetween(
                            reactionTimeMin.getValue().doubleValue(),
                            reactionTimeMax.getValue().doubleValue());
                    delay *= (long) multiplier;
                }
                case "None" -> delay = 0;
                default -> delay = (long) MathUtils.randomDoubleBetween(
                        reactionTimeMin.getValue().doubleValue(),
                        reactionTimeMax.getValue().doubleValue());
            }

            currentReactionDelay = delay;
        }

        if (waitingForReaction && timerReactionTime.hasElapsedTime(currentReactionDelay, true)) {
            if (critMode.getValue().equals("Strict")) {
                if (!mc.player.isOnGround() && !mc.player.isClimbing()) {
                    if (canCrit()
                            && mc.player.getAttackCooldownProgress(0.0f) >= swordThresholdMin.getValue().floatValue()) {
                        if (hasTarget(target) && samePlayerCheck(target)) {
                            attack();
                            waitingForReaction = false;
                        }
                    }
                } else {
                    if (hasElapsedDelay() && hasTarget(target) && samePlayerCheck(target)) {
                        attack();
                        waitingForReaction = false;
                    }
                }
            } else {
                if (hasElapsedDelay() && hasTarget(target) && samePlayerCheck(target)) {
                    attack();
                    waitingForReaction = false;
                }
            }
        }
    }

    private boolean samePlayerCheck(Entity entity) {
        if (!samePlayer.isEnabled())
            return true;
        if (entity == null)
            return false;

        if (lastTargetUUID == null || samePlayerTimer.hasElapsedTime(3000, false)) {
            lastTargetUUID = entity.getUuidAsString();
            samePlayerTimer.reset();
            return true;
        }
        return entity.getUuidAsString().equals(lastTargetUUID);
    }

    private boolean canCrit() {
        if (mc.player == null)
            return false;

        return !mc.player.isOnGround()
                && !mc.player.isClimbing()
                && !mc.player.isInLava()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.fallDistance > 0.065f
                && mc.player.getVehicle() == null;
    }

    private boolean setPreferCrits() {
        if (mc.player == null || mc.world == null)
            return false;

        String mode = critMode.getValue();
        if (mode.equals("None"))
            return false;

        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)
                || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)
                || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            return false;
        }

        if (!(mc.crosshairTarget instanceof EntityHitResult hitResult))
            return false;

        Entity targetEntity = hitResult.getEntity();
        if (targetEntity != target || !hasTarget(targetEntity))
            return false;

        if (mc.player.isTouchingWater()
                || mc.player.isInLava()
                || mc.player.isSubmergedInWater()
                || mc.player.isClimbing()) {
            return false;
        }

        BlockState state = mc.world.getBlockState(mc.player.getBlockPos());
        if (state.isOf(Blocks.COBWEB)
                || state.isOf(Blocks.SWEET_BERRY_BUSH)
                || state.isOf(Blocks.VINE)
                || state.isOf(Blocks.SCAFFOLDING)
                || state.isOf(Blocks.SLIME_BLOCK)
                || state.isOf(Blocks.HONEY_BLOCK)
                || state.isOf(Blocks.POWDER_SNOW)) {
            return false;
        }

        boolean cooldownReady = mc.player.getAttackCooldownProgress(0.0f) >= swordThresholdMin.getValue().floatValue();
        return mode.equals("Strict") && cooldownReady && canCrit();
    }

    private boolean hasElapsedDelay() {
        if (setPreferCrits())
            return false;

        Item heldItem = mc.player.getMainHandStack().getItem();
        float cooldown = mc.player.getAttackCooldownProgress(0.0f);

        if (heldItem instanceof AxeItem) {
            if (!waitingForDelay) {
                randomizedThreshold = (float) MathUtils.randomDoubleBetween(
                        axeThresholdMin.getValue().doubleValue(),
                        axeThresholdMax.getValue().doubleValue());
                randomizedPostDelay = (float) MathUtils.randomDoubleBetween(
                        axePostDelayMin.getValue().doubleValue(),
                        axePostDelayMax.getValue().doubleValue());
                waitingForDelay = true;
            }
            if (cooldown >= randomizedThreshold) {
                if (timer.hasElapsedTime((long) randomizedPostDelay, true)) {
                    waitingForDelay = false;
                    return true;
                }
            } else {
                timer.reset();
            }
            return false;
        } else {
            float swordDelay = (float) MathUtils.randomDoubleBetween(
                    swordThresholdMin.getValue().doubleValue(),
                    swordThresholdMax.getValue().doubleValue());
            return cooldown >= swordDelay;
        }
    }

    private boolean isHoldingSwordOrAxe() {
        if (!useOnlySwordOrAxe.isEnabled())
            return true;

        Item item = mc.player.getMainHandStack().getItem();
        return item instanceof AxeItem || ItemUtils.isSwordItem(item);
    }

    private boolean isShieldFacingAway(PlayerEntity player) {
        if (!player.isBlocking())
            return false;

        double dx = mc.player.getX() - player.getX();
        double dz = mc.player.getZ() - player.getZ();
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double yawDiff = Math.abs(((angle - player.getYaw()) % 360 + 540) % 360 - 180);
        return yawDiff > 90.0;
    }

    public void attack() {
        if (autoUnBlock.isEnabled() && mc.player.isUsingItem()) {
            Item offhandItem = mc.player.getOffHandStack().getItem();
            if (ItemUtils.isShieldItem(offhandItem)) {
                if (mc.interactionManager == null) {
                    return;
                }
                mc.interactionManager.stopUsingItem(mc.player);
            }
        }
        if (autoUnSprint.isEnabled() && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }
        ((MinecraftClientAccessor) mc).invokeDoAttack();
        if (samePlayer.isEnabled() && target != null) {
            lastTargetUUID = target.getUuidAsString();
            samePlayerTimer.reset();
        }
        waitingForDelay = false;
    }

    public boolean hasTarget(Entity en) {
        if (en == mc.player || !en.isAlive())
            return false;

        return switch (en) {
            case EndCrystalEntity ignored when ignoreCrystals.isEnabled() -> false;
            case Tameable ignored -> false;
            case PassiveEntity ignored when ignorePassiveMobs.isEnabled() -> false;
            default -> !ignoreInvisible.isEnabled() || !en.isInvisible();
        };
    }

    @Override
    protected void onEnable() {
        timer.reset();
        timerReactionTime.reset();
        waitingForReaction = false;
        waitingForDelay = false;
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        timer.reset();
        timerReactionTime.reset();
        waitingForReaction = false;
        waitingForDelay = false;
        super.onDisable();
    }
}
