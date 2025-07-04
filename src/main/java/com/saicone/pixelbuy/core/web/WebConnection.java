package com.saicone.pixelbuy.core.web;

import com.saicone.pixelbuy.PixelBuy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface WebConnection {

    String USER_AGENT = "PixelBuy/" + PixelBuy.get().getDescription().getVersion() + " (+https://github.com/saicone/PixelBuy)";

    @Nullable
    <T> T fetch(@NotNull Class<T> contentType, @NotNull Object... args) throws IOException;

    @Nullable
    <T> T send(@NotNull Class<T> responseType, @NotNull Object object, @NotNull Object... args) throws IOException;

}
