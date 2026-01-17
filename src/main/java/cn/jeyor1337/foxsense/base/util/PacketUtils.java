package cn.jeyor1337.foxsense.base.util;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

public class PacketUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Set<Packet<?>> ignoredPackets = Collections.newSetFromMap(new WeakHashMap<>());

    public static void sendPacketNoEvent(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        ignoredPackets.add(packet);
        mc.getNetworkHandler().sendPacket(packet);
    }

    public static boolean isIgnored(Packet<?> packet) {
        return ignoredPackets.remove(packet);
    }

    public static void addIgnored(Packet<?> packet) {
        ignoredPackets.add(packet);
    }
}
