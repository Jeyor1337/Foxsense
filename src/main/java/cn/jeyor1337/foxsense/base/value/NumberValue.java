package cn.jeyor1337.foxsense.base.value;

import java.util.function.Supplier;

public class NumberValue extends Value<Number> {
    protected Number minimum;
    protected Number maximum;
    protected Number increment;

    public NumberValue(String name, Number value, Number minimum, Number maximum, Number increment,
            String description, Supplier<Boolean> visibleSupplier) {
        super(name, value, description, visibleSupplier);
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    public NumberValue(String name, Number value, Number minimum, Number maximum, Number increment,
            String description, boolean visible) {
        super(name, value, description, visible);
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    public NumberValue(String name, Number value, Number minimum, Number maximum, Number increment,
            String description) {
        super(name, value, description);
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    public NumberValue(String name, Number value, Number minimum, Number maximum, Number increment) {
        this(name, value, minimum, maximum, increment, "");
    }

    public NumberValue(String name, Number value, Number minimum, Number maximum, Number increment, boolean visible) {
        this(name, value, minimum, maximum, increment, "", visible);
    }

    public NumberValue(String name, Number value, Number minimum, Number maximum, Number increment,
            Supplier<Boolean> visibleSupplier) {
        this(name, value, minimum, maximum, increment, "", visibleSupplier);
    }

    public NumberValue(String name, Number value, Number minimum, Number maximum, String description) {
        this(name, value, minimum, maximum, 0.1, description);
    }

    public NumberValue(String name, Number value, Number minimum, Number maximum) {
        this(name, value, minimum, maximum, 0.1, "");
    }

    public NumberValue(String name, Number value, Number minimum, Number maximum, boolean visible) {
        this(name, value, minimum, maximum, 0.1, "", visible);
    }

    public NumberValue(String name, Number value) {
        this(name, value, Double.MIN_VALUE, Double.MAX_VALUE, 0.1, "");
    }

    public NumberValue(String name, Number value, boolean visible) {
        this(name, value, Double.MIN_VALUE, Double.MAX_VALUE, 0.1, "", visible);
    }

    public Number getMinimum() {
        return minimum;
    }

    public void setMinimum(Number minimum) {
        this.minimum = minimum;
    }

    public Number getMaximum() {
        return maximum;
    }

    public void setMaximum(Number maximum) {
        this.maximum = maximum;
    }

    public Number getIncrement() {
        return increment;
    }

    public void setIncrement(Number increment) {
        this.increment = increment;
    }

    @Override
    public void setValue(Number value) {
        if (minimum != null && value.doubleValue() < minimum.doubleValue()) {
            value = minimum;
        }
        if (maximum != null && value.doubleValue() > maximum.doubleValue()) {
            value = maximum;
        }
        super.setValue(value);
    }

    public void increase() {
        setValue(value.doubleValue() + increment.doubleValue());
    }

    public void decrease() {
        setValue(value.doubleValue() - increment.doubleValue());
    }

    @Override
    public String getDisplayValue() {
        return String.format("%.2f", value.doubleValue());
    }
}
