package com.saicone.pixelbuy.core.store.action;

import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.module.hook.Placeholders;
import com.saicone.pixelbuy.util.MStrings;
import com.saicone.pixelbuy.util.OptionalType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BroadcastAction extends MessageAction {

    public static final Builder<BroadcastAction> BUILDER = new Builder<BroadcastAction>("(?i)broadcast(-?messages?)?")
            .accept(config -> {
                final List<String> message = config.getRegex("(?i)(value|msg|message)s?").asList(type -> {
                    final String s = type.asString();
                    return s == null ? null : MStrings.color(s);
                });
                final OptionalType width = config.getRegex("(?i)center(ed)?(-?text)?|chat-?width");
                final int center;
                if (width.getValue() instanceof Boolean) {
                    center = (Boolean) width.getValue() ? 0 : -1;
                } else {
                    center = width.asInt(-1);
                }
                final boolean colored = config.getRegex("(?i)color(ed)?(-?after)?").asBoolean(false);
                final boolean viewerParsable = config.getRegex("(?i)(viewer-?)?parse(able)?").asBoolean(false);
                return new BroadcastAction(message, center, colored, viewerParsable);
            });

    private final boolean viewerParsable;

    public BroadcastAction(@NotNull List<String> message, int center, boolean colored, boolean viewerParsable) {
        super(message, center, colored);
        this.viewerParsable = viewerParsable;
    }

    public boolean isViewerParsable() {
        return viewerParsable;
    }

    @Override
    public void run(@NotNull StoreClient client) {
        final List<String> msg = client.parse(getMessage());
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String s : msg) {
                if (isViewerParsable()) {
                    s = Placeholders.parseBracket(player, s);
                }
                player.sendMessage(parse(s));
            }
        }
    }
}
