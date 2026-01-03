package cn.jeyor1337.foxsense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.event.EventKey;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        EventKey event = new EventKey(input.key(), input.scancode(), action, input.modifiers());
        Foxsense.getEventManager().call(event);
    }
}
