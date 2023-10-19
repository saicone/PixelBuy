package com.saicone.pixelbuy.api.store;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class StoreOrder {

    private final int id;
    private final Set<Item> items = new HashSet<>();

    public StoreOrder(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @NotNull
    public Set<Item> getItems() {
        return items;
    }

    public boolean has(@NotNull State state) {
        for (Item item : items) {
            if (item.getState() == state) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public Item addItem(@NotNull Item item) {
        items.add(item);
        return item;
    }

    @NotNull
    public Item addItem(@NotNull String id) {
        return addItem(new Item(id));
    }

    @NotNull
    public Item addItem(@NotNull String id, float price) {
        return addItem(new Item(id, price));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoreOrder that = (StoreOrder) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public enum State {
        DONE,
        PENDING,
        ERROR
    }

    public enum Execution {
        BUY,
        RECOVER,
        REFUND
    }

    public static class Item {
        private final String id;
        private float price;
        private State state = State.PENDING;
        private Execution execution = Execution.BUY;

        private String error;

        public Item(@NotNull String id) {
            this(id, 0.0f);
        }

        public Item(@NotNull String id, float price) {
            this.id = id;
            this.price = price;
        }

        @NotNull
        public String getId() {
            return id;
        }

        public float getPrice() {
            return price;
        }

        @NotNull
        public State getState() {
            return state;
        }

        @NotNull
        public Execution getExecution() {
            return execution;
        }

        @Nullable
        public String getError() {
            return error == null ? null : new String(Base64.getDecoder().decode(error));
        }

        @NotNull
        @Contract("_ -> this")
        public Item price(float price) {
            this.price = price;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Item state(@NotNull State state) {
            this.state = state;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Item execution(@NotNull Execution execution) {
            this.execution = execution;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Item error(@Nullable String error) {
            this.error = error == null ? null : Base64.getEncoder().encodeToString(error.getBytes(StandardCharsets.UTF_8));
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            return id.equals(item.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
