package cn.jeyor1337.foxsense.base.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.MinecraftClient;

public abstract class Command {
    protected final String name;
    protected final String description;
    protected final String syntax;
    protected final List<String> aliases;
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public Command(String name, String description, String syntax, String... aliases) {
        this.name = name;
        this.description = description;
        this.syntax = syntax;
        this.aliases = Arrays.asList(aliases);
    }

    public abstract void execute(String[] args);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSyntax() {
        return syntax;
    }

    public List<String> getAliases() {
        return aliases;
    }

    protected void sendMessage(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§7[§bFoxsense§7] §f" + message), false);
        }
    }

    protected void sendError(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§7[§bFoxsense§7] §c" + message), false);
        }
    }
}
