package cn.jeyor1337.foxsense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.event.EventMouse;
import cn.jeyor1337.foxsense.base.event.EventMouseMove;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;

@Mixin(Mouse.class)
public class MixinMouse {
    private double storedDeltaX;
    private double storedDeltaY;
    private boolean hasStoredX = false;

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        EventMouse event = new EventMouse(window, input.getKeycode(), action);
        Foxsense.getEventManager().call(event);
    }

    @ModifyVariable(method = "updateMouse", at = @At(value = "STORE"), ordinal = 2)
    private double modifyDeltaX(double i) {
        this.storedDeltaX = i;
        this.hasStoredX = true;
        return i;
    }

    @ModifyVariable(method = "updateMouse", at = @At(value = "STORE"), ordinal = 3)
    private double modifyDeltaY(double j) {
        this.storedDeltaY = j;

        if (this.hasStoredX) {
            EventMouseMove event = new EventMouseMove(this.storedDeltaX, this.storedDeltaY);
            Foxsense.getEventManager().call(event);

            this.storedDeltaX = event.getDeltaX();
            this.storedDeltaY = event.getDeltaY();
            this.hasStoredX = false;

            return this.storedDeltaY;
        }

        return j;
    }

    @ModifyVariable(method = "updateMouse", at = @At(value = "LOAD"), ordinal = 4)
    private double loadModifiedDeltaX(double i) {
        return this.storedDeltaX;
    }
}
