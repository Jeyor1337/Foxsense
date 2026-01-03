package cn.jeyor1337.foxsense.base.gui.component.value;

import cn.jeyor1337.foxsense.base.gui.util.AnimationUtil;
import cn.jeyor1337.foxsense.base.gui.util.RenderUtil;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import net.minecraft.client.gui.DrawContext;

public class BooleanComponent extends ValueComponent {
    private final BooleanValue booleanValue;
    private double toggleAnimation = 0.0;
    private double hoverAnimation = 0.0;

    public BooleanComponent(BooleanValue value, double x, double y, double width, double height) {
        super(value, x, y, width, height);
        this.booleanValue = value;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        pendingTooltip = null;

        boolean hovered = RenderUtil.isHovered(mouseX, mouseY, x, y, width, height);

        hoverAnimation = AnimationUtil.animate(hoverAnimation, hovered ? 1.0 : 0.0, 0.1);
        toggleAnimation = AnimationUtil.animate(toggleAnimation, booleanValue.getValue() ? 1.0 : 0.0, 0.1);

        int backgroundColor = 0xFF232323;
        int hoverColor = AnimationUtil.animateColor(backgroundColor, 0xFF333333, hoverAnimation);
        RenderUtil.drawRect(context, x, y, width, height, hoverColor);

        double checkSize = 8;
        double checkX = x + width - checkSize - 2;
        double checkY = y + height / 2 - checkSize / 2;

        RenderUtil.drawRect(context, checkX, checkY, checkSize, checkSize, 0xFF1A1A1A);

        int checkColor = AnimationUtil.animateColor(0xFF1A1A1A, 0xFF1E90FF, toggleAnimation);
        RenderUtil.drawRect(context, checkX + 1, checkY + 1, checkSize - 2, checkSize - 2, checkColor);

        String name = booleanValue.getName();
        int maxTextWidth = (int) (width - checkSize - 10);
        String displayName = RenderUtil.trimTextToWidth(mc.textRenderer, name, maxTextWidth);

        context.drawText(mc.textRenderer, displayName, (int) (x + 4),
                (int) (y + height / 2 - mc.textRenderer.fontHeight / 2f), 0xFFFFFFFF, true);

        if (hovered && !name.equals(displayName)) {
            pendingTooltip = name;
            tooltipMouseX = mouseX;
            tooltipMouseY = mouseY;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            booleanValue.toggle();
        }
    }
}
