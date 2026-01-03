package cn.jeyor1337.foxsense.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.event.EventMotion;
import cn.jeyor1337.foxsense.base.event.EventUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow
    private double lastXClient;

    @Shadow
    private double lastYClient;

    @Shadow
    private double lastZClient;

    @Shadow
    private float lastYawClient;

    @Shadow
    private float lastPitchClient;

    @Shadow
    private boolean lastOnGround;

    @Shadow
    private boolean lastHorizontalCollision;

    @Shadow
    private int ticksSinceLastPositionPacketSent;

    @Shadow
    private boolean autoJumpEnabled;

    @Shadow
    public abstract boolean isCamera();

    @Shadow
    private void sendSprintingPacket() {
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        EventUpdate event = new EventUpdate(EventUpdate.Stage.PRE);
        Foxsense.getEventManager().call(event);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickPost(CallbackInfo ci) {
        EventUpdate event = new EventUpdate(EventUpdate.Stage.POST);
        Foxsense.getEventManager().call(event);
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void onSendMovementPackets(CallbackInfo ci) {
        this.sendSprintingPacket();
        if (this.isCamera()) {
            ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
            double d = self.getX() - this.lastXClient;
            double e = self.getY() - this.lastYClient;
            double f = self.getZ() - this.lastZClient;
            double g = (double) (self.getYaw() - this.lastYawClient);
            double h = (double) (self.getPitch() - this.lastPitchClient);
            ++this.ticksSinceLastPositionPacketSent;
            boolean bl = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4)
                    || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl2 = g != 0.0 || h != 0.0;

            EventMotion eventPre = new EventMotion(
                    EventMotion.Stage.PRE,
                    self.getX(),
                    self.getY(),
                    self.getZ(),
                    self.getYaw(),
                    self.getPitch(),
                    self.isOnGround());
            Foxsense.getEventManager().call(eventPre);

            if (!eventPre.isCancelled()) {
                float yaw = eventPre.getYaw();
                float pitch = eventPre.getPitch();
                boolean onGround = eventPre.isOnGround();

                if (bl && bl2) {
                    this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                            self.getX(), self.getY(), self.getZ(),
                            yaw, pitch, onGround, self.horizontalCollision));
                } else if (bl) {
                    this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            self.getX(), self.getY(), self.getZ(),
                            onGround, self.horizontalCollision));
                } else if (bl2) {
                    this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                            yaw, pitch, onGround, self.horizontalCollision));
                } else if (this.lastOnGround != onGround || this.lastHorizontalCollision != self.horizontalCollision) {
                    this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(
                            onGround, self.horizontalCollision));
                }

                if (bl) {
                    this.lastXClient = self.getX();
                    this.lastYClient = self.getY();
                    this.lastZClient = self.getZ();
                    this.ticksSinceLastPositionPacketSent = 0;
                }

                if (bl2) {
                    this.lastYawClient = yaw;
                    this.lastPitchClient = pitch;
                }

                this.lastOnGround = onGround;
                this.lastHorizontalCollision = self.horizontalCollision;
            }

            EventMotion eventPost = new EventMotion(
                    EventMotion.Stage.POST,
                    self.getX(),
                    self.getY(),
                    self.getZ(),
                    self.getYaw(),
                    self.getPitch(),
                    self.isOnGround());
            Foxsense.getEventManager().call(eventPost);

            this.autoJumpEnabled = (Boolean) this.client.options.getAutoJump().getValue();
        }
        ci.cancel();
    }
}
