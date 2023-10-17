package com.saicone.pixelbuy.api.store;

import com.saicone.pixelbuy.module.hook.Placeholders;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StoreClient {

    private final OfflinePlayer offlinePlayer;

    private Function<String, String> parser;

    public StoreClient(@NotNull OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
    }

    @NotNull
    @Contract("_ -> this")
    public StoreClient parser(@NotNull Function<String, String> parser) {
        if (this.parser == null) {
            this.parser = parser;
        } else {
            this.parser = this.parser.andThen(parser);
        }
        return this;
    }

    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    @NotNull
    public Player getPlayer() {
        final Player player = offlinePlayer.getPlayer();
        if (player != null) {
            return player;
        }
        throw new RuntimeException("Cannot get the online player representation");
    }

    public boolean isOnline() {
        return offlinePlayer.isOnline();
    }

    public boolean isBedrockPlayer() {
        return offlinePlayer.getUniqueId().toString().startsWith("00000000-0000-0000");
    }

    @Nullable
    @Contract("!null -> !null")
    public String parse(@Nullable String s) {
        String finalString = s;
        if (parser != null) {
            finalString = parser.apply(finalString);
        }
        return Placeholders.parse(offlinePlayer, finalString);
    }

    @NotNull
    public List<String> parse(@NotNull List<String> list) {
        final List<String> finalList = new ArrayList<>();
        for (String s : list) {
            finalList.add(parse(s));
        }
        return finalList;
    }
}
