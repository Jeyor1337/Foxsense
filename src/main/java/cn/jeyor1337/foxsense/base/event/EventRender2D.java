package cn.jeyor1337.foxsense.base.event;

import com.cubk.event.impl.Event;

import net.minecraft.client.gui.DrawContext;

public class EventRender2D implements Event {
    private final DrawContext context;
    private final float tickDelta;

    public EventRender2D(DrawContext context, float tickDelta) {
        this.context = context;
        this.tickDelta = tickDelta;
    }

    public DrawContext getContext() {
        return context;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
