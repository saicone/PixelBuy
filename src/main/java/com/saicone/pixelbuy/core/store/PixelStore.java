package com.saicone.pixelbuy.core.store;

import com.saicone.pixelbuy.PixelBuy;
import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.core.store.action.BroadcastAction;
import com.saicone.pixelbuy.core.store.action.CommandAction;
import com.saicone.pixelbuy.core.store.action.ItemAction;
import com.saicone.pixelbuy.core.store.action.MessageAction;
import com.saicone.pixelbuy.core.web.WebSupervisor;
import com.saicone.pixelbuy.core.web.WebType;
import com.saicone.pixelbuy.module.settings.BukkitSettings;
import com.saicone.pixelbuy.module.settings.SettingsFile;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class PixelStore {

    private final SettingsFile config;
    private final Map<String, StoreAction.Builder<?>> actionTypes;
    private final Checkout checkout;

    private String name;
    private String group;
    private String defaultSupervisor;
    private final Map<String, StoreCategory> categories = new HashMap<>();
    private final Map<String, WebSupervisor> supervisors = new LinkedHashMap<>();
    private BukkitSettings baseItem;
    private final Map<String, StoreItem> items = new HashMap<>();

    public PixelStore() {
        config = new SettingsFile("store.yml");
        actionTypes = new HashMap<>();
        checkout = new Checkout(this);
        registerAction("pixelbuy:broadcast", BroadcastAction.BUILDER);
        registerAction("pixelbuy:command", CommandAction.BUILDER);
        registerAction("pixelbuy:item", ItemAction.BUILDER);
        registerAction("pixelbuy:message", MessageAction.BUILDER);
    }

    public void onLoad() {
        config.loadFrom(PixelBuy.get().getDataFolder(), true);
        name = config.getIgnoreCase("display", "name").asString("");
        group = config.getIgnoreCase("options", "group").asString("global");
        categories.clear();
        loadCategories(config.getRegex("(?i)categor(y|ies)").getValue());
        loadSupervisors();
        baseItem = config.getConfigurationSection(settings -> settings.getRegex("(?i)(def(ault)?-?)items?"));
        final Set<String> loadedItems = new HashSet<>();
        final File folder = new File(PixelBuy.get().getDataFolder(), "storeitems");
        if (!folder.exists()) {
            folder.mkdirs();
            PixelBuy.get().saveResource("storeitems/default.yml", false);
        }
        loadItems(folder, loadedItems);
        items.entrySet().removeIf(entry -> !loadedItems.contains(entry.getKey()));
        checkout.onLoad();
    }

    public void loadCategories(@Nullable Object object) {
        if (object instanceof ConfigurationSection) {
            for (String id : ((ConfigurationSection) object).getKeys(false)) {
                loadCategory(id, ((ConfigurationSection) object).get(id));
            }
        } else if (object instanceof Map) {
            for (var entry : ((Map<?, ?>) object).entrySet()) {
                loadCategory(String.valueOf(entry.getKey()), entry.getValue());
            }
        } else if (object instanceof Iterable) {
            for (Object o : (Iterable<?>) object) {
                loadCategories(o);
            }
        } else if (object instanceof String) {
            categories.put((String) object, new StoreCategory((String) object));
        }
    }

    public void loadCategory(@NotNull String id, @Nullable Object value) {
        final StoreCategory category = new StoreCategory(id);
        if (value instanceof Map || value instanceof ConfigurationSection) {
            category.onReload(BukkitSettings.of(value));
        }
        categories.put(id, category);
    }

    public void loadSupervisors() {
        final BukkitSettings section = BukkitSettings.of(config.getRegex("(?i)supervisors?").or(null));
        if (section == null) {
            for (var entry : supervisors.entrySet()) {
                entry.getValue().onClose();
            }
            return;
        }

        // Disable removed or reload current supervisors
        supervisors.entrySet().removeIf(entry -> {
            entry.getValue().onClose();
            if (section.contains(entry.getKey())) {
                final BukkitSettings config = section.getConfigurationSection(entry.getKey());
                if (config != null && WebType.of(config.getIgnoreCase("type").asString()) == entry.getValue().getType()) {
                    entry.getValue().onLoad(config);
                    entry.getValue().onStart();
                    return false;
                }
            }
            return true;
        });

        // Load default supervisor
        defaultSupervisor = config.getIgnoreCase("options", "supervisor").asString();
        if (defaultSupervisor == null && !supervisors.isEmpty()) {
            defaultSupervisor = supervisors.entrySet().iterator().next().getKey();
        }

        // Load added supervisors
        for (String key : section.getKeys(false)) {
            if (supervisors.containsKey(key)) {
                continue;
            }

            final BukkitSettings config = section.getConfigurationSection(key);
            if (config == null) {
                continue;
            }

            final WebType type = WebType.of(config.getIgnoreCase("type").asString());
            final WebSupervisor supervisor = type.newSupervisor(key, config.getIgnoreCase("group").asString(group));
            if (supervisor == null) {
                PixelBuy.log(2, "Unknown web type for '" + key + "' supervisor");
                continue;
            }

            supervisor.onLoad(config);
            supervisor.onStart();
            supervisors.put(key, supervisor);
        }
    }

    public void loadItems(@NotNull File file, @NotNull Set<String> loaded) {
        if (file.isDirectory()) {
            final File[] list = file.listFiles();
            if (list != null) {
                for (File child : list) {
                    loadItems(child, loaded);
                }
            }
            return;
        }

        final YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return;
        }

        for (String id : yaml.getKeys(false)) {
            if (loaded.contains(id)) {
                PixelBuy.log(2, "Duplicated store item id '" + id + "' from file " + file.getPath());
                continue;
            }
            loaded.add(id);

            final StoreItem item = new StoreItem(id);
            final ConfigurationSection section = yaml.getConfigurationSection(id);
            if (section != null) {
                final BukkitSettings config = BukkitSettings.of(section);
                if (baseItem != null) {
                    config.merge(baseItem);
                }
                item.onReload(config);
            }
            items.put(id, item);
        }
    }

    public void onDisable() {
        for (var entry : supervisors.entrySet()) {
            entry.getValue().onClose();
        }
        supervisors.clear();
        categories.clear();
    }

    @Nullable
    public Object get(@NotNull String field) {
        switch (field) {
            case "name":
                return name;
            case "group":
                return group;
            case "supervisor":
                return defaultSupervisor;
            case "categories":
                return String.join("\n", categories.keySet());
            case "categories_size":
                return categories.size();
            case "supervisors":
                return String.join("\n", supervisors.keySet());
            case "supervisors_size":
                return supervisors.size();
            case "items":
                return String.join("\n", items.keySet());
            case "items_size":
                return items.size();
            default:
                return null;
        }
    }

    @NotNull
    public SettingsFile getConfig() {
        return config;
    }

    @NotNull
    public Checkout getCheckout() {
        return checkout;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getGroup() {
        return group;
    }

    @NotNull
    public Set<String> getGroups() {
        final Set<String> groups = new HashSet<>();
        groups.add(group);
        for (var entry : getSupervisors().entrySet()) {
            groups.add(entry.getValue().getGroup());
        }
        return groups;
    }

    @NotNull
    public String getDefaultSupervisor() {
        return defaultSupervisor;
    }

    @Nullable
    public StoreCategory getCategory(@NotNull String id) {
        return categories.get(id);
    }

    @NotNull
    public Map<String, StoreCategory> getCategories() {
        return categories;
    }

    @Nullable
    public WebSupervisor getSupervisor(@NotNull String id) {
        return supervisors.get(id);
    }

    @NotNull
    public Map<String, WebSupervisor> getSupervisors() {
        return supervisors;
    }

    @Nullable
    public StoreItem getItem(@NotNull String id) {
        return items.get(id);
    }

    @Nullable
    public StoreItem getItem(@NotNull Predicate<StoreItem> condition) {
        for (Map.Entry<String, StoreItem> entry : items.entrySet()) {
            if (condition.test(entry.getValue())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @NotNull
    public Map<String, StoreItem> getItems() {
        return items;
    }

    public boolean isItem(@NotNull String id) {
        return getItem(id) != null;
    }

    public boolean registerAction(@NotNull String id, @NotNull StoreAction.Builder<?> builder) {
        return actionTypes.put(id, builder) != null;
    }

    public boolean unregisterAction(@NotNull String id) {
        return actionTypes.remove(id) != null;
    }

    public boolean unregisterAction(@NotNull StoreAction.Builder<?> builder) {
        return actionTypes.entrySet().removeIf(entry -> entry.getValue().equals(builder));
    }

    @Nullable
    public StoreAction buildAction(@NotNull String id, @Nullable Object object) {
        for (var entry : actionTypes.entrySet()) {
            if (entry.getValue().getPattern().matcher(id).matches()) {
                return entry.getValue().build(id, object);
            }
        }
        return null;
    }
}
