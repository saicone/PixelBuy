---
sidebar_position: 3
title: Items
description: Information about PixelBuy store items.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

PixelBuy store items are the main feature from the plugin and also have an advanced configuration.

## Format

* The store items are stored on `.yml` files inside `plugins/PixelBuy/storeitems` folder.
* You can create any subfolders as you want and every item will be loaded.
* A single `.yml` file can hold multiple store items at the same time.
* Every store item must have a unique name.

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

The display configuration is how the item is shown on any gui.

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

For more information about display item format:

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/pixelbuy/usage/item-config/",
  label: "Item Configuration",
  description: "Information about PixelBuy item configuration format"
  }}
/>
```

## Configuration

The item configuration affects how the item is saved, displayed or delivered.

### Price

The item price can be a normal number or an advanced configured one by providing the product ID associated with web supervisor.

```yaml
# Simple configuration
super-pickaxe:
  price: 3.99

# Advanced configuration
super-pickaxe:
  price:
    default: 3.99
    web-supervisor-name: 1234 # <-- Product ID
```

It's suggested to use the advanced configuration.

### Category

A store item can be configured with an associated category or multiple categories.

```yaml
# Single category
creeper-hat:
  category: hats

# Multiple categories
super-pickaxe:
  categories:
    - items
    - pickaxes
```

### Options

The items can have multiple options to handle any interaction or delivery.

```yaml
super-pickaxe:
  options:
    online: true
    always-run: false
    append:
      - 'global'
```

* `online` - Set to true if the store item only can be given if the player is online, this option also affects the entire order where item belongs from to cancel order delivery until player is online.
* `always-run` - Set to true to give the item without being affected by any `online: true` option from other store items in the same order.
* `append` - Order group names to append this store item if the order contains the same item name but with a different group in relation to the current server group name.

## Execution

The execution configuration is a list of actions to execute on item delivery, the execution types are:

* `onBuy` - When an order is delivered for the first time.
* `onRefund` - When an order is refunded.
* `onRecover` - When an order is marked as recovery status, this means the item needs to be delivered one more time or not.

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

For a detailed list of actions, visit the actions page:

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/pixelbuy/action/",
  label: "PixelBuy actions",
  description: "Information about PixelBuy action format and types"
  }}
/>
```