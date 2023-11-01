package com.saicone.pixelbuy.module.command;

import com.google.common.base.Enums;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.util.OptionalType;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class BukkitCommandNode implements BukkitCommandExecution {

    protected BukkitCommandNode root;
    private final String id;
    protected List<BukkitCommandNode> subCommands;
    private final Supplier<String> path = Suppliers.memoize(() -> {
       if (getRoot() != null) {
           return getRoot().getPath() + "." + getId();
       } else {
           return getId();
       }
    });

    protected boolean register;
    protected String name = getId();
    protected List<String> aliases = List.of();
    protected String permission = null;

    protected List<String> tab = null;
    protected Bridge bridge;
    protected Cache<String, Long> delay;
    protected long delayTime;

    public BukkitCommandNode(@NotNull String id) {
        this.id = id;
        this.subCommands = null;
    }

    public BukkitCommandNode(@NotNull String id, @NotNull BukkitCommandNode... subCommands) {
        this.id = id;
        this.subCommands = new ArrayList<>();
        for (BukkitCommandNode subCommand : subCommands) {
            subCommand.setRoot(this);
            this.subCommands.add(subCommand);
        }
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
        final String delay = settings.getIgnoreCase("commands", id, "permission").asString();
        if (delay != null && !delay.isBlank() && !delay.trim().startsWith("-")) {
            final String[] split = delay.trim().split(" ", 2);
            if (Strings.isNumber(split[0])) {
                final long duration = Long.parseLong(split[0]);
                final TimeUnit unit;
                if (split.length < 2) {
                    unit = TimeUnit.SECONDS;
                } else {
                    unit = Enums.getIfPresent(TimeUnit.class, split[1].trim().toUpperCase()).or(TimeUnit.SECONDS);
                }
                this.delay = CacheBuilder.newBuilder().expireAfterWrite(duration, unit).build();
                this.delayTime = unit.toMillis(duration);
            }
        }
        if (getSubCommands() != null) {
            tab = new ArrayList<>();
            for (BukkitCommandNode subCommand : getSubCommands()) {
                tab.add(subCommand.getName());
            }
        }
        if (isRegister()) {
            if (bridge == null) {
                bridge = new Bridge();
            } else {
                BukkitCommand.unregister(bridge);
                bridge.reload();
                BukkitCommand.register(bridge);
            }
        } else if (bridge != null) {
            BukkitCommand.unregister(bridge);
        }
    }

    protected void subCommand(@NotNull BukkitCommandNode subCommand) {
        if (this.subCommands == null) {
            this.subCommands = new ArrayList<>();
        }
        subCommand.setRoot(this);
        this.subCommands.add(subCommand);
    }

    public boolean match(@NotNull String s) {
        if (getName().equalsIgnoreCase(s)) {
            return true;
        }
        for (String alias : getAliases()) {
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
        final String permission = getPermission();
        if (permission == null || permission.length() == 0) {
            return true;
        }

        for (String p : permission.split(";")) {
            if (sender.hasPermission(p)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public BukkitCommandNode getRoot() {
        return root;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public BukkitCommandNode getSubCommand(@NotNull String id) {
        if (subCommands != null) {
            for (BukkitCommandNode subCommand : subCommands) {
                if (subCommand.getId().equals(id)) {
                    return subCommand;
                }
            }
        }
        throw new IllegalArgumentException("The sub command '" + id + "' doesn't exist");
    }

    @Nullable
    public List<BukkitCommandNode> getSubCommands() {
        return subCommands;
    }

    @NotNull
    public String getPath() {
        return path.get();
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
    public BukkitCommandNode.Bridge getBridge() {
        return bridge;
    }

    @Nullable
    public Cache<String, Long> getDelay() {
        return delay;
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

    public int getSubStart() {
        return 0;
    }

    public int getSubStart(@NotNull CommandSender sender) {
        return getSubStart();
    }

    @Nullable
    public Float getDelayTime(@NotNull CommandSender sender) {
        //if (!(sender instanceof Player)) {
        //    return null;
        //}
        if (delay == null) {
            return null;
        }
        final Long time = delay.getIfPresent(sender.getName());
        if (time == null) {
            delay.put(sender.getName(), System.currentTimeMillis() + delayTime);
            return null;
        }
        return (time - System.currentTimeMillis()) / 1000.00F;
    }

    public void setRoot(@Nullable BukkitCommandNode root) {
        this.root = root;
    }

    protected void run(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            sendPermissionMessage(sender);
            return;
        }
        if (getSubCommands() != null) {
            final int start = getSubStart(sender);
            if (args.length > start) {
                for (BukkitCommandNode subCommand : subCommands) {
                    if (subCommand.match(args[start])) {
                        final String[] array = new String[cmd.length + start + 1];
                        System.arraycopy(cmd, 0, array, 0, cmd.length);
                        System.arraycopy(args, 0, array, cmd.length - 1, start + 1);
                        subCommand.run(sender, array, Arrays.copyOfRange(args, start + 1, args.length));
                        return;
                    }
                }
            }
        }
        if (args.length < getMinArgs(sender)) {
            sendUsage(sender, cmd, args);
        } else {
            execute(sender, cmd, args);
        }
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        sendUsage(sender, cmd, args);
    }

    @Nullable
    protected List<String> suggestion(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        final int start = getSubStart(sender);
        if (args.length < start + 1) {
            return tab != null ? tab : tabComplete(sender, alias, args);
        } else if (getSubCommands() != null) {
            for (BukkitCommandNode subCommand : getSubCommands()) {
                if (subCommand.match(args[start])) {
                    return subCommand.suggestion(sender, args[start], Arrays.copyOfRange(args, start + 1, args.length));
                }
            }
        }
        return tabComplete(sender, alias, args);
    }

    @Nullable
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return null;
    }

    public void sendPermissionMessage(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is a mistake.");
    }

    public void sendDelayMessage(@NotNull CommandSender sender, float seconds) {
        sender.sendMessage(ChatColor.RED + "You should wait " + ChatColor.GOLD + seconds + ChatColor.RED + " seconds after execute this command again.");
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
        sender.sendMessage(ChatColor.GOLD + "> " + ChatColor.RED + getName() + (description.isBlank() ? "" : ChatColor.GOLD + " - " + ChatColor.GRAY + description));
    }

    public class Bridge extends Command {

        protected Bridge() {
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
            final Float seconds = getDelayTime(sender);
            if (seconds == null) {
                run(sender, new String[] {commandLabel}, args);
            } else {
                sendDelayMessage(sender, seconds);
            }
            return true;
        }

        @NotNull
        @Override
        public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
            final List<String> list = suggestion(sender, alias, args);
            return list != null ? list : super.tabComplete(sender, alias, args);
        }

        @Override
        public boolean testPermission(@NotNull CommandSender target) {
            if (testPermissionSilent(target)) {
                return true;
            }
            sendPermissionMessage(target);
            return false;
        }

        @Override
        public boolean testPermissionSilent(@NotNull CommandSender target) {
            return hasPermission(target);
        }
    }
}
