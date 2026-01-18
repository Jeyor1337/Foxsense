package cn.jeyor1337.foxsense.base.module.impl.combat;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventRender3D;
import cn.jeyor1337.foxsense.base.ml.AimFeatures;
import cn.jeyor1337.foxsense.base.ml.AimOutput;
import cn.jeyor1337.foxsense.base.ml.MLAimPredictor;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.item.ItemUtils;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.ModeValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {

    private final ModeValue aimMode = new ModeValue("Aim Mode", new String[] { "Normal", "ML" }, "Normal");

    private final NumberValue maxYawSpeed = new NumberValue("Max Yaw Speed", 2.0, 0.1, 5.0, 0.1,
            () -> aimMode.getValue().equals("Normal"));
    private final NumberValue minYawSpeed = new NumberValue("Min Yaw Speed", 2.0, 0.1, 5.0, 0.1,
            () -> aimMode.getValue().equals("Normal"));

    private final BooleanValue pitchEnabled = new BooleanValue("Pitch", true);
    private final NumberValue minPitchSpeed = new NumberValue("Min Pitch Speed", 2.0, 0.1, 5.0, 0.1,
            () -> aimMode.getValue().equals("Normal") && pitchEnabled.getValue());
    private final NumberValue maxPitchSpeed = new NumberValue("Max Pitch Speed", 2.0, 0.1, 5.0, 0.1,
            () -> aimMode.getValue().equals("Normal") && pitchEnabled.getValue());

    private final NumberValue fov = new NumberValue("FOV", 90.0, 10.0, 180.0, 1.0);
    private final NumberValue range = new NumberValue("Range", 4.5, 1.0, 10.0, 0.1);
    private final NumberValue smoothing = new NumberValue("Smoothing", 10.0, 1.0, 100.0, 0.5,
            () -> aimMode.getValue().equals("Normal"));
    private final NumberValue pitchThreshold = new NumberValue("Pitch Threshold", 60.0, 0.0, 90.0, 1.0,
            pitchEnabled::getValue);

    private final BooleanValue targetPlayers = new BooleanValue("Target Players", true);
    private final BooleanValue targetMobs = new BooleanValue("Target Mobs", false);
    private final BooleanValue weaponsOnly = new BooleanValue("Weapons Only", false);
    private final BooleanValue throughWalls = new BooleanValue("Through Walls", false);
    private final BooleanValue disableOnTarget = new BooleanValue("Disable on target", false);
    private final BooleanValue ignoreBlocks = new BooleanValue("Ignore Blocks", true);
    private final BooleanValue aimNearestPoint = new BooleanValue("Aim Nearest Point", false);

    private final BooleanValue prediction = new BooleanValue("Prediction", false,
            () -> aimMode.getValue().equals("Normal"));
    private final ModeValue predictionMode = new ModeValue(
            "Prediction Mode", new String[] { "Simple", "Adaptive", "Smart" }, "Smart",
            () -> aimMode.getValue().equals("Normal") && prediction.getValue());
    private final NumberValue predictionStrength = new NumberValue("Prediction Strength", 1.0, 0.0, 2.0, 0.1,
            () -> aimMode.getValue().equals("Normal") && prediction.getValue()
                    && !predictionMode.getValue().equals("Smart"));

    private final NumberValue mlScale = new NumberValue("ML Scale", 1.0, 0.1, 3.0, 0.1,
            () -> aimMode.getValue().equals("ML"));

    private final BooleanValue gcdFix = new BooleanValue("GCD Fix", true);

    private Entity currentTarget = null;
    private long lastUpdateTime = 0;
    private float currentBaseSpeed = 10f;
    private float nextBaseSpeed = 10f;
    private long lastSpeedChangeTime = 0;
    private Vec3d lastTargetVelocity = Vec3d.ZERO;
    private long lastPredictionTime = 0;
    private Entity lastSmartTarget = null;
    private Vec3d lastSmartVelocity = Vec3d.ZERO;
    private long lastSmartUpdateTime = 0;

    private boolean mlInitialized = false;

    private float accumulatedYaw = 0f;
    private float accumulatedPitch = 0f;

    public AimAssist() {
        super("AimAssist", "Gives you assistance on your aim", ModuleType.COMBAT);
        addValues(
                aimMode, maxYawSpeed, minYawSpeed, maxPitchSpeed, minPitchSpeed, fov, range, smoothing,
                pitchThreshold,
                pitchEnabled, targetPlayers, targetMobs, weaponsOnly, throughWalls, disableOnTarget, ignoreBlocks,
                aimNearestPoint, prediction, predictionMode, predictionStrength, mlScale, gcdFix);
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

            Vec3d aimPos = getAimPosition(currentTarget);
            float[] rotation = calculateRotation(aimPos);

            if (aimMode.getValue().equals("ML")) {
                applyMLAiming(currentTarget, aimPos, rotation[0], rotation[1]);
            } else {
                applySmoothAiming(rotation[0], rotation[1]);
            }
        }
    }

    private void applyMLAiming(Entity target, Vec3d aimPos, float targetYaw, float targetPitch) {
        if (!mlInitialized) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (lastUpdateTime == 0) {
            lastUpdateTime = currentTime;
            return;
        }

        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        if (deltaTime < 0.001f || deltaTime > 0.1f)
            return;

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        Vec3d eyePos = mc.player.getEyePos();
        float deltaX = (float) (aimPos.x - eyePos.x);
        float deltaY = (float) (aimPos.y - eyePos.y);
        float deltaZ = (float) (aimPos.z - eyePos.z);
        float distance = (float) mc.player.distanceTo(target);

        AimFeatures features = new AimFeatures(
                yawDiff,
                pitchDiff,
                deltaX,
                deltaY,
                deltaZ,
                distance);

        AimOutput output = MLAimPredictor.getInstance().predict(features);

        float scale = mlScale.getValue().floatValue();

        if (gcdFix.isEnabled()) {
            float gcd = (float) getGCD();

            accumulatedYaw += output.deltaYaw * scale;
            float yawToApply = accumulatedYaw - (accumulatedYaw % gcd);
            if (Math.abs(yawToApply) >= gcd) {
                mc.player.setYaw(currentYaw + yawToApply);
                accumulatedYaw -= yawToApply;
            }

            if (pitchEnabled.isEnabled()) {
                accumulatedPitch += output.deltaPitch * scale;
                float pitchToApply = accumulatedPitch - (accumulatedPitch % gcd);
                if (Math.abs(pitchToApply) >= gcd) {
                    mc.player.setPitch(MathHelper.clamp(currentPitch + pitchToApply, -89f, 89f));
                    accumulatedPitch -= pitchToApply;
                }
            }
        } else {
            mc.player.setYaw(currentYaw + output.deltaYaw * scale);
            if (pitchEnabled.isEnabled()) {
                mc.player.setPitch(MathHelper.clamp(currentPitch + output.deltaPitch * scale, -89f, 89f));
            }
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

            Vec3d aimPos = getAimPosition(entity);
            float[] rotation = calculateRotation(aimPos);
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

    private Vec3d getAimPosition(Entity entity) {
        Vec3d basePos = aimNearestPoint.isEnabled() ? getNearestPoint(entity) : getChestPosition(entity);

        if (!prediction.isEnabled() || aimMode.getValue().equals("ML")) {
            return basePos;
        }

        Vec3d velocity = entity.getVelocity();
        double playerDistance = mc.player.distanceTo(entity);
        float strength = predictionStrength.getValue().floatValue();

        Vec3d predictedPos = basePos;

        switch (predictionMode.getValue()) {
            case "Simple":
                predictedPos = predictSimple(basePos, velocity, strength);
                break;
            case "Adaptive":
                predictedPos = predictAdaptive(basePos, velocity, strength, playerDistance);
                break;
            case "Smart":
                predictedPos = predictSmart(basePos, entity, playerDistance);
                break;
        }

        return predictedPos;
    }

    private Vec3d getChestPosition(Entity entity) {
        double centerX = entity.getX();
        double centerY;
        double centerZ = entity.getZ();

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            centerY = entity.getY() + livingEntity.getHeight() / 2.0;
        } else {
            centerY = entity.getY();
        }

        return new Vec3d(centerX, centerY, centerZ);
    }

    private Vec3d getNearestPoint(Entity entity) {
        Vec3d eyePos = mc.player.getEyePos();
        Box box = entity.getBoundingBox();
        double x = MathHelper.clamp(eyePos.x, box.minX, box.maxX);
        double y = MathHelper.clamp(eyePos.y, box.minY, box.maxY);
        double z = MathHelper.clamp(eyePos.z, box.minZ, box.maxZ);
        return new Vec3d(x, y, z);
    }

    private Vec3d predictSimple(Vec3d basePos, Vec3d velocity, float strength) {
        return basePos.add(velocity.multiply(strength));
    }

    private Vec3d predictAdaptive(Vec3d basePos, Vec3d velocity, float strength, double distance) {
        double maxRange = range.getValue().doubleValue();
        double distanceFactor = distance / maxRange;
        double adaptiveStrength = strength * distanceFactor * 1.5;
        return basePos.add(velocity.multiply(adaptiveStrength));
    }

    private Vec3d predictSmart(Vec3d basePos, Entity target, double distance) {
        long currentTime = System.currentTimeMillis();
        Vec3d currentVelocity = target.getVelocity();
        Vec3d playerVelocity = mc.player.getVelocity();

        Vec3d relativeVelocity = currentVelocity.subtract(playerVelocity);
        double targetSpeed = currentVelocity.horizontalLength();

        if (targetSpeed < 0.01) {
            return basePos;
        }

        Vec3d acceleration = Vec3d.ZERO;
        if (lastSmartTarget == target && lastSmartUpdateTime > 0) {
            long timeDiff = currentTime - lastSmartUpdateTime;
            if (timeDiff > 0 && timeDiff < 500) {
                acceleration = currentVelocity.subtract(lastSmartVelocity).multiply(1000.0 / timeDiff);
            }
        }

        lastSmartTarget = target;
        lastSmartVelocity = currentVelocity;
        lastSmartUpdateTime = currentTime;

        double aimTime = distance / 3.0;
        double speedFactor = Math.min(targetSpeed / 0.2, 2.0);
        double predictionTicks = aimTime * (0.5 + speedFactor * 0.5);
        predictionTicks = MathHelper.clamp(predictionTicks, 0.5, 5.0);

        double predictionTime = predictionTicks * 0.05;

        Vec3d predictedOffset = relativeVelocity.multiply(predictionTime);

        if (acceleration.lengthSquared() > 0.0001) {
            Vec3d accelerationOffset = acceleration.multiply(0.5 * predictionTime * predictionTime);
            predictedOffset = predictedOffset.add(accelerationOffset);
        }

        return basePos.add(predictedOffset);
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
        if (pitchEnabled.isEnabled()) {
            float pitchDiff = targetPitch - mc.player.getPitch();
            return Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
        } else {
            return Math.abs(yawDiff);
        }
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

        float distance;
        if (pitchEnabled.isEnabled()) {
            distance = (float) Math.hypot(yawDiff, pitchDiff);
        } else {
            distance = Math.abs(yawDiff);
        }

        if (distance < 0.3f)
            return;

        float t = Math.min(distance / 10f, 1f);
        float eased = easeOutCubic(t);

        float lerpFactor = eased * (smoothing.getValue().floatValue() / 10f) * deltaTime;
        float newYaw = MathHelper.lerp(lerpFactor, currentYaw, currentYaw + yawDiff);

        if (gcdFix.isEnabled()) {
            newYaw = fixRotation(currentYaw, newYaw);
        }
        mc.player.setYaw(newYaw);

        if (pitchEnabled.isEnabled()) {
            float newPitch = MathHelper.lerp(lerpFactor, currentPitch, currentPitch + pitchDiff);
            if (gcdFix.isEnabled()) {
                newPitch = fixRotation(currentPitch, newPitch);
            }
            mc.player.setPitch(MathHelper.clamp(newPitch, -89f, 89f));
        }
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

    private double getGCD() {
        double d = mc.options.getMouseSensitivity().getValue() * 0.6000000238418579 + 0.20000000298023224;
        double e = d * d * d;
        double f = e * 8.0;
        return f;
    }

    private float fixRotation(float current, float target) {
        float gcd = (float) getGCD();
        float delta = target - current;
        delta -= delta % gcd;
        return current + delta;
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

        if (aimMode.getValue().equals("ML")) {
            mlInitialized = MLAimPredictor.getInstance().initialize();
            if (!mlInitialized) {
                System.out.println("[AimAssist] ML model failed to load, falling back to Normal mode");
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        currentTarget = null;
        lastTargetVelocity = Vec3d.ZERO;
        lastPredictionTime = 0;
        lastSmartTarget = null;
        lastSmartVelocity = Vec3d.ZERO;
        lastSmartUpdateTime = 0;
        mlInitialized = false;
        accumulatedYaw = 0f;
        accumulatedPitch = 0f;
    }
}
