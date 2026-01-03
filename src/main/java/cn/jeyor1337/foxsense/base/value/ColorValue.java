package cn.jeyor1337.foxsense.base.value;

import java.util.function.Supplier;

public class ColorValue extends Value<Integer> {
    protected final boolean rainbow;

    public ColorValue(String name, int value, boolean rainbow, String description, Supplier<Boolean> visibleSupplier) {
        super(name, value, description, visibleSupplier);
        this.rainbow = rainbow;
    }

    public ColorValue(String name, int value, boolean rainbow, String description, boolean visible) {
        super(name, value, description, visible);
        this.rainbow = rainbow;
    }

    public ColorValue(String name, int value, boolean rainbow, String description) {
        super(name, value, description);
        this.rainbow = rainbow;
    }

    public ColorValue(String name, int value, boolean rainbow) {
        this(name, value, rainbow, "");
    }

    public ColorValue(String name, int value, String description) {
        this(name, value, false, description);
    }

    public ColorValue(String name, int value) {
        this(name, value, false, "");
    }

    public ColorValue(String name, int value, boolean rainbow, Supplier<Boolean> visibleSupplier) {
        this(name, value, rainbow, "", visibleSupplier);
    }

    public ColorValue(String name, int value, Supplier<Boolean> visibleSupplier) {
        this(name, value, false, "", visibleSupplier);
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public int getRed() {
        return (value >> 16) & 0xFF;
    }

    public int getGreen() {
        return (value >> 8) & 0xFF;
    }

    public int getBlue() {
        return value & 0xFF;
    }

    public int getAlpha() {
        return (value >> 24) & 0xFF;
    }

    public void setRed(int red) {
        value = ((red & 0xFF) << 16) | (getGreen() << 8) | getBlue() | (getAlpha() << 24);
    }

    public void setGreen(int green) {
        value = (getRed() << 16) | ((green & 0xFF) << 8) | getBlue() | (getAlpha() << 24);
    }

    public void setBlue(int blue) {
        value = (getRed() << 16) | (getGreen() << 8) | (blue & 0xFF) | (getAlpha() << 24);
    }

    public void setAlpha(int alpha) {
        value = (getRed() << 16) | (getGreen() << 8) | getBlue() | ((alpha & 0xFF) << 24);
    }

    public void setRGB(int red, int green, int blue) {
        value = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF) | (getAlpha() << 24);
    }

    public void setRGBA(int red, int green, int blue, int alpha) {
        value = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF) | ((alpha & 0xFF) << 24);
    }

    public static int fromRGBA(int red, int green, int blue, int alpha) {
        return ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF) | ((alpha & 0xFF) << 24);
    }

    public static int fromRGB(int red, int green, int blue) {
        return fromRGBA(red, green, blue, 255);
    }

    @Override
    public String getDisplayValue() {
        return String.format("#%08X", value);
    }
}
