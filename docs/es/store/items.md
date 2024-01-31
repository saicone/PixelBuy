---
sidebar_position: 3
title: Items
description: Información sobre los items de la tienda de PixelBuy.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

Los items de la tienda en PixelBuy son la característica principal del plugin además de que tiene una configuración avanzada.

## Formato

* Los items de la tienda se guardan en archivos `.yml` dentro de la carpeta `plugins/PixelBuy/storeitems`.
* Puedes crear cuantas subcarpetas quieras y todos los items dentro serán cargados.
* Un archivo `.yml` puede contener varios items de la tienda al mismo tiempo.
* Todos los items de la tienda deben tener un nombre único.

```yaml
# plugins/PixelBuy/storeitems/default.yml
myStoreItem:
  configuration: key

otherStoreItem:
  configuration: key


# plugins/PixelBuy/storeitems/swords.yml
coolSword:
  configuration: key

otherSword:
  configuration: key


# plugins/PixelBuy/storeitems/hats/magic-hats.yml
thunderHat:
  configuration: key

creeperHat:
  configuration: key
```

## Display

La configuración sobre como se mostrará el item en cualquier menú.

```yaml
super-pickaxe:
  display:
    material: DIAMOND_PICKAXE
    name: '&9&lSuper Pickaxe'
    lore:
      - ''
      - '&7Price&8: &a{store_item_price}'
      - ''
      - '&7Get on the server store'
```

Para más información sobre el formato de configuración de items:

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/es/pixelbuy/usage/item-config/",
  label: "Configuración de item",
  description: "Información sobre el formato para configurar items en PixelBuy"
  }}
/>
```

## Configuración

La configuración del item afecta la forma en como es guardado, mostrado o entregado.

### Precio

El precio del item puede ser un númer normal o una configuración avanzada al proveer un id de producto asociado con el supervisor web.

```yaml
# Configuración simple
super-pickaxe:
  price: 3.99

# Configuración avanzada
super-pickaxe:
  price:
    default: 3.99
    web-supervisor-name: 1234 # <-- ID del producto
```

Es sugerido utilizar la configuración avanzada.

### Categoría

Un item de la tienda puede ser configurado utilizando una categoría asociada o múltiples categorías.

```yaml
# Una sola categoría
creeper-hat:
  category: hats

# Múltiples categorías
super-pickaxe:
  categories:
    - items
    - pickaxes
```

### Opciones

Los items tienen múltiples opciones para manejar cualquier interacción o entrega.

```yaml
super-pickaxe:
  options:
    online: true
    always-run: false
    append:
      - 'global'
```

* `online` - Ponlo en true si el item de la tienda solamente puede ser entregado si el jugador está online, esta opción también afecta toda la órden de compra a donde el item pertenece al punto de cancelar la entrega o guardado de la orden de compra hasta que el jugador se conecte.
* `always-run` - Ponlo en true para entregar el item sin ser afectado por cualquier otro item cuya configuración incluya el `online: true` en la misma órden de compra.
* `append` - Los nombres de grupo de servidor donde el item será agregado a las órdenes de compra que contenga el mismo item pero con un grupo de servidor diferente al grupo de servidor actual de la configuración de la tienda.

## Ejecución

La configuración de la ejecución es una lista de acciones para ejecutar cuando el item es entregado, los tipos de ejecución son:

* `onBuy` - Cuando una órden de compra es entregada por primera vez.
* `onRefund` - Cuando una órden de compra es devuelta.
* `onRecover` - Cando una órden de compra es marcada con un estado de recuperación, esto significa que el item debe ser entregado más de una vez o no.

```yaml
super-pickaxe:
  onBuy:
    - item:
        material: DIAMOND_PICKAXE
        amount: 1
        name: '&9&lSuper Pickaxe'
        lore:
          - '&7Custom pickaxe'
          - '&7from server store'
          - ''
          - '&6Buyer: &7%player_name%'
          - '&6Order ID: &7{order_id}'
        enchants:
          SHARPNESS: 10
          DIG_SPEED: 5
          LOOT_BONUS_BLOCKS: 20
    - message: '&e&lYou bought the &9&lSuper Pickaxe&e!'
    - broadcast:
        - '&e-----------------------------'
        - '&fThe player &e{user_name} &fbought the super pickaxe!'
        - '&e-----------------------------'
  onRefund:
    - command: 'ban {user_name} You cannot refund this item'
```

Para información detallada sobre las acciones, visita la página de acciones:

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/es/pixelbuy/action/",
  label: "Acciones de PixelBuy",
  description: "Información sobre las acciones de PixelBuy"
  }}
/>
```