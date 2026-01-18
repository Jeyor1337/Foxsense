package cn.jeyor1337.foxsense.base.ml;

public class AimOutput {

    public float deltaYaw;
    public float deltaPitch;

    public AimOutput() {
    }

    public AimOutput(float deltaYaw, float deltaPitch) {
        this.deltaYaw = deltaYaw;
        this.deltaPitch = deltaPitch;
    }

    public static AimOutput fromArray(float[] array) {
        if (array.length < 2) {
            throw new IllegalArgumentException("Array must have at least 2 elements");
        }
        return new AimOutput(array[0], array[1]);
    }

    public float[] toArray() {
        return new float[] { deltaYaw, deltaPitch };
    }

    @Override
    public String toString() {
        return String.format("AimOutput[deltaYaw=%.4f, deltaPitch=%.4f]", deltaYaw, deltaPitch);
    }
}
