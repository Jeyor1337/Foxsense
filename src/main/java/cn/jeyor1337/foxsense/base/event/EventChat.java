package cn.jeyor1337.foxsense.base.event;

import com.cubk.event.impl.CancellableEvent;

public class EventChat extends CancellableEvent {
    private String message;

    public EventChat(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
