package cn.jeyor1337.foxsense.base.module.impl.movement;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.util.timer.TimerUtil;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class AutoHeadHitter extends Module {
    private final NumberValue jumpDelay = new NumberValue("Jump Delay", 100, 0, 500, 10);
    private final BooleanValue holdingSpace = new BooleanValue("Holding Space", false);

    private final TimerUtil jumpTimer = new TimerUtil();

    public AutoHeadHitter() {
        super("Auto Head Hitter", "Auto jumps when there's a solid block above to make u go fast", ModuleType.MOVEMENT);
        this.addValues(jumpDelay, holdingSpace);
    }

    @EventTarget
    private void onTickEvent(EventTick event) {
        if (isNull())
            return;

        if (holdingSpace.getValue() && !mc.options.jumpKey.isPressed())
            return;

        if (jumpDelay.getValue().intValue() > 0 && !jumpTimer.hasElapsedTime(jumpDelay.getValue().intValue()))
            return;

        if (!mc.player.isOnGround())
            return;

        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos headPos = playerPos.up(2);

        BlockState blockState = mc.world.getBlockState(headPos);

        if (!blockState.isAir() && blockState.getBlock() != Blocks.WATER && blockState.getBlock() != Blocks.LAVA) {
            mc.player.jump();
            jumpTimer.reset();
        }
    }
}
