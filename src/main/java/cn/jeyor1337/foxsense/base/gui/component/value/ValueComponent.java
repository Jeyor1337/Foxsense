package cn.jeyor1337.foxsense.base.gui.component.value;

import cn.jeyor1337.foxsense.base.gui.component.Component;
import cn.jeyor1337.foxsense.base.value.Value;
import net.minecraft.client.gui.DrawContext;

public abstract class ValueComponent extends Component {
    protected final Value<?> value;
    protected String pendingTooltip = null;
    protected int tooltipMouseX = 0;
    protected int tooltipMouseY = 0;

    public ValueComponent(Value<?> value, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.value = value;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    public boolean isVisible() {
        return value.isVisible();
    }

    public String getPendingTooltip() {
        return pendingTooltip;
    }

    public int getTooltipMouseX() {
        return tooltipMouseX;
    }

    public int getTooltipMouseY() {
        return tooltipMouseY;
    }

    public void clearTooltip() {
        pendingTooltip = null;
    }
}
