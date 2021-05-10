package com.minelatino.pixelbuy.api;

import com.minelatino.pixelbuy.api.action.ActionExecutor;
import com.minelatino.pixelbuy.api.action.ActionType;
import org.jetbrains.annotations.NotNull;

public class PixelBuyAPI {

    private static ActionExecutor executor;

    public static void setupExecutor(@NotNull ActionExecutor ex) {
        executor = ex;
    }

    /**
     * Register an action only if provided action doesn't exist in registered actions list.
     * @param action The action to register.
     * @return True if provided action has registered / False if not.
     */
    public static boolean registerAction(@NotNull ActionType<?, ?> action) {
        return executor.addAction(action);
    }

    /**
     * Unregister an action by type.
     * @param action The action to unregister.
     * @return True if provided action has unregistered / False if not.
     */
    public static boolean unregisterAction(@NotNull ActionType<?, ?> action) {
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
     * @param action Action content
     */
    public static void executeAction(@NotNull String player, @NotNull String action) {
        executor.execute(player, action);
    }

    /**
     * Execute an action without player.
     * @param action Action content
     */
    public static void executeAction(@NotNull String action) {
        executor.execute(null, action);
    }
}
