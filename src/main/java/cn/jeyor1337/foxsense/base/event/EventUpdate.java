package cn.jeyor1337.foxsense.base.event;

import com.cubk.event.impl.Event;

public class EventUpdate implements Event {
    private final Stage stage;

    public EventUpdate(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isPre() {
        return stage == Stage.PRE;
    }

    public boolean isPost() {
        return stage == Stage.POST;
    }

    public enum Stage {
        PRE, POST
    }
}
