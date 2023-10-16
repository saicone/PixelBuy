package com.saicone.pixelbuy.core.store.action;

import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.util.OptionalType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandAction extends StoreAction {

    public static final Builder<CommandAction> BUILDER = new Builder<CommandAction>("(?i)((run|execute)-?)?(command|cmd)")
            .accept(config -> {
                final List<String> commands = config.getRegex("(?i)(value|command|cmd)s?").asList(OptionalType::asString);
                final boolean console = config.getRegex("(?i)console(-?sender)?").asBoolean(true);
                return new CommandAction(commands, console);
            });

    private final List<String> commands;
    private final boolean console;

    public CommandAction(@NotNull List<String> commands, boolean console) {
        this.commands = commands;
        this.console = console;
    }

    @NotNull
    public List<String> getCommands() {
        return commands;
    }

    public boolean isConsole() {
        return console;
    }

    @Override
    public void run(@NotNull StoreClient client) {
        final CommandSender sender;
        if (isConsole()) {
            sender = Bukkit.getConsoleSender();
        } else if (client.isOnline()) {
            sender = client.getPlayer();
        } else {
            return;
        }
        final List<String> cmds = client.parse(getCommands());
        for (String cmd : cmds) {
            Bukkit.getServer().dispatchCommand(sender, cmd);
        }
    }

    @Override
    public String toString() {
        return getCommands().toString();
    }
}
