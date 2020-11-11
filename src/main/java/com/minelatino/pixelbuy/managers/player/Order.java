package com.minelatino.pixelbuy.managers.player;

import java.util.List;

public class Order {

    private final Integer id;
    private final List<String> cmds;

    public Order(Integer id, List<String> cmds) {
        this.id = id;
        this.cmds = cmds;
    }

    public Integer getId() {
        return id;
    }

    public List<String> getCmds() {
        return cmds;
    }
}
