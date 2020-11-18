package com.minelatino.pixelbuy.managers.store;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.managers.store.acts.BroadcastAction;
import com.minelatino.pixelbuy.managers.store.acts.CommandAction;
import com.minelatino.pixelbuy.managers.store.acts.ItemAction;
import com.minelatino.pixelbuy.managers.store.acts.MessageAction;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StoreManager {

    private FileConfiguration store;

    private String storeName = "";

    private Double discount = 1D;

    private final PixelBuy pl = PixelBuy.get();

    private static List<ActionType> actions = Arrays.asList(
            new BroadcastAction(),
            new CommandAction(),
            new ItemAction(),
            new MessageAction()
    );

    private final List<StoreItem> items = new ArrayList<>();

    public StoreManager() {
        reload(true);
    }

    public void reload(boolean init) {
        if (!init) items.clear();
        File cF = new File(pl.getDataFolder(), "store.yml");
        if (!cF.exists()) pl.saveResource("store.yml", false);
        store = YamlConfiguration.loadConfiguration(cF);
        storeName = store.getString("Name");
        String dis = store.getString("Global-Discount", "1");
        discount = Double.parseDouble("0." + (dis.contains("%") ? dis.replace("%", "") : dis.split("\\.", 2)[1]));
        for (String identifier : Objects.requireNonNull(store.getConfigurationSection("Items")).getKeys(false)) {
            items.add(new StoreItem(identifier, store.getString("Items." + identifier + ".price"), store.getBoolean("Items." + identifier + ".online"), parseActions(store.getStringList("Items." + identifier + ".execute"), store.getString("Items." + identifier + ".price"))));
        }
    }

    public static List<ActionType> parseActions(List<String> list, String price) {
        List<ActionType> acts = new ArrayList<>();
        for (String string : list) {
            String type = string.split(":", 2)[0].toUpperCase();
            ActionType action = actions.stream().filter(a -> a.getType().equals(type)).findFirst().orElse(null);
            if (action != null) {
                action.setParts(string.split(":", 2)[1], price);
                acts.add(action);
            }
        }
        return acts;
    }

    public StoreItem getItem(String identifier) {
        for (StoreItem item : items) {
            if (item.getIdentifier().equals(identifier)) return item;
        }
        return null;
    }
}
