package com.minelatino.pixelbuy.api.object;

import java.util.Map;

public class ServerData {

    private String name;
    private String displayName;
    private final Map<String, Double> donations;
    private final Donations top;

    public ServerData(String name, String displayName, Map<String, Double> donations, Donations top) {
        this.name = name;
        this.displayName = displayName;
        this.donations = donations;
        this.top = top;
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

    public Map<String, Double> getDonations() {
        return donations;
    }

    public Donations getTop() {
        return top;
    }
}
