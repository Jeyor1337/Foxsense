package cn.jeyor1337.foxsense.base.gui.component.value;

import java.math.BigDecimal;
import java.math.RoundingMode;

import cn.jeyor1337.foxsense.base.gui.util.AnimationUtil;
import cn.jeyor1337.foxsense.base.gui.util.RenderUtil;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import net.minecraft.client.gui.DrawContext;

public class NumberComponent extends ValueComponent {
    private final NumberValue numberValue;
    private boolean dragging;
    private double hoverAnimation = 0.0;
    private double sliderAnimation = 0.0;

    public NumberComponent(NumberValue value, double x, double y, double width, double height) {
        super(value, x, y, width, height);
        this.numberValue = value;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        pendingTooltip = null;

        boolean hovered = RenderUtil.isHovered(mouseX, mouseY, x, y, width, height);

        hoverAnimation = AnimationUtil.animate(hovered ? 1.0 : 0.0, hoverAnimation, 0.1);

        double min = numberValue.getMinimum().doubleValue();
        double max = numberValue.getMaximum().doubleValue();
        double val = numberValue.getValue().doubleValue();
        double renderWidth = (width - 6) * (val - min) / (max - min);

        sliderAnimation = AnimationUtil.animate(renderWidth, sliderAnimation, 0.1);

        if (dragging) {
            double diff = Math.min(width - 6, Math.max(0, mouseX - (x + 3)));
            double newValue = min + (diff / (width - 6)) * (max - min);

            double inc = numberValue.getIncrement().doubleValue();
            if (inc != 0) {
                newValue = Math.round(newValue * (1.0 / inc)) / (1.0 / inc);
            }

            numberValue.setValue(newValue);
        }

        int backgroundColor = 0xFF232323;
        int hoverColor = AnimationUtil.animateColor(backgroundColor, 0xFF333333, hoverAnimation);
        RenderUtil.drawRect(context, x, y, width, height, hoverColor);

        String name = numberValue.getName();
        String displayValue = String.valueOf(round(numberValue.getValue().doubleValue(), 2));
        int valueWidth = mc.textRenderer.getWidth(displayValue);
        int maxNameWidth = (int) (width - valueWidth - 12);
        String displayName = RenderUtil.trimTextToWidth(mc.textRenderer, name, maxNameWidth);

        context.drawText(mc.textRenderer, displayName, (int) (x + 4), (int) (y + 1), 0xFFFFFFFF, true);
        context.drawText(mc.textRenderer, displayValue,
                (int) (x + width - valueWidth - 4), (int) (y + 1), 0xFFFFFFFF, true);

        RenderUtil.drawRect(context, x + 3, y + height - 4, width - 6, 2, 0xFF1A1A1A);
        RenderUtil.drawRect(context, x + 3, y + height - 4, sliderAnimation, 2, 0xFF1E90FF);

        if (hovered && !name.equals(displayName)) {
            pendingTooltip = name;
            tooltipMouseX = mouseX;
            tooltipMouseY = mouseY;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    private double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
