package cn.jeyor1337.foxsense.base.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.module.impl.combat.AimAssist;
import cn.jeyor1337.foxsense.base.module.impl.combat.AntiMiss;
import cn.jeyor1337.foxsense.base.module.impl.combat.AutoMace;
import cn.jeyor1337.foxsense.base.module.impl.combat.STap;
import cn.jeyor1337.foxsense.base.module.impl.combat.StunCob;
import cn.jeyor1337.foxsense.base.module.impl.combat.TriggerBot;
import cn.jeyor1337.foxsense.base.module.impl.combat.WTap;
import cn.jeyor1337.foxsense.base.module.impl.misc.PearlCatch;
import cn.jeyor1337.foxsense.base.module.impl.misc.TestModule;
import cn.jeyor1337.foxsense.base.module.impl.movement.Sprint;
import cn.jeyor1337.foxsense.base.module.impl.render.ClickGui;
import cn.jeyor1337.foxsense.base.module.impl.render.HUD;

public class ModuleManager {
    private final List<Module> modules;
    private final Map<ModuleType, List<Module>> modulesByType;

    public ModuleManager() {
        this.modules = new ArrayList<>();
        this.modulesByType = new HashMap<>();
        init();
    }

    private void init() {
        for (ModuleType type : ModuleType.values()) {
            modulesByType.put(type, new ArrayList<>());
        }

        this.registerModule(new TestModule());
        this.registerModule(new ClickGui());
        this.registerModule(new AimAssist());
        this.registerModule(new AntiMiss());
        this.registerModule(new TriggerBot());
        this.registerModule(new WTap());
        this.registerModule(new STap());
        this.registerModule(new Sprint());
        this.registerModule(new HUD());
        this.registerModule(new AutoMace());
        this.registerModule(new StunCob());
        this.registerModule(new PearlCatch());

        modules.sort((m1, m2) -> m1.getName().compareToIgnoreCase(m2.getName()));
        modulesByType.values().forEach(list -> list.sort((m1, m2) -> m1.getName().compareToIgnoreCase(m2.getName())));
    }

    public void registerModule(Module module) {
        modules.add(module);
        modulesByType.get(module.getType()).add(module);
    }

    public void unregisterModule(Module module) {
        modules.remove(module);
        modulesByType.get(module.getType()).remove(module);
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public Module getModule(Class<? extends Module> clazz) {
        for (Module module : modules) {
            if (module.getClass().equals(clazz)) {
                return module;
            }
        }
        return null;
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByType(ModuleType type) {
        return modulesByType.get(type);
    }

    public List<Module> getEnabledModules() {
        List<Module> enabled = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                enabled.add(module);
            }
        }
        return enabled;
    }

    public void handleKeybind(int key) {
        for (Module module : modules) {
            if (module.getKeybind() == key) {
                module.toggle();
            }
        }
    }
}
