package cn.jeyor1337.foxsense.base.gui.util;

public class AnimationUtil {
    public static double animate(double current, double target, double speed) {
        if (Math.abs(current - target) < 0.001) {
            return target;
        }
        return current + (target - current) * speed;
    }

    public static int animateColor(int current, int target, double speed) {
        int currentA = (current >> 24) & 0xFF;
        int currentR = (current >> 16) & 0xFF;
        int currentG = (current >> 8) & 0xFF;
        int currentB = current & 0xFF;

        int targetA = (target >> 24) & 0xFF;
        int targetR = (target >> 16) & 0xFF;
        int targetG = (target >> 8) & 0xFF;
        int targetB = target & 0xFF;

        int newA = (int) animate(currentA, targetA, speed);
        int newR = (int) animate(currentR, targetR, speed);
        int newG = (int) animate(currentG, targetG, speed);
        int newB = (int) animate(currentB, targetB, speed);

        return (newA << 24) | (newR << 16) | (newG << 8) | newB;
    }
}
