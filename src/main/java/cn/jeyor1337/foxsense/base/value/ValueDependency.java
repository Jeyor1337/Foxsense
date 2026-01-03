package cn.jeyor1337.foxsense.base.value;

import java.util.function.Supplier;

public class ValueDependency {
    private final Value<?> dependentValue;
    private final Supplier<Boolean> condition;

    public ValueDependency(Value<?> dependentValue, Supplier<Boolean> condition) {
        this.dependentValue = dependentValue;
        this.condition = condition;
    }

    public Value<?> getDependentValue() {
        return dependentValue;
    }

    public boolean isVisible() {
        return condition.get();
    }
}
