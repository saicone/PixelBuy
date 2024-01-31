---
sidebar_position: 1
title: API
description: PixelBuy API usage.
---

PixelBuy contains multiple methods that can be accessed from it's API.

## Add PixelBuy to your plugin

There is not dependency documentation.

## Examples

### Load actions

Load action from any object.

```java
Object obj = ...;
List<StoreAction> actions = PixelBuyAPI.buildActions(obj);
```

### Register action

Register action to be used on item executions.

```java
public class MyAction extends StoreAction {
    @Override
    public void run(@NotNull StoreClient client) {
        // do something
    }
}

Builder<MyAction> builder = new Builder<MyAction>("(?i)myaction").accept(config -> {
    // build action
});

PixelBuyAPI.registerAction("myaction", builder);
```

### Plugin events

The `OrderProcessEvent` runs before process any order.

```java
@EventHandler
public void onOrder(OrderProcessEvent event) {
    // do something
}
```