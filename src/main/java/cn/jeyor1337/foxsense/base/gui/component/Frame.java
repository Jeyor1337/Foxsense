package cn.jeyor1337.foxsense.base.gui.component;

import java.util.ArrayList;
import java.util.List;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.gui.util.AnimationUtil;
import cn.jeyor1337.foxsense.base.gui.util.RenderUtil;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import net.minecraft.client.gui.DrawContext;

public class Frame extends Component {
    private final ModuleType type;
    private final List<ModuleButton> buttons;
    private boolean dragging;
    private double dragX, dragY;
    private boolean expanded;
    private double expandAnimation;

    public Frame(ModuleType type, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.type = type;
        this.buttons = new ArrayList<>();
        this.expanded = true;
        this.expandAnimation = 1.0;

        initButtons();
    }

    private void initButtons() {
        double yOffset = height;
        for (Module module : Foxsense.getModuleManager().getModulesByType(type)) {
            ModuleButton button = new ModuleButton(module, x, y + yOffset, width, 14);
            buttons.add(button);
            yOffset += 14;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        expandAnimation = AnimationUtil.animate(expandAnimation, expanded ? 1.0 : 0.0, 0.15);

        RenderUtil.drawRect(context, x, y, width, height, 0xFF1A1A1A);
        RenderUtil.drawGradientRect(context, x, y, width, height, 0xFF0066CC, 0xFF0044AA);

        context.drawText(mc.textRenderer, type.name(), (int) (x + 5), (int) (y + 4), 0xFFFFFFFF, true);

        String expandSymbol = expanded ? "-" : "+";
        context.drawText(mc.textRenderer, expandSymbol, (int) (x + width - 12), (int) (y + 4), 0xFFFFFFFF, true);

        if (expandAnimation > 0.01) {
            double currentY = y + height;
            for (ModuleButton button : buttons) {
                button.setX(x);
                button.setY(currentY);
                button.setWidth(width);

                if (expandAnimation < 1.0) {
                    context.enableScissor(
                            (int) x,
                            (int) (y + height),
                            (int) (x + width),
                            (int) (y + height + getTotalButtonHeight() * expandAnimation));
                }

                button.render(context, mouseX, mouseY, delta);
                currentY += button.getTotalHeight();

                if (expandAnimation < 1.0) {
                    context.disableScissor();
                }
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            } else if (button == 1) {
                expanded = !expanded;
            }
        }

        if (expanded) {
            for (ModuleButton moduleButton : buttons) {
                moduleButton.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }

        if (expanded) {
            for (ModuleButton moduleButton : buttons) {
                moduleButton.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        if (expanded) {
            for (ModuleButton moduleButton : buttons) {
                moduleButton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (expanded) {
            for (ModuleButton button : buttons) {
                button.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (expanded) {
            for (ModuleButton button : buttons) {
                button.charTyped(chr, modifiers);
            }
        }
    }

    private double getTotalButtonHeight() {
        double total = 0;
        for (ModuleButton button : buttons) {
            total += button.getTotalHeight();
        }
        return total;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void renderTooltips(DrawContext context) {
        if (expanded) {
            for (ModuleButton button : buttons) {
                button.renderTooltips(context);
            }
        }
    }
}
