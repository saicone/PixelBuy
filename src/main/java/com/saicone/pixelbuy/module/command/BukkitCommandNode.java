package com.saicone.pixelbuy.module.command;

import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.util.OptionalType;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class BukkitCommandNode {

    private final String id;
    private final List<BukkitCommandNode> subCommands;

    private boolean register;
    private String name = getId();
    private List<String> aliases = List.of();
    private String permission = null;

    private List<String> tab = null;
    private RootCommand root;

    public BukkitCommandNode(@NotNull String id) {
        this.id = id;
        this.subCommands = null;
    }

    public BukkitCommandNode(@NotNull String id, @NotNull BukkitCommandNode... subCommands) {
        this.id = id;
        this.subCommands = new ArrayList<>();
        Collections.addAll(this.subCommands, subCommands);
    }

    public void onLoad(@NotNull BukkitSettings settings) {
        if (subCommands != null) {
            for (BukkitCommandNode subCommand : subCommands) {
                if (subCommand.main()) {
                    subCommand.onLoad(settings);
                }
            }
        }
        register = settings.getIgnoreCase("commands", id, "main").asBoolean(false);
        name = settings.getIgnoreCase("commands", id, "name").asString(id);
        aliases = settings.getIgnoreCase("commands", id, "aliases").asList(OptionalType::asString);
        permission = settings.getIgnoreCase("commands", id, "permission").asString();
        if (getSubCommands() != null) {
            tab = new ArrayList<>();
            for (BukkitCommandNode subCommand : getSubCommands()) {
                tab.add(subCommand.getName());
            }
        }
        if (register) {
            if (root == null) {
                root = new RootCommand();
            } else {
                BukkitCommand.unregister(root);
                root.reload();
                BukkitCommand.register(root);
            }
        } else if (root != null) {
            BukkitCommand.unregister(root);
        }
    }

    public boolean match(@NotNull String s) {
        if (name.equalsIgnoreCase(s)) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean main() {
        return false;
    }

    public boolean isRegister() {
        return register;
    }

    public boolean hasPermission(@NotNull CommandSender sender) {
        return getPermission() == null || sender.hasPermission(getPermission());
    }

    @NotNull
    public String getId() {
        return id;
    }

    @Nullable
    public List<BukkitCommandNode> getSubCommands() {
        return subCommands;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getAliases() {
        return aliases;
    }

    @Nullable
    public String getPermission() {
        return permission;
    }

    @Nullable
    public RootCommand getRoot() {
        return root;
    }

    @NotNull
    public String getUsage(@NotNull CommandSender sender) {
        return "";
    }

    @NotNull
    public String getDescription(@NotNull CommandSender sender) {
        return "";
    }

    public int getMinArgs() {
        return 0;
    }

    public int getMinArgs(@NotNull CommandSender sender) {
        return getMinArgs();
    }

    protected void run(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            sendPermissionMessage(sender);
            return;
        }
        if (getSubCommands() != null && args.length > 0) {
            for (BukkitCommandNode subCommand : subCommands) {
                if (subCommand.match(args[0])) {
                    final String[] array = new String[cmd.length + 1];
                    System.arraycopy(cmd, 0, array, 0, cmd.length);
                    array[cmd.length - 1] = args[0];
                    subCommand.run(sender, array, Arrays.copyOfRange(args, 1, args.length));
                    return;
                }
            }
        }
        if (args.length < getMinArgs(sender)) {
            sendUsage(sender, cmd, args);
        } else {
            execute(sender, cmd, args);
        }
    }

    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        sendUsage(sender, cmd, args);
    }

    @Nullable
    protected List<String> suggestion(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length < 1) {
            return tab != null ? tab : tabComplete(sender, alias, args);
        } else if (getSubCommands() != null) {
            for (BukkitCommandNode subCommand : getSubCommands()) {
                if (subCommand.match(args[0])) {
                    return subCommand.suggestion(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
                }
            }
        }
        return null;
    }

    @Nullable
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return null;
    }

    public void sendPermissionMessage(@NotNull CommandSender sender) {
        sender.sendMessage("You don't have permission to use this command");
    }

    public void sendUsage(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        final String usage = getUsage(sender);
        if (usage.isBlank()) {
            return;
        }
        final Object[] array = new Object[args.length + 1];
        array[0] = String.join(" ", cmd);
        System.arraycopy(args, 0, array, 1, args.length);
        sender.sendMessage(Strings.replaceArgs(usage, array));
        if (getSubCommands() != null) {
            for (BukkitCommandNode subCommand : getSubCommands()) {
                subCommand.sendSubUsage(sender);
            }
        }
    }

    public void sendSubUsage(@NotNull CommandSender sender) {
        final String description = getDescription(sender);
        sender.sendMessage("> " + getName() + (description.isBlank() ? "" : " - " + description));
    }

    public class RootCommand extends Command {

        protected RootCommand() {
            super(BukkitCommandNode.this.getName());
            reload();
        }

        public void reload() {
            setName(BukkitCommandNode.this.getName());
            setPermission(BukkitCommandNode.this.getPermission());
            setAliases(BukkitCommandNode.this.getAliases());
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            run(sender, new String[] {commandLabel}, args);
            return true;
        }

        @NotNull
        @Override
        public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
            final List<String> list = suggestion(sender, alias, args);
            return list != null ? list : super.tabComplete(sender, alias, args);
        }
    }
}
