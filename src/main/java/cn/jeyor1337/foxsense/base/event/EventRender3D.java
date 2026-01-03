package cn.jeyor1337.foxsense.base.event;

import com.cubk.event.impl.Event;

import net.minecraft.client.util.math.MatrixStack;

public class EventRender3D implements Event {
    private final MatrixStack matrices;
    private final float tickDelta;

    public EventRender3D(MatrixStack matrices, float tickDelta) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
