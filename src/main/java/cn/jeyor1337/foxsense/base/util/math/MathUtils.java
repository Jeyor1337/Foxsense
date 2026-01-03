package cn.jeyor1337.foxsense.base.util.math;

import java.util.Random;

public class MathUtils {
    private static final Random random = new Random();

    public static double randomDoubleBetween(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    public static float randomFloatBetween(float min, float max) {
        return min + (max - min) * random.nextFloat();
    }

    public static int randomIntBetween(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
