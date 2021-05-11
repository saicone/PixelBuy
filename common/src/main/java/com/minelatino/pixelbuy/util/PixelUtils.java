package com.minelatino.pixelbuy.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.minelatino.pixelbuy.PixelBuy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PixelUtils {

    private static final Gson gson = new Gson();

    public static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static Map<String, String> getKeys(String reference, String mainSplit, int mainMax, String subSplit) {
        return getKeys(reference.split(mainSplit, mainMax), subSplit);
    }

    public static Map<String, String> getKeys(String reference, String mainSplit, String subSplit) {
        return getKeys(reference.split(mainSplit), subSplit);
    }

    public static Map<String, String> getKeys(List<String> reference, String split) {
        return getKeys((String[]) reference.toArray(), split);
    }

    public static Map<String, String> getKeys(String[] reference, String split) {
        Map<String, String> map = new HashMap<>();
        for (String s : reference) {
            String[] s0 = s.split(split, 2);
            map.put(s0[0].toLowerCase(), (s.length() > 1 ? s0[1] : ""));
        }
        return map;
    }

    public static Map<String, String> getJsonKeys(String json) {
        try {
            return getJsonKeys(gson.fromJson(json, JsonObject.class));
        } catch (JsonSyntaxException e) {
            PixelBuy.LOCALE.log(0, "Action.Error.JsonSyntax", json, e.getMessage());
            return new HashMap<>();
        }
    }

    public static Map<String, String> getJsonKeys(JsonObject json) {
        Map<String, String> map = new HashMap<>();

        for (String s : json.keySet()) {
            JsonElement element = json.get(s);

            if (element instanceof JsonObject) {
                Map<String, String> map0 = getJsonKeys((JsonObject) element);
                map0.forEach((key, string) -> map.put(s + "." + key, string));
            } else {
                map.put(s, element.getAsString());
            }
        }

        return map;
    }
}
