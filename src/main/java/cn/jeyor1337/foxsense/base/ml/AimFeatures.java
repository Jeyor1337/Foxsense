package cn.jeyor1337.foxsense.base.ml;

public class AimFeatures {

    public float deltaYaw;
    public float deltaPitch;
    public float deltaX;
    public float deltaY;
    public float deltaZ;
    public float distance;

    public AimFeatures() {
    }

    public AimFeatures(float deltaYaw, float deltaPitch, float deltaX, float deltaY, float deltaZ, float distance) {
        this.deltaYaw = deltaYaw;
        this.deltaPitch = deltaPitch;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.distance = distance;
    }

    public float[] toArray() {
        return new float[] {
                deltaYaw, deltaPitch, deltaX, deltaY, deltaZ, distance
        };
    }

    public static AimFeatures fromArray(float[] array) {
        if (array.length < 6) {
            throw new IllegalArgumentException("Array must have at least 6 elements");
        }
        return new AimFeatures(
                array[0], array[1], array[2], array[3], array[4], array[5]);
    }

    public static int getFeatureCount() {
        return 6;
    }
}
