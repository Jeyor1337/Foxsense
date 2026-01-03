package cn.jeyor1337.foxsense.base.gui.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class RenderUtil {
    public static void drawRect(DrawContext context, double x, double y, double width, double height, int color) {
        context.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
    }

    public static void drawGradientRect(DrawContext context, double x, double y, double width, double height,
            int startColor, int endColor) {
        context.fillGradient((int) x, (int) y, (int) (x + width), (int) (y + height), startColor, endColor);
    }

    public static void drawBorder(DrawContext context, double x, double y, double width, double height, int color,
            double borderWidth) {
        drawRect(context, x, y, width, borderWidth, color);
        drawRect(context, x, y + height - borderWidth, width, borderWidth, color);
        drawRect(context, x, y, borderWidth, height, color);
        drawRect(context, x + width - borderWidth, y, borderWidth, height, color);
    }

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static String trimTextToWidth(TextRenderer textRenderer, String text, int maxWidth) {
        if (textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = textRenderer.getWidth(ellipsis);
        StringBuilder trimmed = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (textRenderer.getWidth(trimmed.toString() + c) + ellipsisWidth > maxWidth) {
                break;
            }
            trimmed.append(c);
        }
        return trimmed.toString() + ellipsis;
    }

    public static void drawTooltip(DrawContext context, String text, int mouseX, int mouseY) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer textRenderer = mc.textRenderer;

        int textWidth = textRenderer.getWidth(text);
        int tooltipWidth = textWidth + 6;
        int tooltipHeight = textRenderer.fontHeight + 4;

        int tooltipX = mouseX + 8;
        int tooltipY = mouseY + 8;

        if (mc.getWindow() != null) {
            int screenWidth = mc.getWindow().getScaledWidth();
            int screenHeight = mc.getWindow().getScaledHeight();

            if (tooltipX + tooltipWidth > screenWidth) {
                tooltipX = mouseX - tooltipWidth - 8;
            }
            if (tooltipY + tooltipHeight > screenHeight) {
                tooltipY = mouseY - tooltipHeight - 8;
            }
        }

        drawRect(context, tooltipX - 2, tooltipY - 2, tooltipWidth, tooltipHeight, 0xF0000000);
        drawBorder(context, tooltipX - 2, tooltipY - 2, tooltipWidth, tooltipHeight, 0xFF1E90FF, 1);
        context.drawText(textRenderer, text, tooltipX, tooltipY, 0xFFFFFFFF, true);
    }
}
