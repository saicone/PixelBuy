package com.saicone.pixelbuy.api.store;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class StoreOrder {

    private static final int EXECUTION_SIZE = Execution.values().length;

    private int dataId = -1;
    private final String provider;
    private final int id;
    // Main group used to identify order precedence
    private final String group;
    private UUID buyer;
    private final LocalDate[] dates = new LocalDate[EXECUTION_SIZE];
    private Execution execution = Execution.BUY;
    private final Map<String, Set<Item>> items = new HashMap<>();

    private transient boolean edited;

    public StoreOrder(@NotNull String provider, int id, @NotNull String group) {
        this.provider = provider;
        this.id = id;
        this.group = group;
        this.dates[0] = LocalDate.now();
    }

    @Nullable
    public Object get(@NotNull String field) {
        switch (field) {
            case "data_id":
                return dataId;
            case "provider":
                return provider;
            case "id":
                return id;
            case "group":
                return group;
            case "buyer":
                return buyer;
            case "buyer_id":
                return buyer.toString().replace('-', '\0');
            case "date":
                return getDate();
            case "date_buy":
                return dates[Execution.BUY.ordinal()];
            case "date_recover":
                return dates[Execution.RECOVER.ordinal()];
            case "date_refund":
                return dates[Execution.REFUND.ordinal()];
            case "execution":
                return execution.name();
            case "items":
                return items.size();
            default:
                return null;
        }
    }

    public int getDataId() {
        return dataId;
    }

    @NotNull
    public String getKey() {
        return provider + ":" + id + ":" + group;
    }

    @NotNull
    public String getProvider() {
        return provider;
    }

    public int getId() {
        return id;
    }

    @NotNull
    public String getGroup() {
        return group;
    }

    @Nullable
    public UUID getBuyer() {
        return buyer;
    }

    @NotNull
    public LocalDate getDate() {
        return dates[execution.ordinal()];
    }

    @Nullable
    public LocalDate getDate(@NotNull Execution execution) {
        return dates[execution.ordinal()];
    }

    @NotNull
    public LocalDate[] getDates() {
        return dates;
    }

    @NotNull
    public Execution getExecution() {
        return execution;
    }

    @NotNull
    public Set<Item> getItems() {
        return items.getOrDefault(group, Set.of());
    }

    @NotNull
    public Set<Item> getItems(@NotNull String group) {
        return items.getOrDefault(group, Set.of());
    }

    @NotNull
    public Map<String, Set<Item>> getAllItems() {
        return items;
    }

    public boolean isEdited() {
        return edited;
    }

    public boolean has(@NotNull State state) {
        for (var entry : items.entrySet()) {
            for (Item item : entry.getValue()) {
                if (item.getState() == state) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public void setBuyer(@NotNull UUID buyer) {
        this.edited = true;
        this.buyer = buyer;
    }

    public void setDate() {
        setDate(LocalDate.now());
    }

    public void setDate(@NotNull LocalDate date) {
        setDate(execution, date);
    }

    public void setDate(@NotNull Execution execution, @NotNull LocalDate date) {
        setDate(execution.ordinal(), date);
    }

    public void setDate(int ordinal, @NotNull LocalDate date) {
        this.edited = true;
        if (ordinal < dates.length) {
            this.dates[ordinal] = date;
        }
    }

    public void setExecution(@NotNull Execution execution) {
        this.execution = execution;
        setDate();
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    @NotNull
    public Item addItem(@NotNull Item item) {
        return addItem(group, item);
    }

    @NotNull
    public Item addItem(@NotNull String group, @NotNull Item item) {
        this.edited = true;
        items.computeIfAbsent(group, __ -> new HashSet<>()).add(item);
        return item;
    }

    @NotNull
    public Item addItem(@NotNull String id) {
        return addItem(new Item(id));
    }

    @NotNull
    public Item addItem(@NotNull String group, @NotNull String id) {
        return addItem(group, new Item(id));
    }

    @NotNull
    public Item addItem(@NotNull String id, float price) {
        return addItem(new Item(id, price));
    }

    @NotNull
    public Item addItem(@NotNull String group, @NotNull String id, float price) {
        return addItem(group, new Item(id, price));
    }

    public void merge(@NotNull StoreOrder order) {
        edited = true;
        if (dataId < 1) {
            dataId = order.dataId;
        }
        if (order.buyer != null) {
            buyer = order.buyer;
        }
        for (int i = 0; i < dates.length; i++) {
            if (dates[i] == null) {
                dates[i] = order.dates[i];
            }
        }
        execution = order.execution;
        for (var entry : order.items.entrySet()) {
            if (items.containsKey(entry.getKey())) {
                items.get(entry.getKey()).addAll(entry.getValue());
            } else {
                items.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoreOrder that = (StoreOrder) o;

        if (id != that.id) return false;
        if (!provider.equals(that.provider)) return false;
        return group.equals(that.group);
    }

    @Override
    public int hashCode() {
        int result = provider.hashCode();
        result = 31 * result + id;
        result = 31 * result + group.hashCode();
        return result;
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

        private String error;

        public Item(@NotNull String id) {
            this(id, 0.0f);
        }

        public Item(@NotNull String id, float price) {
            this.id = id;
            this.price = price;
        }

        @Nullable
        public Object get(@NotNull String field) {
            switch (field) {
                case "id":
                    return id;
                case "price":
                    return price;
                case "state":
                    return state;
                case "error":
                    return getError();
                default:
                    return null;
            }
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
        public Item error(@Nullable String error) {
            this.error = error == null ? null : Base64.getEncoder().encodeToString(error.getBytes(StandardCharsets.UTF_8));
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof String) return id.equals((String) o);
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
