package cn.jeyor1337.foxsense.base.module.impl.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.cubk.event.annotations.EventTarget;
import com.google.gson.Gson;

import cn.jeyor1337.foxsense.base.event.EventRender3D;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class AimDataCollector extends Module {

    private final NumberValue range = new NumberValue("Range", 6.0, 1.0, 10.0, 0.1);
    private final NumberValue minSamples = new NumberValue("Min Samples Before Save", 1000.0, 100.0, 10000.0, 100.0);
    private final BooleanValue onlyWhenAiming = new BooleanValue("Only When Aiming", true);
    private final BooleanValue autoSave = new BooleanValue("Auto Save", true);
    private final NumberValue autoSaveInterval = new NumberValue("Auto Save Interval (samples)", 5000.0, 1000.0,
            50000.0, 1000.0, autoSave::getValue);

    private final List<AimSample> samples = new ArrayList<>();
    private ExecutorService saveExecutor;
    private volatile boolean isSaving = false;
    private long lastSampleTime = 0;
    private float lastYaw = 0;
    private float lastPitch = 0;
    private Entity lastTarget = null;
    private Vec3d lastTargetPos = null;
    private int sessionId = 0;
    private int totalSavedSamples = 0;

    public AimDataCollector() {
        super("AimDataCollector", "Collects aiming data for ML training", ModuleType.MISC);
        addValues(range, minSamples, onlyWhenAiming, autoSave, autoSaveInterval);
    }

    @EventTarget
    private void onRender3D(EventRender3D event) {
        if (isNull())
            return;
        if (mc.currentScreen != null)
            return;

        Entity target = findTargetByAim();
        if (target == null)
            return;

        if (onlyWhenAiming.isEnabled()) {
            float[] targetRotation = calculateRotationTo(target);
            float yawDiff = Math.abs(wrapDegrees(targetRotation[0] - mc.player.getYaw()));
            float pitchDiff = Math.abs(targetRotation[1] - mc.player.getPitch());
            if (yawDiff > 45 || pitchDiff > 30)
                return;
        }

        long currentTime = System.currentTimeMillis();

        if (lastSampleTime > 0 && lastTarget == target && lastTargetPos != null) {
            float deltaTime = (currentTime - lastSampleTime) / 1000.0f;

            if (deltaTime > 0.001f && deltaTime < 0.5f) {
                float currentYaw = mc.player.getYaw();
                float currentPitch = mc.player.getPitch();

                float outputDeltaYaw = wrapDegrees(currentYaw - lastYaw);
                float outputDeltaPitch = currentPitch - lastPitch;

                if (Math.abs(outputDeltaYaw) > 0.01f || Math.abs(outputDeltaPitch) > 0.01f) {
                    AimSample sample = createSample(target, outputDeltaYaw, outputDeltaPitch);
                    samples.add(sample);

                    if (autoSave.isEnabled() && samples.size() >= autoSaveInterval.getValue().intValue()) {
                        saveData();
                    }
                }
            }
        }

        lastSampleTime = currentTime;
        lastYaw = mc.player.getYaw();
        lastPitch = mc.player.getPitch();
        lastTarget = target;
        lastTargetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
    }

    private AimSample createSample(Entity target, float outputDeltaYaw, float outputDeltaPitch) {
        AimSample sample = new AimSample();

        Vec3d playerPos = mc.player.getEyePos();
        Vec3d targetPos = getTargetCenter(target);
        Vec3d diff = targetPos.subtract(playerPos);

        float[] targetRotation = calculateRotationTo(target);
        sample.deltaYaw = wrapDegrees(targetRotation[0] - mc.player.getYaw());
        sample.deltaPitch = targetRotation[1] - mc.player.getPitch();

        sample.deltaX = (float) diff.x;
        sample.deltaY = (float) diff.y;
        sample.deltaZ = (float) diff.z;
        sample.distance = (float) mc.player.distanceTo(target);

        sample.outputDeltaYaw = outputDeltaYaw;
        sample.outputDeltaPitch = outputDeltaPitch;

        return sample;
    }

    private Vec3d getTargetCenter(Entity target) {
        if (target instanceof LivingEntity living) {
            return new Vec3d(target.getX(), target.getY() + living.getHeight() / 2.0, target.getZ());
        }
        return new Vec3d(target.getX(), target.getY(), target.getZ());
    }

    private Entity findTargetByAim() {
        Entity bestTarget = null;
        double bestScore = Double.MAX_VALUE;
        double maxRange = range.getValue().doubleValue();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity))
                continue;
            if (entity == mc.player)
                continue;
            if (!((LivingEntity) entity).isAlive())
                continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > maxRange)
                continue;

            float[] rotation = calculateRotationTo(entity);
            float yawDiff = Math.abs(wrapDegrees(rotation[0] - mc.player.getYaw()));
            float pitchDiff = Math.abs(rotation[1] - mc.player.getPitch());
            double angleDist = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

            double score = angleDist + (dist * 0.5);

            if (score < bestScore) {
                bestScore = score;
                bestTarget = entity;
            }
        }

        return bestTarget;
    }

    private float[] calculateRotationTo(Entity target) {
        Vec3d diff = getTargetCenter(target).subtract(mc.player.getEyePos());
        double distance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, distance));
        return new float[] { wrapDegrees(yaw), pitch };
    }

    private float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0f;
        if (wrapped >= 180.0f)
            wrapped -= 360.0f;
        if (wrapped < -180.0f)
            wrapped += 360.0f;
        return wrapped;
    }

    private void saveData() {
        if (samples.isEmpty() || isSaving)
            return;

        List<AimSample> samplesToSave = new ArrayList<>(samples);
        samples.clear();
        int currentSessionId = sessionId++;
        int sampleCount = samplesToSave.size();

        isSaving = true;
        saveExecutor.submit(() -> {
            try {
                File dir = new File(MinecraftClient.getInstance().runDirectory, "foxsense/training_data");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File file = new File(dir, "aim_data_" + timestamp + "_" + currentSessionId + ".json");

                DataExport export = new DataExport();
                export.version = "3.0";
                export.timestamp = System.currentTimeMillis();
                export.sampleCount = sampleCount;
                export.samples = samplesToSave;

                Gson gson = new Gson();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    gson.toJson(export, writer);
                }

                totalSavedSamples += sampleCount;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                isSaving = false;
            }
        });
    }

    @Override
    public void onEnable() {
        super.onEnable();
        samples.clear();
        lastSampleTime = 0;
        lastTarget = null;
        lastTargetPos = null;
        sessionId = 0;
        totalSavedSamples = 0;
        isSaving = false;
        saveExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (samples.size() >= minSamples.getValue().intValue()) {
            saveDataSync();
        }
        samples.clear();

        if (saveExecutor != null) {
            saveExecutor.shutdown();
            try {
                saveExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            saveExecutor = null;
        }
    }

    private void saveDataSync() {
        if (samples.isEmpty())
            return;

        try {
            File dir = new File(MinecraftClient.getInstance().runDirectory, "foxsense/training_data");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(dir, "aim_data_" + timestamp + "_" + sessionId + ".json");

            DataExport export = new DataExport();
            export.version = "3.0";
            export.timestamp = System.currentTimeMillis();
            export.sampleCount = samples.size();
            export.samples = new ArrayList<>(samples);

            Gson gson = new Gson();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                gson.toJson(export, writer);
            }

            totalSavedSamples += samples.size();
            samples.clear();
            sessionId++;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class AimSample {
        public float deltaYaw;
        public float deltaPitch;
        public float deltaX;
        public float deltaY;
        public float deltaZ;
        public float distance;
        public float outputDeltaYaw;
        public float outputDeltaPitch;
    }

    public static class DataExport {
        public String version;
        public long timestamp;
        public int sampleCount;
        public List<AimSample> samples;
    }
}
