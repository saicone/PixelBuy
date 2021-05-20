package com.minelatino.pixelbuy.api.object;

import com.minelatino.pixelbuy.PixelBuy;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Donations {

    private final int amount;
    private final Map<String, Double> top;

    public Donations(int amount, Map<String, Double> top) {
        this.amount = amount;
        this.top = top;
    }

    public int getAmount() {
        return amount;
    }

    public @NotNull Map<String, Double> getTop() {
        int max = PixelBuy.SETTINGS.getInt("Donations.TopSize");
        if (max < 1) return Collections.emptyMap();
        Map<String, Double> map = top.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(max)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        top.keySet().retainAll(map.keySet());
        return map;
    }

    public void handlePlayer(String player, double donated) {
        if (top.containsKey(player) || top.size() < PixelBuy.SETTINGS.getInt("Donations.TopSize")) {
            top.put(player, donated);
        } else {
            boolean put = false;
            for (Double i : top.values()) {
                if (donated > i) {
                    put = true;
                    break;
                }
            }
            if (put) {
                top.put(player, donated);
            }
        }
    }
}
