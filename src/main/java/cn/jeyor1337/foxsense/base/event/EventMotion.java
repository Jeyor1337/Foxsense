package cn.jeyor1337.foxsense.base.event;

import com.cubk.event.impl.CancellableEvent;

public class EventMotion extends CancellableEvent {
    private final Stage stage;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public EventMotion(Stage stage, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.stage = stage;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public enum Stage {
        PRE, POST
    }
}
