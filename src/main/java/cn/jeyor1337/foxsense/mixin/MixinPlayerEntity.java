package cn.jeyor1337.foxsense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.module.impl.world.SpeedMine;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {
    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        if (Foxsense.getModuleManager() == null)
            return;

        SpeedMine optionalModule = (SpeedMine) Foxsense.getModuleManager().getModule(SpeedMine.class);

        if (optionalModule == null)
            return;

        if (!optionalModule.isEnabled())
            return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player != MinecraftClient.getInstance().player)
            return;

        float modifiedSpeed = cir.getReturnValue() * optionalModule.getSpeed();
        cir.setReturnValue(modifiedSpeed);
    }
}