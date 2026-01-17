package cn.jeyor1337.foxsense.base.gui.component;

import java.util.ArrayList;
import java.util.List;

import cn.jeyor1337.foxsense.Foxsense;
import cn.jeyor1337.foxsense.base.gui.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class ConfigPanel extends Component {
    private final List<String> configs;
    private boolean dragging;
    private double dragX, dragY;
    private int selectedIndex = -1;

    public ConfigPanel(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.configs = new ArrayList<>();
        loadConfigs();
    }

    private void loadConfigs() {
        if (Foxsense.getConfigManager() == null) {
            return;
        }
        configs.clear();
        for (cn.jeyor1337.foxsense.base.config.Config config : Foxsense.getConfigManager().getConfigs()) {
            configs.add(config.getName());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        loadConfigs();

        RenderUtil.drawRect(context, x, y, width, 16, 0xFF1A1A1A);
        RenderUtil.drawGradientRect(context, x, y, width, 16, 0xFF0066CC, 0xFF0044AA);

        context.drawText(mc.textRenderer, "Configs", (int) (x + 5), (int) (y + 4), 0xFFFFFFFF, true);

        double currentY = y + 16;
        for (int i = 0; i < configs.size(); i++) {
            String config = configs.get(i);
            boolean hovered = RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, 14);
            boolean selected = i == selectedIndex;

            int bgColor = selected ? 0xFF1E90FF : (hovered ? 0xFF2A2A2A : 0xFF1A1A1A);
            RenderUtil.drawRect(context, x, currentY, width, 14, bgColor);

            context.drawText(mc.textRenderer, config, (int) (x + 5), (int) (currentY + 3), 0xFFFFFFFF, true);
            currentY += 14;
        }

        RenderUtil.drawRect(context, x, currentY, width, 14, 0xFF1A1A1A);
        context.drawText(mc.textRenderer, "+ New Config", (int) (x + 5), (int) (currentY + 3), 0xFF00FF00, true);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (RenderUtil.isHovered(mouseX, mouseY, x, y, width, 16)) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            }
            return;
        }

        double currentY = y + 16;
        for (int i = 0; i < configs.size(); i++) {
            if (RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, 14)) {
                if (button == 0) {
                    selectedIndex = i;
                    String configName = configs.get(i);
                    cn.jeyor1337.foxsense.base.config.Config config = Foxsense.getConfigManager().getConfig(configName);
                    if (config != null) {
                        Foxsense.getConfigManager().loadConfig(config);
                        Foxsense.getConfigManager().setCurrentConfig(config);
                    }
                } else if (button == 1) {
                    String configName = configs.get(i);
                    cn.jeyor1337.foxsense.base.config.Config config = Foxsense.getConfigManager().getConfig(configName);
                    if (config != null) {
                        Foxsense.getConfigManager().deleteConfig(config);
                        if (selectedIndex == i) {
                            selectedIndex = -1;
                        } else if (selectedIndex > i) {
                            selectedIndex--;
                        }
                        loadConfigs();
                    }
                }
                return;
            }
            currentY += 14;
        }

        if (RenderUtil.isHovered(mouseX, mouseY, x, currentY, width, 14) && button == 0) {
            String configName = "config_" + System.currentTimeMillis();
            cn.jeyor1337.foxsense.base.config.Config newConfig = Foxsense.getConfigManager().createConfig(configName,
                    false);
            Foxsense.getConfigManager().saveConfig(newConfig);
            loadConfigs();
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }
    }
}
