package cn.jeyor1337.foxsense.base.manager;

import java.util.ArrayList;
import java.util.List;

import cn.jeyor1337.foxsense.base.command.Command;
import cn.jeyor1337.foxsense.base.command.impl.BindCommand;
import cn.jeyor1337.foxsense.base.command.impl.ConfigCommand;
import cn.jeyor1337.foxsense.base.command.impl.HelpCommand;
import cn.jeyor1337.foxsense.base.command.impl.ToggleCommand;

public class CommandManager {
    private final List<Command> commands;
    private final String prefix = "!";

    public CommandManager() {
        this.commands = new ArrayList<>();
        init();
    }

    private void init() {
        registerCommand(new HelpCommand());
        registerCommand(new ToggleCommand());
        registerCommand(new BindCommand());
        registerCommand(new ConfigCommand());
    }

    public void registerCommand(Command command) {
        commands.add(command);
    }

    public void unregisterCommand(Command command) {
        commands.remove(command);
    }

    public Command getCommand(String name) {
        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(name)) {
                return command;
            }
            for (String alias : command.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return command;
                }
            }
        }
        return null;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean handleCommand(String message) {
        if (!message.startsWith(prefix)) {
            return false;
        }

        String commandString = message.substring(prefix.length());
        String[] parts = commandString.split(" ");

        if (parts.length == 0) {
            return false;
        }

        String commandName = parts[0];
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        Command command = getCommand(commandName);

        if (command != null) {
            try {
                command.execute(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }
}
