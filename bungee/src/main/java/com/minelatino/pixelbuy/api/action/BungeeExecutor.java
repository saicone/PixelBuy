package com.minelatino.pixelbuy.api.action;

import java.util.Map;

public class BungeeExecutor extends ActionExecutor {

    public BungeeExecutor() {
        super();
    }

    @Override
    public boolean execute(String player, boolean online, String actionName, Map<String, String> keys, String content) {
        return false;
    }
}
