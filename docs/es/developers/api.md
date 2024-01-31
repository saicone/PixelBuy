---
sidebar_position: 1
title: API
description: Uso de la API de PixelBuy.
---

PixelBuy tiene múltiples métodos que pueden ser obtenidos utilizando su API.

## Agregar la API de PixelBuy a tu plugin.

No hay información sobre la dependencia.

## Ejemplos

### Cargar acciones

Cargar acciones utilizando cualquier objeto.

```java
Object obj = ...;
List<StoreAction> actions = PixelBuyAPI.buildActions(obj);
```

### Registrar una acción

Registrar una acción que podrá ser utilizada en las ejecuciones de los items de la tienda.

```java
public class MyAction extends StoreAction {
    @Override
    public void run(@NotNull StoreClient client) {
        // hacer algo
    }
}

Builder<MyAction> builder = new Builder<MyAction>("(?i)myaction").accept(config -> {
    // construir la acción
});

PixelBuyAPI.registerAction("myaction", builder);
```

### Eventos del plugin

El evento `OrderProcessEvent` es ejecutado antes de procesar cualquier órden de compra.

```java
@EventHandler
public void onOrder(OrderProcessEvent event) {
    // hacer algo
}
```