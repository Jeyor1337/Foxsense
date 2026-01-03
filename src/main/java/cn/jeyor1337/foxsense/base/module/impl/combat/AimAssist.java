package cn.jeyor1337.foxsense.base.module.impl.combat;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventRender3D;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.item.ItemUtils;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {

    private final NumberValue maxYawSpeed = new NumberValue("Max Yaw Speed", 2.0, 0.1, 5.0, 0.1);
    private final NumberValue minYawSpeed = new NumberValue("Min Yaw Speed", 2.0, 0.1, 5.0, 0.1);

    private final NumberValue minPitchSpeed = new NumberValue("Min Pitch Speed", 2.0, 0.1, 5.0, 0.1);
    private final NumberValue maxPitchSpeed = new NumberValue("Max Pitch Speed", 2.0, 0.1, 5.0, 0.1);

    private final NumberValue fov = new NumberValue("FOV", 90.0, 10.0, 180.0, 1.0);
    private final NumberValue range = new NumberValue("Range", 4.5, 1.0, 10.0, 0.1);
    private final NumberValue smoothing = new NumberValue("Smoothing", 10.0, 1.0, 20.0, 0.5);
    private final NumberValue pitchThreshold = new NumberValue("Pitch Threshold", 60.0, 0.0, 90.0, 1.0);

    private final BooleanValue targetPlayers = new BooleanValue("Target Players", true);
    private final BooleanValue targetMobs = new BooleanValue("Target Mobs", false);
    private final BooleanValue weaponsOnly = new BooleanValue("Weapons Only", false);
    private final BooleanValue throughWalls = new BooleanValue("Through Walls", false);
    private final BooleanValue disableOnTarget = new BooleanValue("Disable on target", false);
    private final BooleanValue ignoreBlocks = new BooleanValue("Ignore Blocks", true);

    private Entity currentTarget = null;
    private long lastUpdateTime = 0;
    private float currentBaseSpeed = 10f;
    private float nextBaseSpeed = 10f;
    private long lastSpeedChangeTime = 0;

    public AimAssist() {
        super("Aim Assist", "Gives you assistance on your aim", ModuleType.COMBAT);
        addValues(
                maxYawSpeed, minYawSpeed, maxPitchSpeed, minPitchSpeed, fov, range, smoothing, pitchThreshold,
                targetPlayers, targetMobs, weaponsOnly, throughWalls, disableOnTarget, ignoreBlocks);
    }

    @EventTarget
    private void onRender3D(EventRender3D event) {
        if (isNull())
            return;
        if (maxPitchSpeed.getValue().floatValue() <= minPitchSpeed.getValue().floatValue()) {
            maxPitchSpeed.setValue(minPitchSpeed.getValue().doubleValue() + 0.1);
        }
        if (maxYawSpeed.getValue().floatValue() <= minYawSpeed.getValue().floatValue()) {
            maxYawSpeed.setValue(minYawSpeed.getValue().doubleValue() + 0.1);
        }
        if (weaponsOnly.isEnabled() && !isHoldingWeapon())
            return;

        if (mc.currentScreen != null)
            return;

        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK
                && mc.options.attackKey.isPressed()) {
            return;
        }

        if (mc.player.getPitch() > pitchThreshold.getValue().floatValue()) {
            return;
        }

        if (ignoreBlocks.isEnabled() && mc.crosshairTarget != null
                && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            return;
        }

        currentTarget = findBestTarget();

        if (mc.targetedEntity == currentTarget && disableOnTarget.isEnabled()) {
            return;
        }

        if (currentTarget != null) {
            if (!throughWalls.isEnabled() && !mc.player.canSee(currentTarget))
                return;

            Vec3d chestPos = getChestPosition(currentTarget);
            float[] rotation = calculateRotation(chestPos);
            applySmoothAiming(rotation[0], rotation[1]);
        }
    }

    private Entity findBestTarget() {
        if (isNull())
            return null;

        Entity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!isValidTarget(entity))
                continue;

            double distance = mc.player.distanceTo(entity);
            if (distance > range.getValue().doubleValue())
                continue;

            Vec3d chestPos = getChestPosition(entity);
            float[] rotation = calculateRotation(chestPos);
            double fovDistance = getFOVDistance(rotation[0], rotation[1]);

            if (fovDistance <= fov.getValue().doubleValue() / 2.0) {
                double score = distance + (fovDistance * 2.0);
                if (score < bestScore) {
                    bestScore = score;
                    bestTarget = entity;
                }
            }
        }

        return bestTarget;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == null || entity == mc.player || !(entity instanceof LivingEntity livingEntity))
            return false;
        if (!livingEntity.isAlive() || livingEntity.isDead())
            return false;

        return entity instanceof PlayerEntity ? targetPlayers.isEnabled() : targetMobs.isEnabled();
    }

    private Vec3d getChestPosition(Entity entity) {
        return new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
    }

    private float[] calculateRotation(Vec3d target) {
        Vec3d diff = target.subtract(mc.player.getEyePos());
        double distance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, distance));
        return new float[] { MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -89.0f, 89.0f) };
    }

    private double getFOVDistance(float targetYaw, float targetPitch) {
        float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDiff = targetPitch - mc.player.getPitch();
        return Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
    }

    private void applySmoothAiming(float targetYaw, float targetPitch) {
        long currentTime = System.currentTimeMillis();

        if (lastUpdateTime == 0) {
            lastUpdateTime = currentTime;
            return;
        }

        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        if (deltaTime < 0.001f || deltaTime > 0.1f)
            return;
        deltaTime *= randomFloat(0.9f, 1.1f);

        updateBaseSpeed();

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float distance = (float) Math.hypot(yawDiff, pitchDiff);
        if (distance < 0.3f)
            return;

        float t = Math.min(distance / 10f, 1f);
        float eased = easeOutCubic(t);

        float lerpFactor = eased * (smoothing.getValue().floatValue() / 10f) * deltaTime;
        float newYaw = MathHelper.lerp(lerpFactor, currentYaw, currentYaw + yawDiff);
        float newPitch = MathHelper.lerp(lerpFactor, currentPitch, currentPitch + pitchDiff);

        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -89f, 89f));
    }

    private float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1f - t, 3);
    }

    private void updateBaseSpeed() {
        long now = System.currentTimeMillis();
        if (now - lastSpeedChangeTime > 200) {
            lastSpeedChangeTime = now;
            float change = randomFloat(-5f, 5f);
            nextBaseSpeed += change;
            nextBaseSpeed = MathHelper.clamp(nextBaseSpeed, 8f, 100f);
        }

        currentBaseSpeed = (currentBaseSpeed * 0.9f) + (nextBaseSpeed * 0.1f);
    }

    private float randomFloat(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    private boolean isHoldingWeapon() {
        if (mc.player == null)
            return false;
        if (mc.player.getMainHandStack().isEmpty())
            return false;
        Item heldItem = mc.player.getMainHandStack().getItem();
        return ItemUtils.isSwordItem(heldItem) || heldItem instanceof AxeItem;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        currentTarget = null;
    }
}
