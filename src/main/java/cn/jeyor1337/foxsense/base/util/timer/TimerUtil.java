package cn.jeyor1337.foxsense.base.util.timer;

public class TimerUtil {
    private long lastTime;

    public TimerUtil() {
        this.lastTime = System.currentTimeMillis();
    }

    public void reset() {
        this.lastTime = System.currentTimeMillis();
    }

    public boolean hasElapsedTime(long time, boolean reset) {
        if (System.currentTimeMillis() - lastTime >= time) {
            if (reset) {
                reset();
            }
            return true;
        }
        return false;
    }

    public boolean hasElapsedTime(long time) {
        return hasElapsedTime(time, false);
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - lastTime;
    }
}
