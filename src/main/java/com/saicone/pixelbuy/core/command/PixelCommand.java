package com.saicone.pixelbuy.core.command;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.Lang;
import com.saicone.pixelbuy.module.command.BukkitCommandExecution;
import com.saicone.pixelbuy.module.command.BukkitCommandNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class PixelCommand extends BukkitCommandNode {

    public PixelCommand(@NotNull String id) {
        super(id);
    }

    public PixelCommand(@NotNull String id, @NotNull BukkitCommandNode... subCommands) {
        super(id, subCommands);
    }

    public void subCommand(@NotNull String id, @NotNull BukkitCommandExecution execution) {
        subCommand(new PixelCommand(id) {
            @Override
            public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
                execution.execute(sender, cmd, args);
            }
        });
    }

    public void subCommand(@NotNull String id, int minArgs, @NotNull BukkitCommandExecution execution) {
        subCommand(new PixelCommand(id) {
            @Override
            public int getMinArgs() {
                return minArgs;
            }

            @Override
            public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
                execution.execute(sender, cmd, args);
            }
        });
    }

    @Override
    public @NotNull String getUsage(@NotNull CommandSender sender) {
        final String usage = PixelBuy.get().getLang().getLangTextOrNull(sender, "Command." + getPath() + ".Usage");
        return usage != null ? usage : Lang.COMMAND_DISPLAY_USAGE.getText(sender);
    }

    @Override
    public @NotNull String getDescription(@NotNull CommandSender sender) {
        return PixelBuy.get().getLang().getLangText(sender, "Command." + getPath() + ".Description");
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return super.hasPermission(sender) || sender.hasPermission("pixelbuy.*");
    }

    @Override
    protected void run(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        super.run(sender, cmd, args);
    }

    @Override
    public void sendPermissionMessage(@NotNull CommandSender sender) {
        Lang.NO_PERM.sendTo(sender, getPermission());
    }

    @Override
    public void sendSubUsage(@NotNull CommandSender sender) {
        Lang.COMMAND_DISPLAY_SUB.sendTo(sender, getName(), getDescription(sender));
    }

    public void sendLang(@NotNull CommandSender sender, @NotNull String path, @NotNull Object... args) {
        PixelBuy.get().getLang().sendTo(sender, "Command." + getPath() + "." + path, args);
    }

    public void sendLang(@NotNull CommandSender sender, @NotNull String path, @NotNull String[] cmd, @NotNull String[] args) {
        final Object[] array = new Object[args.length + 1];
        array[0] = String.join(" ", cmd);
        System.arraycopy(args, 0, array, 1, args.length);
        PixelBuy.get().getLang().sendTo(sender, "Command." + getPath() + "." + path, array);
    }
}
