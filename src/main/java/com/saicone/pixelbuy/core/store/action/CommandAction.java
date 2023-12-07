package com.saicone.pixelbuy.core.store.action;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.util.OptionalType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandAction extends StoreAction {

    public static final Builder<CommandAction> BUILDER = new Builder<CommandAction>("(?i)((run|execute)-?)?(multi(ples)?-?)?(command|cmd)s?")
            .accept((id, config) -> {
                final List<String> commands = config.getRegex("(?i)(value|command|cmd)s?").asList(OptionalType::asString);
                final boolean console = config.getRegex("(?i)console(-?sender)?").asBoolean(true);
                final boolean multiple = id.toLowerCase().contains("multi") || config.getRegex("(?i)multiples?").asBoolean(false);
                return new CommandAction(commands, console, multiple);
            });

    private final List<String> commands;
    private final boolean console;
    private final boolean multiple;

    public CommandAction(@NotNull List<String> commands, boolean console, boolean multiple) {
        this.commands = commands;
        this.console = console;
        this.multiple = multiple;
    }

    @NotNull
    public List<String> getCommands() {
        return commands;
    }

    public boolean isConsole() {
        return console;
    }

    public boolean isMultiple() {
        return multiple;
    }

    @Override
    public void run(@NotNull StoreClient client, int amount) {
        final CommandSender sender;
        if (isConsole()) {
            sender = Bukkit.getConsoleSender();
        } else if (client.isOnline()) {
            sender = client.getPlayer();
        } else {
            return;
        }
        final List<String> cmds = client.parse(getCommands());
        if (Bukkit.isPrimaryThread()) {
            dispatch(sender, cmds, amount);
        } else {
            Bukkit.getScheduler().runTask(PixelBuy.get(), () -> dispatch(sender, cmds, amount));
        }
    }

    private void dispatch(@NotNull CommandSender sender, @NotNull List<String> cmds, int amount) {
        if (isMultiple()) {
            for (int i = 0; i < amount; i++) {
                for (String cmd : cmds) {
                    Bukkit.getServer().dispatchCommand(sender, cmd);
                }
            }
        } else {
            for (String cmd : cmds) {
                Bukkit.getServer().dispatchCommand(sender, cmd);
            }
        }
    }

    @Override
    public String toString() {
        return getCommands().toString();
    }
}
