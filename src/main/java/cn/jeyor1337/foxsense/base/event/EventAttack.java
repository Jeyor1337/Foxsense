package cn.jeyor1337.foxsense.base.event;

import com.cubk.event.impl.CancellableEvent;

import net.minecraft.entity.Entity;

public class EventAttack extends CancellableEvent {
    private final Entity targetEntity;

    public EventAttack(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }
}
