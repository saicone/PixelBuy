package com.minelatino.pixelbuy.api.action;

import com.minelatino.pixelbuy.api.action.type.GiveItemAction;
import com.minelatino.pixelbuy.util.PixelUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BukkitExecutor extends ActionExecutor {

    public BukkitExecutor() {
        super();
        actions.add(new GiveItemAction(""));
    }

    @Override
    public boolean execute(String player, boolean online, Object action) {
        if (action instanceof ConfigurationSection) {
            Object content = ((ConfigurationSection) action).get("content");
            if (content == null) return false;
            Object keys = ((ConfigurationSection) action).get("keys");
            return execute(player, online, ((ConfigurationSection) action).getName(),
                    (keys != null ? (keys instanceof ConfigurationSection ? getSectionKeys((ConfigurationSection) keys) : PixelUtils.getKeys(String.valueOf(keys), ";", ":")) : new HashMap<>()),
                    (content instanceof ConfigurationSection ? getSectionKeys((ConfigurationSection) content) : Collections.singletonMap("content", String.valueOf(content))));
        } else {
            return execute(player, online, String.valueOf(action));
        }
    }

    @Override
    public boolean execute(String player, boolean online, String actionName, Map<String, String> keys, Map<String, String> content) {
        if (content == null) return false;

        return false;
    }

    private Map<String, String> getSectionKeys(ConfigurationSection section) {
        Map<String, String> keys = new HashMap<>();
        section.getKeys(false).forEach(key -> keys.put(key.toLowerCase(), String.valueOf(section.get(key))));
        return keys;
    }
}
