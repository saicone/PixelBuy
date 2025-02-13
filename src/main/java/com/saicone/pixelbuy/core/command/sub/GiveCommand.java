package com.saicone.pixelbuy.core.command.sub;

import com.saicone.nbt.io.TagReader;
import com.saicone.pixelbuy.core.command.PixelCommand;
import com.saicone.pixelbuy.module.hook.Placeholders;
import com.saicone.pixelbuy.module.settings.SettingsItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class GiveCommand extends PixelCommand {

    public GiveCommand() {
        super("give");
    }

    @Override
    public boolean main() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        final Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sendLang(sender, "Error", args[0]);
            return;
        }

        final String snbt = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        final SettingsItem settings = SettingsItem.of(TagReader.fromString(snbt));
        final ItemStack item = settings.parse(s -> Placeholders.parse(player, s)).build();
        if (!player.getInventory().addItem(item).isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), item);
        }

        sendLang(sender, "Done", snbt, args[0]);
    }
}
