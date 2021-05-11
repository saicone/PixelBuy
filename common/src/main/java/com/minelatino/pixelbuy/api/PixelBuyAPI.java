package com.minelatino.pixelbuy.api;

import com.minelatino.pixelbuy.api.action.ActionExecutor;
import com.minelatino.pixelbuy.api.action.ActionType;
import org.jetbrains.annotations.NotNull;

public class PixelBuyAPI {

    private static ActionExecutor executor;

    public static void setupExecutor(@NotNull ActionExecutor ex) {
        executor = ex;
    }

    public static ActionExecutor getExecutor() {
        return executor;
    }

    /**
     * Register an action only if provided action doesn't exist in registered actions list.
     * @param action The action to register.
     * @return True if provided action has registered / False if not.
     */
    public static boolean registerAction(@NotNull ActionType action) {
        return executor.addAction(action);
    }

    /**
     * Unregister an action by type.
     * @param action The action to unregister.
     * @return True if provided action has unregistered / False if not.
     */
    public static boolean unregisterAction(@NotNull ActionType action) {
        return executor.removeAction(action);
    }

    /**
     * Unregister an action by identifier.
     * @param actionName The name of action.
     * @return True if provided action has unregistered / False if not.
     */
    public static boolean unregisterAction(@NotNull String actionName) {
        return executor.removeAction(actionName);
    }

    /**
     * Execute an action string for a player
     * @param player The player name.
     * @param action Action content.
     * @param online Need for the player to be online.
     * @return True if action are executed / False if not
     */
    public static boolean executeAction(@NotNull String player, @NotNull String action, boolean online) {
        return executor.execute(player, action, online);
    }

    /**
     * Execute an action without player.
     * @param action Action content.
     * @return True if action are executed / False if not
     */
    public static boolean executeAction(@NotNull String action) {
        return executor.execute(null, action, false);
    }
}
