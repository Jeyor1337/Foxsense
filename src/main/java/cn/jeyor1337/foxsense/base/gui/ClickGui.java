package cn.jeyor1337.foxsense.base.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import cn.jeyor1337.foxsense.base.gui.component.ConfigPanel;
import cn.jeyor1337.foxsense.base.gui.component.Frame;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGui extends Screen {
    private final List<Frame> frames;
    private final ConfigPanel configPanel;
    private boolean initialized;

    public ClickGui() {
        super(Text.of("ClickGui"));
        this.frames = new ArrayList<>();
        this.configPanel = new ConfigPanel(10, 10, 100, 100);
        this.initialized = false;
    }

    @Override
    protected void init() {
        if (!initialized) {
            double x = 10;
            double y = 10;
            for (ModuleType type : ModuleType.values()) {
                frames.add(new Frame(type, x, y, 100, 16));
                x += 110;
            }
            configPanel.setX(x);
            configPanel.setY(y);
            initialized = true;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Frame frame : frames) {
            frame.render(context, mouseX, mouseY, delta);
        }

        configPanel.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        renderTooltips(context, mouseX, mouseY);
    }

    private void renderTooltips(DrawContext context, int mouseX, int mouseY) {
        for (Frame frame : frames) {
            frame.renderTooltips(context);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean consumed) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        for (Frame frame : frames) {
            frame.mouseClicked(mouseX, mouseY, button);
        }
        configPanel.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(click, consumed);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }
        configPanel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        for (Frame frame : frames) {
            frame.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        configPanel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        int keyCode = input.key();
        int scanCode = input.scancode();
        int modifiers = input.modifiers();

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        for (Frame frame : frames) {
            frame.keyPressed(keyCode, scanCode, modifiers);
        }
        configPanel.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
