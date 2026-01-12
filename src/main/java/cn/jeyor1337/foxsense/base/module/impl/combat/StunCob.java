package cn.jeyor1337.foxsense.base.module.impl.combat;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventAttack;
import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class StunCob extends Module {

    private final NumberValue predictionTime = new NumberValue("Prediction Time", 0.5, 0.1, 2.0, 0.1);
    private final NumberValue placeDelay = new NumberValue("Place Delay", 50, 0, 200, 10);
    private final BooleanValue targetPlayers = new BooleanValue("Target Players", true);
    private final BooleanValue targetMobs = new BooleanValue("Target Mobs", false);
    private final BooleanValue requireCobweb = new BooleanValue("Require Cobweb", true);

    private Entity lastHitTarget = null;
    private long lastHitTime = 0;

    public StunCob() {
        super("StunCob", "Predicts player movement and places cobweb", ModuleType.COMBAT);
        this.addValues(predictionTime, placeDelay, targetPlayers, targetMobs, requireCobweb);
    }

    @EventTarget
    private void onAttack(EventAttack event) {
        if (isNull())
            return;

        Entity target = event.getTargetEntity();
        if (target == null || !isValidTarget(target))
            return;

        lastHitTarget = target;
        lastHitTime = System.currentTimeMillis();
    }

    @EventTarget
    private void onTick(EventTick event) {
        if (isNull())
            return;

        if (lastHitTarget == null)
            return;
        if (System.currentTimeMillis() - lastHitTime < placeDelay.getValue().longValue())
            return;

        if (requireCobweb.getValue() && !hasCobwebInInventory())
            return;

        Vec3d predictedPos = predictLandingPosition(lastHitTarget);
        if (predictedPos != null) {
            placeCobwebAtPosition(predictedPos);
        }

        lastHitTarget = null;
    }

    private Vec3d predictLandingPosition(Entity target) {
        if (!(target instanceof LivingEntity))
            return null;

        Vec3d currentPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d velocity = target.getVelocity();

        boolean isSprinting = mc.player.isSprinting();
        double knockbackMultiplier = isSprinting ? 1.5 : 1.0;
        Vec3d adjustedVelocity = new Vec3d(
                velocity.x * knockbackMultiplier,
                velocity.y,
                velocity.z * knockbackMultiplier);

        double predictionTime = this.predictionTime.getValue().doubleValue();
        double gravity = 0.08;
        double airResistance = 0.98;

        Vec3d predictedPos = currentPos;
        Vec3d predictedVel = adjustedVelocity;

        for (double t = 0; t < predictionTime; t += 0.05) {
            predictedVel = new Vec3d(
                    predictedVel.x * airResistance,
                    predictedVel.y - gravity * 0.05,
                    predictedVel.z * airResistance);
            predictedPos = predictedPos.add(predictedVel.multiply(0.05));

            if (predictedPos.y <= 0) {
                predictedPos = new Vec3d(predictedPos.x, 0, predictedPos.z);
                break;
            }
        }

        BlockPos blockPos = new BlockPos((int) Math.floor(predictedPos.x), (int) Math.floor(predictedPos.y),
                (int) Math.floor(predictedPos.z));

        if (canPlaceCobweb(blockPos)) {
            return new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        }

        return null;
    }

    private boolean canPlaceCobweb(BlockPos pos) {
        if (mc.world == null)
            return false;

        if (!mc.world.getBlockState(pos).isAir())
            return false;
        if (!mc.world.getBlockState(pos.down()).isSolidBlock(mc.world, pos.down()))
            return false;

        double distance = mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return distance <= 4.5;
    }

    private void placeCobwebAtPosition(Vec3d pos) {
        BlockPos blockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));

        if (!canPlaceCobweb(blockPos))
            return;

        int cobwebSlot = getCobwebSlot();
        if (cobwebSlot == -1)
            return;

        int originalSlot = ((PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot();
        ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(cobwebSlot);

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                new BlockHitResult(pos, Direction.UP, blockPos, false));

        ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(originalSlot);
    }

    private int getCobwebSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.COBWEB) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasCobwebInInventory() {
        return getCobwebSlot() != -1;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == null || entity == mc.player || entity == mc.getCameraEntity())
            return false;
        if (!(entity instanceof LivingEntity livingEntity))
            return false;
        if (!livingEntity.isAlive() || livingEntity.isDead())
            return false;

        if (entity instanceof PlayerEntity) {
            return targetPlayers.getValue();
        } else {
            return targetMobs.getValue();
        }
    }

    @Override
    protected void onEnable() {
        lastHitTarget = null;
        lastHitTime = 0;
    }

    @Override
    protected void onDisable() {
        lastHitTarget = null;
        lastHitTime = 0;
    }
}
