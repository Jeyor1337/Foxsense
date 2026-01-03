package cn.jeyor1337.foxsense.base.value;

import java.util.Arrays;
import java.util.function.Supplier;

public class ModeValue extends Value<String> {
    protected final String[] modes;

    public ModeValue(String name, String[] modes, String defaultValue, String description,
            Supplier<Boolean> visibleSupplier) {
        super(name, defaultValue, description, visibleSupplier);
        this.modes = modes;
    }

    public ModeValue(String name, String[] modes, String defaultValue, String description, boolean visible) {
        super(name, defaultValue, description, visible);
        this.modes = modes;
    }

    public ModeValue(String name, String[] modes, String defaultValue, String description) {
        this(name, modes, defaultValue, description, true);
    }

    public ModeValue(String name, String[] modes, String defaultValue) {
        this(name, modes, defaultValue, "", true);
    }

    public ModeValue(String name, String[] modes, String defaultValue, boolean visible) {
        this(name, modes, defaultValue, "", visible);
    }

    public ModeValue(String name, String[] modes, String defaultValue, Supplier<Boolean> visibleSupplier) {
        this(name, modes, defaultValue, "", visibleSupplier);
    }

    public String[] getModes() {
        return modes;
    }

    @Override
    public void setValue(String value) {
        if (Arrays.stream(modes).anyMatch(v -> v.equals(value))) {
            super.setValue(value);
        }
    }

    public void setValue(int index) {
        if (index >= 0 && index < modes.length) {
            super.setValue(modes[index]);
        }
    }

    public int getIndex() {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    public void next() {
        int newIndex = getIndex() + 1;
        if (newIndex >= modes.length) {
            newIndex = 0;
        }
        setValue(newIndex);
    }

    public void previous() {
        int newIndex = getIndex() - 1;
        if (newIndex < 0) {
            newIndex = modes.length - 1;
        }
        setValue(newIndex);
    }

    @Override
    public String getDisplayValue() {
        return value;
    }
}
