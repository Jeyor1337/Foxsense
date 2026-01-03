package cn.jeyor1337.foxsense.base.event;

import com.cubk.event.impl.Event;

public class EventMouse implements Event {
    private final long window;
    private final int keyCode;
    private final int action;

    public EventMouse(long window, int keyCode, int action) {
        this.window = window;
        this.keyCode = keyCode;
        this.action = action;
    }

    public long getWindow() {
        return window;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getAction() {
        return action;
    }
}
