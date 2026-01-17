package cn.jeyor1337.foxsense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.event.EventPacket;
import cn.jeyor1337.foxsense.base.util.PacketUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (PacketUtils.isIgnored(packet)) {
            return;
        }

        EventPacket event = new EventPacket(EventPacket.Type.SEND, packet);
        Foxsense.getEventManager().call(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (PacketUtils.isIgnored(packet)) {
            return;
        }

        EventPacket event = new EventPacket(EventPacket.Type.RECEIVE, packet);
        Foxsense.getEventManager().call(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
