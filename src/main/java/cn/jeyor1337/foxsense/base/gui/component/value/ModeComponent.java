package cn.jeyor1337.foxsense.base.gui.component.value;

import cn.jeyor1337.foxsense.base.gui.util.AnimationUtil;
import cn.jeyor1337.foxsense.base.gui.util.RenderUtil;
import cn.jeyor1337.foxsense.base.value.ModeValue;
import net.minecraft.client.gui.DrawContext;

public class ModeComponent extends ValueComponent {
    private final ModeValue modeValue;
    private double hoverAnimation = 0.0;

    public ModeComponent(ModeValue value, double x, double y, double width, double height) {
        super(value, x, y, width, height);
        this.modeValue = value;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        pendingTooltip = null;

        boolean hovered = RenderUtil.isHovered(mouseX, mouseY, x, y, width, height);

        hoverAnimation = AnimationUtil.animate(hovered ? 1.0 : 0.0, hoverAnimation, 0.1);

        int backgroundColor = 0xFF232323;
        int hoverColor = AnimationUtil.animateColor(backgroundColor, 0xFF333333, hoverAnimation);
        RenderUtil.drawRect(context, x, y, width, height, hoverColor);

        String name = modeValue.getName();
        String modeName = modeValue.getDisplayValue();
        int modeWidth = mc.textRenderer.getWidth(modeName);
        int maxNameWidth = (int) (width - modeWidth - 12);
        String displayName = RenderUtil.trimTextToWidth(mc.textRenderer, name, maxNameWidth);

        context.drawText(mc.textRenderer, displayName, (int) (x + 4),
                (int) (y + height / 2 - mc.textRenderer.fontHeight / 2f), 0xFFFFFFFF, true);
        context.drawText(mc.textRenderer, modeName,
                (int) (x + width - modeWidth - 4),
                (int) (y + height / 2 - mc.textRenderer.fontHeight / 2f), 0xFF1E90FF, true);

        if (hovered && !name.equals(displayName)) {
            pendingTooltip = name;
            tooltipMouseX = mouseX;
            tooltipMouseY = mouseY;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
            if (button == 0) {
                modeValue.next();
            } else if (button == 1) {
                modeValue.previous();
            }
        }
    }
}
