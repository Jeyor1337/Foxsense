package cn.jeyor1337.foxsense.base.module.impl.player;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.timer.TimerUtil;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.mixin.PlayerInventoryAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {
    private static final NumberValue delay = new NumberValue("Delay", 5, 0, 100, 1);
    private static final BooleanValue returnToPrevious = new BooleanValue("Return To Previous", true);
    private static final BooleanValue onlyWhenSneaking = new BooleanValue("Only When Sneaking", false);
    private static final BooleanValue preventLowDurability = new BooleanValue("Prevent Low Durability", true);
    private static final NumberValue durabilityThreshold = new NumberValue("Durability Threshold", 10, 1, 100, 1);

    private final TimerUtil timer = new TimerUtil();
    private int previousSlot = -1;

    public AutoTool() {
        super("AutoTool", "Automatically switches to the best tool", ModuleType.PLAYER);
        this.addValues(delay, returnToPrevious, onlyWhenSneaking, preventLowDurability, durabilityThreshold);
    }

    @EventTarget
    private void onTickEvent(EventTick event) {
        if (isNull())
            return;

        if (onlyWhenSneaking.getValue() && !mc.player.isSneaking())
            return;

        if (!mc.options.attackKey.isPressed()) {
            if (returnToPrevious.getValue() && previousSlot != -1) {
                ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(previousSlot);
                previousSlot = -1;
            }
            return;
        }

        HitResult hit = mc.crosshairTarget;
        if (hit == null)
            return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            handleMining((BlockHitResult) hit);
        }
    }

    private void handleMining(BlockHitResult hit) {
        BlockState state = mc.world.getBlockState(hit.getBlockPos());
        if (state.getHardness(mc.world, hit.getBlockPos()) < 0)
            return;

        int bestSlot = findBestTool(state);
        if (bestSlot != -1 && bestSlot != ((PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot()) {
            switchTool(bestSlot);
        }
    }

    private void switchTool(int slot) {
        if (!timer.hasElapsedTime(delay.getValue().intValue()))
            return;

        if (previousSlot == -1) {
            previousSlot = ((PlayerInventoryAccessor) mc.player.getInventory()).getSelectedSlot();
        }
        ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(slot);
        timer.reset();
    }

    private int findBestTool(BlockState state) {
        int bestSlot = -1;
        float bestScore = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !isEffectiveTool(stack, state))
                continue;
            if (preventLowDurability.getValue() && hasLowDurability(stack))
                continue;

            float score = calculateToolScore(stack, state);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private boolean isEffectiveTool(ItemStack tool, BlockState state) {
        return tool.isSuitableFor(state) || tool.getMiningSpeedMultiplier(state) > 1.0f;
    }

    private float calculateToolScore(ItemStack tool, BlockState state) {
        float baseScore = tool.getMiningSpeedMultiplier(state);
        float materialBonus = getMaterialBonus(tool);
        return baseScore * materialBonus;
    }

    private float getMaterialBonus(ItemStack tool) {
        String name = tool.getItem().toString().toLowerCase();
        if (name.contains("netherite"))
            return 6.0f;
        if (name.contains("diamond"))
            return 5.0f;
        if (name.contains("iron"))
            return 4.0f;
        if (name.contains("golden"))
            return 3.5f;
        if (name.contains("stone"))
            return 2.0f;
        if (name.contains("wooden"))
            return 1.5f;
        return 1.0f;
    }

    private boolean hasLowDurability(ItemStack stack) {
        if (stack.getMaxDamage() <= 0)
            return false;
        int remaining = stack.getMaxDamage() - stack.getDamage();
        return remaining <= durabilityThreshold.getValue().intValue();
    }

    @Override
    protected void onEnable() {
        timer.reset();
        previousSlot = -1;
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        if (returnToPrevious.getValue() && previousSlot != -1) {
            ((PlayerInventoryAccessor) mc.player.getInventory()).setSelectedSlot(previousSlot);
        }
        previousSlot = -1;
        super.onDisable();
    }
}
