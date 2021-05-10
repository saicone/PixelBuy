package com.minelatino.pixelbuy.api.action;

import java.util.Map;

public abstract class ActionType<P, O> {

    public abstract String getName();

    public void executeOnline(P player, Map<String, String> keys, String content) {
        execute(keys, content);
    }

    public void executeOffline(O offlinePlayer, Map<String, String> keys, String content) {
        execute(keys, content);
    }

    public void execute(Map<String, String> keys, String content) {

    }
}
