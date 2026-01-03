package cn.jeyor1337.foxsense.base.gui.component;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import cn.jeyor1337.foxsense.base.gui.component.value.BooleanComponent;
import cn.jeyor1337.foxsense.base.gui.component.value.ColorComponent;
import cn.jeyor1337.foxsense.base.gui.component.value.ModeComponent;
import cn.jeyor1337.foxsense.base.gui.component.value.NumberComponent;
import cn.jeyor1337.foxsense.base.gui.component.value.ValueComponent;
import cn.jeyor1337.foxsense.base.gui.util.AnimationUtil;
import cn.jeyor1337.foxsense.base.gui.util.RenderUtil;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.ColorValue;
import cn.jeyor1337.foxsense.base.value.ModeValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.base.value.Value;
import net.minecraft.client.gui.DrawContext;

public class ModuleButton extends Component {
    private final Module module;
    private final List<ValueComponent> valueComponents;
    private boolean expanded;
    private boolean binding;
    private double hoverAnimation = 0.0;
    private double toggleAnimation = 0.0;

    public ModuleButton(Module module, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.module = module;
        this.valueComponents = new ArrayList<>();

        for (Value<?> value : module.getValues()) {
            if (value instanceof BooleanValue) {
                valueComponents.add(new BooleanComponent((BooleanValue) value, x, y, width, height));
            } else if (value instanceof NumberValue) {
                valueComponents.add(new NumberComponent((NumberValue) value, x, y, width, height));
            } else if (value instanceof ModeValue) {
                valueComponents.add(new ModeComponent((ModeValue) value, x, y, width, height));
            } else if (value instanceof ColorValue) {
                valueComponents.add(new ColorComponent((ColorValue) value, x, y, width, height));
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = RenderUtil.isHovered(mouseX, mouseY, x, y, width, height);

        hoverAnimation = AnimationUtil.animate(hoverAnimation, hovered ? 1.0 : 0.0, 0.1);
        toggleAnimation = AnimationUtil.animate(toggleAnimation, module.isEnabled() ? 1.0 : 0.0, 0.1);

        int backgroundColor = 0xFF2A2A2A;
        int activeColor = 0xFF1E90FF;

        int currentColor = AnimationUtil.animateColor(backgroundColor, activeColor, toggleAnimation);
        if (hovered) {
            currentColor = AnimationUtil.animateColor(currentColor, activeColor, 0.2);
        }

        RenderUtil.drawRect(context, x, y, width, height, currentColor);

        String text = module.getName();
        if (binding) {
            text = "Binding...";
        }
        context.drawText(mc.textRenderer, text, (int) (x + width / 2 - mc.textRenderer.getWidth(text) / 2f),
                (int) (y + height / 2 - mc.textRenderer.fontHeight / 2f), 0xFFFFFFFF, true);

        if (expanded) {
            double currentY = y + height;
            for (ValueComponent component : valueComponents) {
                if (component.isVisible()) {
                    component.setX(x);
                    component.setY(currentY);
                    component.render(context, mouseX, mouseY, delta);
                    currentY += component.getHeight();
                }
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
            if (button == 0) {
                module.toggle();
            } else if (button == 1) {
                expanded = !expanded;
            } else if (button == 2) {
                binding = !binding;
            }
        }

        if (expanded) {
            for (ValueComponent component : valueComponents) {
                if (component.isVisible()) {
                    component.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (expanded) {
            for (ValueComponent component : valueComponents) {
                if (component.isVisible()) {
                    component.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                module.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                module.setKeybind(keyCode);
            }
            binding = false;
        }

        if (expanded) {
            for (ValueComponent component : valueComponents) {
                if (component.isVisible()) {
                    component.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    public double getTotalHeight() {
        double h = height;
        if (expanded) {
            for (ValueComponent component : valueComponents) {
                if (component.isVisible()) {
                    h += component.getHeight();
                }
            }
        }
        return h;
    }

    public void renderTooltips(DrawContext context) {
        if (expanded) {
            for (ValueComponent component : valueComponents) {
                if (component.isVisible() && component.getPendingTooltip() != null) {
                    cn.jeyor1337.foxsense.base.gui.util.RenderUtil.drawTooltip(
                            context,
                            component.getPendingTooltip(),
                            component.getTooltipMouseX(),
                            component.getTooltipMouseY());
                    break;
                }
            }
        }
    }
}
