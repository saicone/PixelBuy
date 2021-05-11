package com.minelatino.pixelbuy.api.action;

import com.minelatino.pixelbuy.api.action.type.GiveItemAction;

import java.util.Map;

public class BukkitExecutor extends ActionExecutor {

    public BukkitExecutor() {
        super();
        actions.add(new GiveItemAction(""));
    }

    @Override
    public boolean execute(String player, boolean online, String actionName, Map<String, String> keys, String content) {
        return false;
    }
}