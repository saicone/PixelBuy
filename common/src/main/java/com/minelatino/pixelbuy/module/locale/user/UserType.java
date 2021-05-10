package com.minelatino.pixelbuy.module.locale.user;

import java.util.UUID;

public abstract class UserType<T> {

    UUID CONSOLE_UUID = new UUID(0, 0);

    abstract String getName(T user);

    abstract UUID getUniqueId(T user);

    abstract boolean hasPermission(T user, String permission);

    abstract void sendMessage(T user, String msg, String... args);

    public final PixelUser<?> of(T user) {
        return new PixelUser<>(this, user);
    }
}
