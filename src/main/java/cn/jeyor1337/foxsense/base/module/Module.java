package cn.jeyor1337.foxsense.base.module;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import cn.jeyor1337.foxsense.base.value.Value;
import net.minecraft.client.MinecraftClient;

public abstract class Module {
    protected final String name;
    protected final String description;
    protected final ModuleType type;
    protected int keybind;
    protected boolean enabled;
    protected List<Value<?>> values;
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public Module(String name, String description, ModuleType type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.keybind = GLFW.GLFW_KEY_UNKNOWN;
        this.enabled = false;
        this.values = new ArrayList<>();
    }

    public Module(String name, ModuleType type) {
        this(name, "", type);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModuleType getType() {
        return type;
    }

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled)
            return;

        this.enabled = enabled;

        if (enabled) {
            onEnable();
            cn.jeyor1337.foxsense.Foxsense.getEventManager().register(this);
        } else {
            cn.jeyor1337.foxsense.Foxsense.getEventManager().unregister(this);
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public List<Value<?>> getValues() {
        return values;
    }

    public void addValue(Value<?> value) {
        values.add(value);
    }

    public void addValues(Value<?>... values) {
        for (Value<?> value : values) {
            addValue(value);
        }
    }

    public Value<?> getValue(String name) {
        for (Value<?> value : values) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Value<?>> T getValue(Class<T> type) {
        for (Value<?> value : values) {
            if (type.isInstance(value)) {
                return (T) value;
            }
        }
        return null;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void reset() {
        setEnabled(false);
        for (Value<?> value : values) {
            value.reset();
        }
    }

    public boolean isNull() {
        return mc.player == null || mc.world == null;
    }
}
