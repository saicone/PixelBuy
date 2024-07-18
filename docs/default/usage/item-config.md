---
sidebar_position: 5
title: Item Configuration
description: Information about PixelBuy item configuration format.
---

Any item configured on PixelBuy uses the same format which is quite simple.

## Usage

Using common tags you can make for example a diamond with custom name:

```yaml
material: DIAMOND
amount: 3
name: '&eCustom name'
```

## Format

PixelBuy uses XSeries library to serialize items with XItemStack format.

### Item

The basic item tags to build it properly, only `material` is required.

```yaml
material: DIAMOND
amount: 1
damage: 1
unbreakable: false
```

* `material` - The item material.
* `amount` - The item amount.
* `damage` - The damage value data.
* `unbreakable` (Only MC +1.11) - Set true to make the item unbreakable.

:::tip

The `unbreakable` tag was introduced on Minecraft 1.7, but Bukkit API only allows editing it since MC 1.11, you can still set an unbreakable state using item NBT configuration below this page.

:::

### Display

Any display related configuration.

```yaml
name: '&eCustom name'
lore:
  - '&7Some custom'
  - '&alore lines'
flags:
  - HIDE_ATTRIBUTES
glow: false
custom-model-data: 40
```

* `name` - The item display name.
* `lore` - The item display lore.
* `flags` - The display item flags, use `ALL` to set all item flags.
* `glow` - Set true to make item glow (It also adds a `Durability I` to the item with `HIDE_ENCHANTS` flag).
* `custom-model-data` (Only MC +1.14) - The custom model data from texture pack used for this item.

### Attributes

:::info

This feature is only for MC 1.13 or higher.

:::

The attribute modifiers used on item, a list of available attributes can be found on [Spigot Javadocs](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html).

```yaml
attributes:
  GENERIC_ATTACK_DAMAGE:
    id: '7ca003dc-175f-4f1f-b490-5651045311ad'
    name: generic.attack_damage
    amount: 8.0
    operation: ADD_NUMBER
    slot: HAND
```

* `id` - Is the unique id for the attribute modifier (is not required).
* `name` - Is the name of the attribute.
* `amount` - Is the base amount used on the operation.
* `operation` - The operation used in the attribute, a list of available operation can be found on [Spigot Javadocs](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/AttributeModifier.Operation.html).
* `slot` - Is the equipment slot where attribute modifier takes effect, can be `CHEST`, `FEET`, `HAND`, `HEAD`, `LEGS` or `OFF_HAND`.

### Enchantments

The current enchantments on item.

```yaml
enchants:
  DURABILITY: 3
stored-enchants:
  LURE: 2
```

* `enchants` - The enchantments to apply on item.
* `stored-enchants` - Same as above, but only applies for enchanted books.

### Textured head

Adds a texture to current item if it's a `PLAYER_HEAD` material.

```yaml
skull: Rubenicos
```

The texture can be `player unique id`, `player name`, `base64 texture`, `mojang url` or `texture hash`.

### Special items

There is a bunch of special items that can be configured using XItemStack format, but for now it's not covered by this wiki, take a look into [XItemStack source code](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XItemStack.java) for more information.

## Providers

You can make an advanced material configuration to get an item from other plugins.

### Oraxen

Get item from Oraxen plugin.

```yaml
# Simple configuration
material: 'oraxen:storm_sword'

# Advanced configuration
material:
  provider: oraxen
  id: storm_sword
```

### MMOItems

Get item from MMOItems plugin.

```yaml
# Simple configuration
material: 'mmoitems:SWORD:lava_sword'

# Advanced configuration
material:
  provider: mmoitems
  type: SWORD
  id: lava_sword
```

### ItemsAdder

Get item from ItemsAdder plugin.

```yaml
# Simple configuration
material: 'itemsadder:custom_item'

# Advanced configuration
material:
  provider: itemsadder
  id: custom_item
```

## Append

Append some display information to item after building it, this is useful if you want an item from another plugin and also add some lines to item lore.

### Name

Append to item display name.

```yaml
# Simple configuration
append:
  name.before: '&eText before'
  name.after: '&bText after'

# Advanced configuration
append:
  name:
    before: '&eText before'
    after: '&bText after'
```

### Lore

Append to item display lore.

```yaml
# Simple configuration
append:
  lore.before:
    - '&eLine before'
  lore.after:
    - '&bLine after'

# Advanced configuration
append:
  lore:
    before:
      - '&eLine before'
    after:
      - '&bLine after'
```

## NBT

It's the most advanced feature to edit item tags directly from internal data.

```yaml
nbt:
  # Make item unbreakable on versions older than 1.11
  Unbreakable: true
  # Set item repair cost on anvils
  RepairCost: 10
  # And also add anything here
  some:
    custom:
      tag: 'path'
```

**Data types:**

* `byte` - Write as `<number>b`, for example `tag: 30b`
* `short` - Write as `<number>s`, for example `tag: 30s`
* `int` - Write as `<number>`, for example `tag: 30`
* `long` - Write as `<number>L`, for example `tag: 30L`
* `float` - Write as `<number>f`, for example `tag: 30.0f`
* `double` - Write as `<number>d`, for example `tag: 30.0d`
* `byte[]` - Write as `[B; <bytes...>B]`, for example `tag: [B; 30B, 40B, 50B]`
* `int[]` - Write as `[I; <integers...>]`, for example `tag: [I; 30, 40, 50]`
* `long[]` - Write as `[L; <longs...>l]`, for example `tag: [L; 30L, 40L, 50L]`
* `String` - Write as `<text>`, for example `tag: 'Hello'`
