package cn.jeyor1337.foxsense.base.event;

import com.cubk.event.impl.CancellableEvent;

import net.minecraft.network.packet.Packet;

public class EventPacket extends CancellableEvent {
    private final Type type;
    private Packet<?> packet;

    public EventPacket(Type type, Packet<?> packet) {
        this.type = type;
        this.packet = packet;
    }

    public Type getType() {
        return type;
    }

    public boolean isSend() {
        return type == Type.SEND;
    }

    public boolean isReceive() {
        return type == Type.RECEIVE;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public enum Type {
        SEND, RECEIVE
    }
}
