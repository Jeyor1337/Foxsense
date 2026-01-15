package cn.jeyor1337.foxsense.base.module.impl.world;

import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.value.NumberValue;

public class SpeedMine extends Module {
    private final NumberValue speed = new NumberValue("Speed", 5.0f, 1.0f, 10.0f, 0.5f);

    public SpeedMine() {
        super("SpeedMine", ModuleType.WORLD);
        this.addValues(speed);
    }

    public Float getSpeed() {
        return speed.getValue().floatValue();
    }

}
