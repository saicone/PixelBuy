package com.minelatino.pixelbuy.module.locale.user;

import com.minelatino.pixelbuy.PixelBuy;

import java.util.UUID;

public final class PixelUser<T> {

    private final UserType<T> type;
    private final T user;

    private final boolean console;
    private final String name;
    private final UUID uuid;

    public PixelUser(UserType<T> type, T user) {
        this.type = type;
        this.user = user;
        console = type.isConsole(user);
        name = type.getName(user);
        uuid = type.getUniqueId(user);
    }

    public boolean isConsole() {
        return console;
    }

    public String getName() {
        return name;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean hasPermission(String permission) {
        if (isConsole()) return true;
        return type.hasPermission(user, permission);
    }

    public void sendMessage(String msg, String... args) {
        type.sendMessage(user, msg, args);
    }

    public void sendPath(String path, String... args) {
        PixelBuy.LOCALE.sendTo(user, path, args);
    }

    public void sendTitle(String path, String... args) {
        PixelBuy.LOCALE.sendTitle(user, path, args);
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, String... args) {
        PixelBuy.LOCALE.sendTitle(user, title, subtitle, fadeIn, stay, fadeOut, args);
    }

    public void sendActionbar(String path, String... args) {
        PixelBuy.LOCALE.sendActionbar(user, path, args);
    }

    public void sendActionbar(String text, int pulses, String... args) {
        PixelBuy.LOCALE.sendActionbar(user, text, pulses, args);
    }
}
