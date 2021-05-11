package com.minelatino.pixelbuy.api.action.type;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.api.action.ActionType;
import com.minelatino.pixelbuy.util.PixelUtils;

import java.util.Map;

public class MessageAction extends ActionType {

    public MessageAction(String regex) {
        super(regex);
    }

    @Override
    public String getName() {
        return "MESSAGE";
    }

    @Override
    public boolean executeOnline(Object player, Map<String, String> keys, String content) {
        String type = keys.getOrDefault("type", "CHAT").toUpperCase();
        if (type.equals("CHAT")) {
            for (String s : content.split("\\n")) {
                PixelBuy.LOCALE.sendMessage(player, s);
            }
            return true;
        }

        Map<String, String> subKeys = PixelUtils.getKeys(content, ";", ":");
        if (type.equals("TITLE")) {
            String title = subKeys.getOrDefault("title", "");
            String subtitle = subKeys.getOrDefault("subtitle", "");
            if (title.isEmpty() && subtitle.isEmpty()) return false;
            PixelBuy.LOCALE.sendTitle(player, title, subtitle, PixelUtils.parseInt(subKeys.getOrDefault("fadein", "20"), 20), PixelUtils.parseInt(subKeys.getOrDefault("stay", "20"), 20), PixelUtils.parseInt(subKeys.getOrDefault("fadeout", "20"), 20));
        } else if (type.equals("ACTIONBAR")) {
            String text = subKeys.getOrDefault("text", "");
            if (text.isEmpty()) return false;
            PixelBuy.LOCALE.sendActionbar(player, text, PixelUtils.parseInt(subKeys.getOrDefault("pulses", "1"), 1));
        } else {
            return false;
        }
        return true;
    }
}
