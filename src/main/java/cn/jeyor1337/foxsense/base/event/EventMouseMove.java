package cn.jeyor1337.foxsense.base.event;

import com.cubk.event.impl.CancellableEvent;

public class EventMouseMove extends CancellableEvent {
    private double deltaX;
    private double deltaY;

    public EventMouseMove(double deltaX, double deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(double deltaX) {
        this.deltaX = deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }
}
