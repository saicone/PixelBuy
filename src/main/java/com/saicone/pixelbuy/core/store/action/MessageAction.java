package com.saicone.pixelbuy.core.store.action;

import com.saicone.pixelbuy.api.store.StoreAction;
import com.saicone.pixelbuy.api.store.StoreClient;
import com.saicone.pixelbuy.util.MStrings;
import com.saicone.pixelbuy.util.OptionalType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.StringJoiner;

public class MessageAction extends StoreAction {

    public static final Builder<MessageAction> BUILDER = new Builder<MessageAction>("(?i)(send-?)?messages?")
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
                return new MessageAction(message, center, colored);
            });

    private final List<String> message;
    private final int center;
    private final boolean colored;

    public MessageAction(@NotNull List<String> message, int center, boolean colored) {
        this.message = message;
        this.center = center;
        this.colored = colored;
    }

    @NotNull
    public List<String> getMessage() {
        return message;
    }

    public boolean isCentered() {
        return center >= 0;
    }

    public boolean isColored() {
        return colored;
    }

    @Override
    public void run(@NotNull StoreClient client) {
        if (client.isOnline()) {
            final Player player = client.getPlayer();
            final List<String> msg = client.parse(getMessage());
            for (String s : msg) {
                player.sendMessage(parse(s));
            }
        }
    }

    @NotNull
    protected String parse(@NotNull String s) {
        if (isColored()) {
            s = MStrings.color(s);
        }
        if (center == 0) {
            s = MStrings.centerText(s);
        } else if (center >= 0) {
            s = MStrings.centerText(s, center);
        }
        return s;
    }

    @Override
    public String toString() {
        final StringJoiner joiner = new StringJoiner(MStrings.COLOR_CHAR + "f, ", MStrings.COLOR_CHAR + "f[", MStrings.COLOR_CHAR + "f]");
        for (String s : getMessage()) {
            joiner.add(s);
        }
        return joiner.toString();
    }
}
