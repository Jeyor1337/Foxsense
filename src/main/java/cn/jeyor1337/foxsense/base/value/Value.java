package cn.jeyor1337.foxsense.base.value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class Value<T> {
    protected T value;
    protected T defaultValue;
    protected final String name;
    protected final String description;
    protected List<ValueListener<T>> listeners;
    protected Supplier<Boolean> visibleSupplier;

    public Value(String name, T value, String description, Supplier<Boolean> visibleSupplier) {
        this.name = name;
        this.value = value;
        this.defaultValue = value;
        this.description = description;
        this.listeners = new ArrayList<>();
        this.visibleSupplier = visibleSupplier;
    }

    public Value(String name, T value, String description, boolean visible) {
        this(name, value, description, () -> visible);
    }

    public Value(String name, T value, String description) {
        this(name, value, description, () -> true);
    }

    public Value(String name, T value) {
        this(name, value, "", () -> true);
    }

    public Value(String name, T value, boolean visible) {
        this(name, value, "", () -> visible);
    }

    public Value(String name, T value, Supplier<Boolean> visibleSupplier) {
        this(name, value, "", visibleSupplier);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        T oldValue = this.value;
        this.value = value;
        notifyListeners(oldValue, value);
    }

    public boolean isVisible() {
        return visibleSupplier.get();
    }

    public void setVisible(boolean visible) {
        this.visibleSupplier = () -> visible;
    }

    public void setVisibleSupplier(Supplier<Boolean> visibleSupplier) {
        this.visibleSupplier = visibleSupplier;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addListener(ValueListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ValueListener<T> listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(T oldValue, T newValue) {
        for (ValueListener<T> listener : listeners) {
            listener.onValueChanged(oldValue, newValue);
        }
    }

    public void reset() {
        setValue(defaultValue);
    }

    public String getDisplayValue() {
        return String.valueOf(value);
    }

    public interface ValueListener<T> {
        void onValueChanged(T oldValue, T newValue);
    }
}
