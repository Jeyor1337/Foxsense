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
import net.minecraft.entity.Entity;

public class STap extends Module {
    public static final NumberValue chance = new NumberValue("Chance (%)", 100, 1, 100, 1);
    private final NumberValue msDelay = new NumberValue("Ms", 60, 1, 500, 1);
    private final BooleanValue onlyOnGround = new BooleanValue("Only on ground", true);
    private final BooleanValue alwaysKeepRange = new BooleanValue("Always KeepRange", false);
    private final NumberValue keepRange = new NumberValue("KeepRange", 3.0, 0.5, 6.0, 0.1, alwaysKeepRange::getValue);
    private final NumberValue keepRangeTimeout = new NumberValue("Keep Timeout", 500, 100, 2000, 50,
            alwaysKeepRange::getValue);
    boolean wasSprinting;
    boolean isKeepingRange;
    Entity lastTarget;
    TimerUtil timer = new TimerUtil();
    TimerUtil attackTimer = new TimerUtil();

    public STap() {
        super("STap", "Makes you automatically STAP", ModuleType.COMBAT);
        this.addValues(msDelay, chance, onlyOnGround, alwaysKeepRange, keepRange, keepRangeTimeout);
    }

    @Override
    protected void onDisable() {
        if (mc.options != null && mc.options.backKey != null) {
            mc.options.backKey.setPressed(false);
        }
        wasSprinting = false;
        isKeepingRange = false;
        lastTarget = null;
    }

    @EventTarget
    private void onAttackEvent(EventAttack event) {
        if (isNull())
            return;
        var target = event.getTargetEntity();
        if (target == null)
            return;
        if (!target.isAlive())
            return;

        lastTarget = target;
        attackTimer.reset();

        if (!mc.player.isOnGround() && onlyOnGround.getValue())
            return;
        if (Math.random() * 100 > chance.getValue().floatValue())
            return;
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W))
            return;
        if (mc.player.isSprinting()) {
            wasSprinting = true;
            mc.options.backKey.setPressed(true);
        }
    }

    @EventTarget
    private void onTickEvent(EventTick event) {
        if (isNull())
            return;

        if (alwaysKeepRange.getValue()) {
            if (onlyOnGround.getValue() && !mc.player.isOnGround()) {
                if (isKeepingRange) {
                    mc.options.backKey.setPressed(false);
                    isKeepingRange = false;
                }
            } else if (attackTimer.hasElapsedTime(keepRangeTimeout.getValue().intValue(), false)) {
                if (lastTarget != null) {
                    lastTarget = null;
                    if (isKeepingRange) {
                        mc.options.backKey.setPressed(false);
                        isKeepingRange = false;
                    }
                }
            } else if (lastTarget != null && lastTarget.isAlive()) {
                double distanceToTarget = getDistanceToEntityBoundingBox(lastTarget);
                if (distanceToTarget < keepRange.getValue().doubleValue()) {
                    mc.options.backKey.setPressed(true);
                    isKeepingRange = true;
                } else {
                    if (isKeepingRange) {
                        mc.options.backKey.setPressed(false);
                        isKeepingRange = false;
                    }
                }
            } else {
                if (isKeepingRange) {
                    mc.options.backKey.setPressed(false);
                    isKeepingRange = false;
                }
            }
        } else {
            if (isKeepingRange) {
                mc.options.backKey.setPressed(false);
                isKeepingRange = false;
            }
        }

        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W))
            return;
        if (timer.hasElapsedTime(msDelay.getValue().intValue(), true)) {
            if (wasSprinting) {
                mc.options.backKey.setPressed(false);
                wasSprinting = false;
            }
        }
    }

    private double getDistanceToEntityBoundingBox(Entity entity) {
        var eyePos = mc.player.getEyePos();
        var lookVec = mc.player.getRotationVec(1.0F);
        var box = entity.getBoundingBox();

        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        double closestX = Math.max(minX, Math.min(eyePos.x, maxX));
        double closestY = Math.max(minY, Math.min(eyePos.y, maxY));
        double closestZ = Math.max(minZ, Math.min(eyePos.z, maxZ));

        double dx = eyePos.x - closestX;
        double dy = eyePos.y - closestY;
        double dz = eyePos.z - closestZ;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
