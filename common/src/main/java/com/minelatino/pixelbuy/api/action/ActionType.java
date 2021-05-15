package com.minelatino.pixelbuy.api.action;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ActionType {

    private final Pattern pattern;

    public ActionType(String regex) {
        pattern = Pattern.compile(regex);
    }

    public abstract String getName();

    public boolean match(String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public boolean executeOnline(Object player, Map<String, String> keys, Map<String, String> content) {
        return execute(keys, content);
    }

    public boolean executeOffline(Object offlinePlayer, Map<String, String> keys, Map<String, String> content) {
        return execute(keys, content);
    }

    public boolean execute(Map<String, String> keys, Map<String, String> content) {
        return false;
    }
}
