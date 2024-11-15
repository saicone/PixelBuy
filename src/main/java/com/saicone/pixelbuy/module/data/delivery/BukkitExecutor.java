package com.saicone.pixelbuy.module.data.delivery;

import com.saicone.delivery4j.util.DelayedExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BukkitExecutor implements DelayedExecutor<BukkitTask> {

    private final Plugin plugin;

    public BukkitExecutor(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull BukkitTask execute(@NotNull Runnable command) {
        return Bukkit.getScheduler().runTaskAsynchronously(this.plugin, command);
    }

    @Override
    public @NotNull BukkitTask execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, command, (long) (unit.toMillis(delay) * 0.02));
    }

    @Override
    public @NotNull BukkitTask execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, command, (long) (unit.toMillis(delay) * 0.02), (long) (unit.toMillis(period) * 0.02));
    }

    @Override
    public void cancel(@NotNull BukkitTask task) {
        task.cancel();
    }
}
