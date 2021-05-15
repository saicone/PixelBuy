package com.minelatino.pixelbuy.api.action;

import com.minelatino.pixelbuy.api.action.type.BroadcastAction;
import com.minelatino.pixelbuy.api.action.type.MessageAction;
import com.minelatino.pixelbuy.util.PixelUtils;

import java.util.*;

public abstract class ActionExecutor {

    final List<ActionType> actions = new ArrayList<>();

    public ActionExecutor(){
        actions.add(new BroadcastAction(""));
        actions.add(new MessageAction(""));
    }

    public List<ActionType> getActions() {
        return actions;
    }

    public boolean addAction(ActionType action) {
        if (actions.contains(action)) return false;
        boolean exists = false;
        for (ActionType act : actions) {
            if (act.getName().toUpperCase().equals(action.getName().toUpperCase())) {
                exists = true;
            }
        }
        if (exists) return false;
        actions.add(action);
        return true;
    }

    public boolean removeAction(ActionType action) {
        if (!actions.contains(action)) return false;
        actions.remove(action);
        return true;
    }

    public boolean removeAction(String actionName) {
        int index = -1;
        for (ActionType act : actions) {
            if (act.getName().toUpperCase().equals(actionName.toUpperCase())) {
                index = actions.indexOf(act);
            }
        }
        if (index < 0) return false;
        actions.remove(index);
        return true;
    }

    public boolean execute(String player, boolean online, Object action) {
        return execute(player, online, String.valueOf(action));
    }

    public boolean execute(String player, boolean online, String action) {
        String[] act = action.split("=", 2);
        if (act.length == 2) {
            String[] name = act[0].split("\\|", 2);
            Map<String, String> keys = (name.length > 1 ? PixelUtils.getKeys(name[1].trim(), ";", ":") : new HashMap<>());
            name[0] = name[0].trim();
            act[1] = act[1].trim();
            if (name[0].isEmpty() || act[1].isEmpty()) {
                return false;
            } else {
                return execute(player, online, name[0], keys, Collections.singletonMap("content", act[1]));
            }
        }
        return false;
    }

    public abstract boolean execute(String player, boolean online, String actionName, Map<String, String> keys, Map<String, String> content);
}
