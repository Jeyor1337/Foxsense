package cn.jeyor1337.foxsense.base.gui.component.value;

import cn.jeyor1337.foxsense.base.gui.util.AnimationUtil;
import cn.jeyor1337.foxsense.base.gui.util.RenderUtil;
import cn.jeyor1337.foxsense.base.value.ColorValue;
import net.minecraft.client.gui.DrawContext;

public class ColorComponent extends ValueComponent {
    private final ColorValue colorValue;
    private double titleHoverAnimation = 0.0;
    private double redHoverAnimation = 0.0;
    private double greenHoverAnimation = 0.0;
    private double blueHoverAnimation = 0.0;
    private double alphaHoverAnimation = 0.0;

    private double redSliderAnimation = 0.0;
    private double greenSliderAnimation = 0.0;
    private double blueSliderAnimation = 0.0;
    private double alphaSliderAnimation = 0.0;

    private boolean draggingRed;
    private boolean draggingGreen;
    private boolean draggingBlue;
    private boolean draggingAlpha;

    public ColorComponent(ColorValue value, double x, double y, double width, double height) {
        super(value, x, y, width, height * 5);
        this.colorValue = value;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int backgroundColor = 0xFF232323;
        double rowHeight = 14;

        if (draggingRed) {
            updateRed(mouseX);
        }
        if (draggingGreen) {
            updateGreen(mouseX);
        }
        if (draggingBlue) {
            updateBlue(mouseX);
        }
        if (draggingAlpha) {
            updateAlpha(mouseX);
        }

        boolean titleHovered = RenderUtil.isHovered(mouseX, mouseY, x, y, width, rowHeight);
        titleHoverAnimation = AnimationUtil.animate(titleHovered ? 1.0 : 0.0, titleHoverAnimation, 0.1);
        int titleHoverColor = AnimationUtil.animateColor(backgroundColor, 0xFF333333, titleHoverAnimation);
        RenderUtil.drawRect(context, x, y, width, rowHeight, titleHoverColor);

        double colorPreviewSize = 12;
        double colorPreviewX = x + width - colorPreviewSize - 4;
        double colorPreviewY = y + rowHeight / 2 - colorPreviewSize / 2;
        RenderUtil.drawRect(context, colorPreviewX, colorPreviewY, colorPreviewSize, colorPreviewSize,
                colorValue.getValue());
        RenderUtil.drawBorder(context, colorPreviewX, colorPreviewY, colorPreviewSize, colorPreviewSize, 0xFFFFFFFF, 1);

        String name = colorValue.getName();
        int maxNameWidth = (int) (width - colorPreviewSize - 12);
        String displayName = RenderUtil.trimTextToWidth(mc.textRenderer, name, maxNameWidth);
        context.drawText(mc.textRenderer, displayName, (int) (x + 4), (int) (y + 2), 0xFFFFFFFF, true);

        if (titleHovered && !name.equals(displayName)) {
            pendingTooltip = name;
            tooltipMouseX = mouseX;
            tooltipMouseY = mouseY;
        } else {
            pendingTooltip = null;
        }

        double currentY = y + rowHeight;

        renderColorSlider(context, mouseX, mouseY, currentY, "R", colorValue.getRed(), 0xFFFF0000, redHoverAnimation,
                redSliderAnimation, draggingRed);
        redHoverAnimation = AnimationUtil.animate(
                RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, rowHeight) ? 1.0 : 0.0, redHoverAnimation,
                0.1);
        redSliderAnimation = AnimationUtil.animate((colorValue.getRed() / 255.0) * (width - 6), redSliderAnimation,
                0.1);

        currentY += rowHeight;
        renderColorSlider(context, mouseX, mouseY, currentY, "G", colorValue.getGreen(), 0xFF00FF00,
                greenHoverAnimation, greenSliderAnimation, draggingGreen);
        greenHoverAnimation = AnimationUtil.animate(
                RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, rowHeight) ? 1.0 : 0.0, greenHoverAnimation,
                0.1);
        greenSliderAnimation = AnimationUtil.animate((colorValue.getGreen() / 255.0) * (width - 6),
                greenSliderAnimation, 0.1);

        currentY += rowHeight;
        renderColorSlider(context, mouseX, mouseY, currentY, "B", colorValue.getBlue(), 0xFF0000FF, blueHoverAnimation,
                blueSliderAnimation, draggingBlue);
        blueHoverAnimation = AnimationUtil.animate(
                RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, rowHeight) ? 1.0 : 0.0, blueHoverAnimation,
                0.1);
        blueSliderAnimation = AnimationUtil.animate((colorValue.getBlue() / 255.0) * (width - 6), blueSliderAnimation,
                0.1);

        currentY += rowHeight;
        renderColorSlider(context, mouseX, mouseY, currentY, "A", colorValue.getAlpha(), 0xFFFFFFFF,
                alphaHoverAnimation, alphaSliderAnimation, draggingAlpha);
        alphaHoverAnimation = AnimationUtil.animate(
                RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, rowHeight) ? 1.0 : 0.0, alphaHoverAnimation,
                0.1);
        alphaSliderAnimation = AnimationUtil.animate((colorValue.getAlpha() / 255.0) * (width - 6),
                alphaSliderAnimation, 0.1);
    }

    private void renderColorSlider(DrawContext context, int mouseX, int mouseY, double currentY, String label,
            int value, int color, double hoverAnim, double sliderAnim, boolean dragging) {
        int backgroundColor = 0xFF232323;
        int hoverColor = AnimationUtil.animateColor(backgroundColor, 0xFF333333, hoverAnim);
        double rowHeight = 14;

        RenderUtil.drawRect(context, x, currentY, width, rowHeight, hoverColor);

        context.drawText(mc.textRenderer, label, (int) (x + 4), (int) (currentY + 1), color, true);

        String valueText = String.valueOf(value);
        context.drawText(mc.textRenderer, valueText, (int) (x + width - mc.textRenderer.getWidth(valueText) - 4),
                (int) (currentY + 1), 0xFFFFFFFF, true);

        RenderUtil.drawRect(context, x + 3, currentY + rowHeight - 4, width - 6, 2, 0xFF1A1A1A);
        RenderUtil.drawRect(context, x + 3, currentY + rowHeight - 4, sliderAnim, 2, color);
    }

    private double getSliderValue(double mouseX, double x, double width) {
        double diff = Math.min(width, Math.max(0, mouseX - x));
        double newValue = diff / width;
        return Math.max(0, Math.min(1, newValue));
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double rowHeight = 14;
            double currentY = y + rowHeight;

            if (RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, rowHeight)) {
                draggingRed = true;
                updateRed(mouseX);
            }

            currentY += rowHeight;
            if (RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, rowHeight)) {
                draggingGreen = true;
                updateGreen(mouseX);
            }

            currentY += rowHeight;
            if (RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, rowHeight)) {
                draggingBlue = true;
                updateBlue(mouseX);
            }

            currentY += rowHeight;
            if (RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, rowHeight)) {
                draggingAlpha = true;
                updateAlpha(mouseX);
            }
        }
    }

    private void updateRed(double mouseX) {
        double sliderX = x + 3;
        double sliderWidth = width - 6;
        double value = getSliderValue(mouseX, sliderX, sliderWidth);
        colorValue.setRed((int) Math.round(value * 255));
    }

    private void updateGreen(double mouseX) {
        double sliderX = x + 3;
        double sliderWidth = width - 6;
        double value = getSliderValue(mouseX, sliderX, sliderWidth);
        colorValue.setGreen((int) Math.round(value * 255));
    }

    private void updateBlue(double mouseX) {
        double sliderX = x + 3;
        double sliderWidth = width - 6;
        double value = getSliderValue(mouseX, sliderX, sliderWidth);
        colorValue.setBlue((int) Math.round(value * 255));
    }

    private void updateAlpha(double mouseX) {
        double sliderX = x + 3;
        double sliderWidth = width - 6;
        double value = getSliderValue(mouseX, sliderX, sliderWidth);
        colorValue.setAlpha((int) Math.round(value * 255));
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingRed = false;
        draggingGreen = false;
        draggingBlue = false;
        draggingAlpha = false;
    }
}
