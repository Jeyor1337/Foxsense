package cn.jeyor1337.foxsense.base.module.impl.render;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cubk.event.annotations.EventTarget;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.event.EventRender2D;
import cn.jeyor1337.foxsense.base.gui.util.AnimationUtil;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import net.minecraft.client.gui.DrawContext;

public class HUD extends Module {
    private final Map<Module, Double> animationMap = new HashMap<>();
    private static final double FADE_IN_SPEED = 0.15;
    private static final double FADE_OUT_SPEED = 0.25;

    public HUD() {
        super("HUD", "Displays client name and module list", ModuleType.RENDER);
        this.setEnabled(true);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (isNull())
            return;

        DrawContext context = event.getContext();

        renderClientName(context);
        renderArrayList(context);
    }

    private void renderClientName(DrawContext context) {
        String clientName = Foxsense.NAME;
        context.drawText(mc.textRenderer, clientName, 5, 5, 0xFFFFFFFF, true);
    }

    private void renderArrayList(DrawContext context) {
        for (Module module : Foxsense.getModuleManager().getModules()) {
            if (!animationMap.containsKey(module)) {
                animationMap.put(module, 0.0);
            }
        }

        for (Module module : Foxsense.getModuleManager().getModules()) {
            if (module == this)
                continue;

            double currentOffset = animationMap.get(module);
            double targetOffset = module.isEnabled() ? 1.0 : 0.0;
            double speed = targetOffset > currentOffset ? FADE_IN_SPEED : FADE_OUT_SPEED;
            double animatedOffset = AnimationUtil.animate(currentOffset, targetOffset, speed);
            animationMap.put(module, animatedOffset);
        }

        List<Module> modulesToRender = animationMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.05 && entry.getKey() != this)
                .map(Map.Entry::getKey)
                .sorted(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())))
                .collect(Collectors.toList());

        int screenWidth = mc.getWindow().getScaledWidth();
        int y = 2;

        for (Module module : modulesToRender) {
            String name = module.getName();
            int textWidth = mc.textRenderer.getWidth(name);
            double animatedOffset = animationMap.get(module);

            int x = (int) (screenWidth - textWidth - 5 + (1.0 - animatedOffset) * (textWidth + 10));

            context.drawText(mc.textRenderer, name, x, y, 0xFFFFFFFF, true);
            y += 10;
        }
    }
}
