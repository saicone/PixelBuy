package com.saicone.pixelbuy.core.command.sub;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.core.command.PixelCommand;
import com.saicone.pixelbuy.core.store.StoreCategory;
import com.saicone.pixelbuy.util.MStrings;
import com.saicone.pixelbuy.util.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StoreCommand extends PixelCommand {

    private static final int PAGE_SIZE = 20;

    public StoreCommand() {
        super("store");
        subCommand("categories", this::categories);
        subCommand("items", this::items);
    }

    @Override
    public boolean main() {
        return true;
    }

    @Override
    public int getMinArgs(@NotNull CommandSender sender) {
        if (sender instanceof Player) {
            return 0;
        }
        return 1;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {

    }

    public void categories(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        sendLang(sender, "Categories.Info");
        for (Map.Entry<String, StoreCategory> entry : PixelBuy.get().getStore().getCategories().entrySet()) {
            sender.sendMessage(MStrings.color("&e- &f" + entry.getKey()));
        }
    }

    public void items(@NotNull CommandSender sender, @NotNull String[] cmd, @NotNull String[] args) {
        final String category;
        final int page;
        if (args.length > 0) {
            if (Strings.isNumber(args[0])) {
                page = Integer.parseInt(args[0]);
                if (args.length > 1) {
                    category = args[1];
                } else {
                    category = null;
                }
            } else {
                category = args[0];
                if (args.length > 1 && Strings.isNumber(args[1])) {
                    page = Integer.parseInt(args[1]);
                } else {
                    page = 1;
                }
            }
        } else {
            category = null;
            page = 1;
        }

        final List<String> items = PixelBuy.get().getStore().getItems().entrySet().stream()
                .filter(entry -> category == null || entry.getValue().getCategories().contains(category))
                .map(Map.Entry::getKey)
                .sorted().collect(Collectors.toList());
        sendLang(sender, "Items.Info", page, (items.size() - 1) / PAGE_SIZE + 1);
        int itemNum = 1;
        int start = (page - 1) * PAGE_SIZE;
        int i = 0;
        for (String item : items) {
            if (i < start) {
                i++;
                continue;
            }
            if (itemNum > PAGE_SIZE) {
                break;
            }
            final ItemStack display = PixelBuy.get().getStore().getItem(item).getDisplay();
            if (display != null && display.hasItemMeta() && display.getItemMeta().hasDisplayName()) {
                sender.sendMessage(MStrings.color("&e- &f" + item + " &7- " + display.getItemMeta().getDisplayName()));
            } else {
                sender.sendMessage(MStrings.color("&e- &f" + item));
            }
            itemNum++;
        }
    }
}
