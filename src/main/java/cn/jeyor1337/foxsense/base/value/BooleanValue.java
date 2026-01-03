package cn.jeyor1337.foxsense.base.value;

import java.util.function.Supplier;

public class BooleanValue extends Value<Boolean> {

    public BooleanValue(String name, Boolean value, String description, Supplier<Boolean> visibleSupplier) {
        super(name, value, description, visibleSupplier);
    }

    public BooleanValue(String name, Boolean value, String description, boolean visible) {
        super(name, value, description, visible);
    }

    public BooleanValue(String name, Boolean value, String description) {
        super(name, value, description);
    }

    public BooleanValue(String name, Boolean value) {
        super(name, value);
    }

    public BooleanValue(String name, Boolean value, boolean visible) {
        super(name, value, "", visible);
    }

    public BooleanValue(String name, Boolean value, Supplier<Boolean> visibleSupplier) {
        super(name, value, visibleSupplier);
    }

    public boolean isEnabled() {
        return value;
    }

    public void toggle() {
        setValue(!value);
    }

    @Override
    public String getDisplayValue() {
        return value ? "ON" : "OFF";
    }
}
