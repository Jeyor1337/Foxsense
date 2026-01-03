package cn.jeyor1337.foxsense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.event.EventChat;
import net.minecraft.client.gui.screen.ChatScreen;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        EventChat event = new EventChat(chatText);
        Foxsense.getEventManager().call(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
