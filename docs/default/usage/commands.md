---
sidebar_position: 2
title: Commands
description: Information about PixelBuy commands.
---

PixelBuy has a variety of commands to manage the plugin and data.

**Command arguments:** Every argument rounded by `<>` is required, and `[]` is optional.

## Reload

**Usage:** `/pbuy reload <type>`

Reload the plugin with the current types:

* `files` - Reload settings and language files (doesn't include store).
* `store` - Reload pixel store.
* `database` - Reload database connection.
* `command` - Reload commands configuration.
* `all` - Reload all the types above.

## User

**Usage:** `/pbuy user <player> <command>`

User related commands, the `<player>` argument can be a player name or player unique id.

### Info

**Usage:** `/pbuy user <player> info [page] [group]`

Get the information about the provided player, store orders are separated into groups of 5, so can be displayed by specifying a different page.

Also store orders have items depending on server group name, to display specific group name items the group should be set after page argument.

### Calculate

**Usage:** `/pbuy user <player> calculate`

Calculate store user donated amount, this command also updates user data in case the donated amount is not equals as its database information.

### Provided

**Usage:** `/pbuy user <player> provided`

Get player information from the player provider specified on plugin configuration.

## Order

**Usage:** `/pbuy order <order> <command>`

Order related commands, the `<order>` argument use the format `provider:id:group`:

* `provider` (Optional) - The order provider that came from, by default it's the default web supervisor from store configuration.
* `id` -  The order numbered ID.
* `group` (Optional) - The order associated server group name, by default it's the current server group name from store configuration.

**Date format:** Any date argument use the format `YYYY-MM-DD`, for example `2024-03-21`.

**Order execution types:**

* `BUY` - The order will be marked as if it had been purchased and the items must be delivered.
* `RECOVER` - The order will be marked as if it had been purchased, but now the items are required to be redelivered due any loss or compensation.
* `REFUND` - Execute a refund for the provided order.

**Order item state types:**

* `DONE` - The item is already delivered.
* `PENDING` - The item is not delivered.
* `ERROR` - The item cannot be delivered.

### Info

**Usage:** `/pbuy order <order> info [group]`

Get information about provided order, by default it only displays items with the same group as store order, to display specific group name items the group be set as command argument.

### Fix

**Usage:** `/pbuy order <order> fix`

Change every store item marked as `ERROR`, to `PENDING` meaning that any error produced while order delivery is already fixed.

### Execute

**Usage:** `/pbuy order <order> execute <execution> [date]`

Execute order action by current date or specified date.

### Give

**Usage:** `/pbuy order <order> give <player> <items...> [parameters...]`

Create and give the specified order to the player with a list of store items.

Every item use the format `name|amount` where amount is optional (and also the `|` separator).

The parameters affect any item specified after parameter and use the format `--<type>=<value>` with the types:

* `group` - Override group where items will be added.
* `date` - Set order date.
* `execution` - Set order execution.
* `state` - Override item state.

### Delete

**Usage:** `/pbuy order <order> delete`

Delete the provided order from database.

### Lookup

**Usage:** `/pbuy order <order> lookup <player>`

**Give:** `/pbuy order <order> lookup run`

Lookup any order from the web supervisor that belongs to the provided player and display order information.

If the order was found, the parameter `run` instead of player processes the found order as web supervisor order.

## Order Item

**Usage:** `/pbuy order <order> item <item> <command>`

This is a sub command of orden command to edit store items information.

The `<item>` parameter uses the format `name:group` where group is optional (and also the `:` separator).

### Info

**Usage:** `/pbuy order <order> item <item> info`

**See saved error:** `/pbuy order <order> item <item> info error`

Get information about the provided item, you can use `error` parameter to display an error message item that has `ERROR` state.

### State

**Usage:** `/pbuy order <order> item <item> state <state> [message]`

Change the provided item state, if the state is `ERROR` you can provide the error message as the last parameter.

### Price

**Usage:** `/pbuy order <order> item <item> price <price>`

Change provided item price.

### Amount

**Usage:** `/pbuy order <order> item <item> amount <amount>`

Change provided item amount.

### Add

**Usage:** `/pbuy order <order> item <item> add [state] [price]`

Add the provided item into order with the specified parameters.

### Delete

**Usage:** `/pbuy order <order> item <item> delete`

Delete the provided item from order.