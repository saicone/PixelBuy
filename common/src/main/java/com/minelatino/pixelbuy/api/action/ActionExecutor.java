package com.minelatino.pixelbuy.api.action;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.minelatino.pixelbuy.PixelBuy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ActionExecutor {

    private final Gson gson = new Gson();
    private final List<ActionType<?, ?>> actions = new ArrayList<>();

    private Map<String, String> getJsonKeys(JsonObject json) {
        Map<String, String> map = new HashMap<>();

        for (String s : json.keySet()) {
            JsonElement element = json.get(s);

            if (element instanceof JsonObject) {
                Map<String, String> map0 = getJsonKeys((JsonObject) element);
                map0.forEach((key, string) -> map.put(s + "." + key, string));
            } else {
                map.put(s, element.getAsString());
            }
        }

        return map;
    }

    public List<ActionType<?, ?>> getActions() {
        return actions;
    }

    public boolean addAction(ActionType<?, ?> action) {
        if (actions.contains(action)) return false;
        boolean exists = false;
        for (ActionType<?, ?> act : actions) {
            if (act.getName().toUpperCase().equals(action.getName().toUpperCase())) {
                exists = true;
            }
        }
        if (exists) return false;
        actions.add(action);
        return true;
    }

    public boolean removeAction(ActionType<?, ?> action) {
        if (!actions.contains(action)) return false;
        actions.remove(action);
        return true;
    }

    public boolean removeAction(String actionName) {
        int index = -1;
        for (ActionType<?, ?> act : actions) {
            if (act.getName().toUpperCase().equals(actionName.toUpperCase())) {
                index = actions.indexOf(act);
            }
        }
        if (index < 0) return false;
        actions.remove(index);
        return true;
    }

    public void execute(String player, String action) {
        String[] act = action.split("=", 2);
        if (act.length == 2) {
            String[] name = act[0].split("\\{", 2);
            Map<String, String> keys;
            if (name.length > 1) {
                try {
                    keys = getJsonKeys(gson.fromJson(name[1], JsonObject.class));
                } catch (JsonSyntaxException e) {
                    PixelBuy.LOCALE.log(0, "Action.Error.JsonSyntax", name[1], e.getMessage());
                    keys = new HashMap<>();
                }
            } else {
                keys = new HashMap<>();
            }
            execute(player, name[0], keys, act[1]);
        }
    }

    public abstract void execute(String player, String actionName, Map<String, String> keys, String content);
}
