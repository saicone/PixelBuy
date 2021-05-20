package com.minelatino.pixelbuy.api.object;

import com.minelatino.pixelbuy.PixelBuy;
import com.minelatino.pixelbuy.util.PixelUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class ServerData {

    private String name;
    private String displayName;
    private final Map<String, Donations> donations;

    public ServerData(String name, String displayName) {
        this(name, displayName, new HashMap<>());
    }

    public ServerData(String name, String displayName, Map<String, Donations> donations) {
        this.name = name;
        this.displayName = displayName;
        this.donations = donations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, Donations> getAllDonations() {
        return donations;
    }

    public Donations getDonations() {
        String date = new SimpleDateFormat("MM/yyyy").format(new Date(System.currentTimeMillis()));
        return donations.getOrDefault(date, getDonations0(date));
    }

    private Donations getDonations0(String date) {
        donations.put(date, new Donations(0, new HashMap<>()));
        checkLimit();
        return donations.get(date);
    }

    // TODO: Make a better limit checker
    private void checkLimit() {
        int max = PixelBuy.SETTINGS.getInt("Donations.MonthsSize");
        if (max < 0) return;

        if (donations.size() > max) {
            Map<Integer, List<Integer>> map = new HashMap<>();
            donations.keySet().forEach(key -> {
                String[] s = key.split("/", 2);
                if (s.length == 2) {
                    int month = PixelUtils.parseInt(s[0], 0);
                    int year = PixelUtils.parseInt(s[1], 0);
                    if (month != 0 && year != 0) {
                        if (map.containsKey(year)) {
                            map.get(year).add(month);
                        } else {
                            List<Integer> list = new ArrayList<>();
                            list.add(month);
                            map.put(year, list);
                        }
                    }
                }
            });
            List<Integer> years = new ArrayList<>(map.keySet());
            years.sort(Comparator.reverseOrder());
            int num = 0;

            List<String> allowed = new ArrayList<>();
            for (int year : years) {
                if (num < max) {
                    map.get(year).sort(Comparator.reverseOrder());
                    for (int month : map.get(year)) {
                        if (num < max) {
                            allowed.add(month + "/" + year);
                            num++;
                        } else break;
                    }
                } else break;
            }
            donations.keySet().retainAll(allowed);
        }
    }
}
