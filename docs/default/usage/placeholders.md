---
sidebar_position: 3
title: Placeholders
description: Information about PixelBuy placeholders.
---

PixelBuy has a lot of information that can be accessed using placeholders provided by the plugin interactions or get them using PlaceholderAPI.

## Internal placeholders

**Format:** `{<object>_<parameter>}`

The internal placeholders are available on order delivery interactions (like order actions) and the available object parameters are:

* User
* Order
* Order item
* Store
* Store item
* Action

## PlaceholderAPI compatibility

**Format:** `%<name>_[object]_<parameter>%`

The placeholder name can be changed on PixelBuy configuration.

The available object parameters to use on PlaceholderAPI placeholders are:

* User (it used by default, doesn't require to specify "user" on `[object]` parameter).
* Top

To get current player position on top calculation: `%<name>_top%`

## Object parameters

The parameters that can be used to get object information.

### User

**Format:** `user_<parameter>`

**Parameters:**

* `uuid`, `uniqueid` or `unique_id` - User unique id.
* `id` - User unique id without `-`.
* `name` - User name (in lowercase), also can be `null`.
* `donated` - User donated amount.
* `orders` - Orders size.

### Top

**Format:** `top_<position>_<parameter>`

The `<position>` is a number from cached top calculation and `<parameter>` is a parameter from the user.

### Order

**Format:** `order_<parameter>`

**Parameters:**

* `data_id` - Database associated id, can be `null`.
* `provider` - Order web supervisor/provider.
* `id` - Order id.
* `group` - Order group.
* `buyer` - Order buyer unique id.
* `buyer_id` - Order byter unique id without `-`.
* `date` - Order date for current order execution.
* `date_buy` - Order date when order was bought.
* `date_recover` - Order date when order was redelivered.
* `date_refund` - Order date when order was refunded.
* `execution` - Current order execution.
* `items` - Items size.

### Order item

**Format:** `order_item_<parameter>`

**Parameters:**

* `id` - The item name.
* `amount` - The amount of times this item is repeated.
* `price` - Item price.
* `state` - Current item state.
* `error` - Current item error message if state is `ERROR`.

### Store

**Format:** `store_<parameter>`

**Parameters:**

* `name` - Store name.
* `group` - Server group name.
* `supervisor` - Default web supervisor name.
* `categories` - Store categories names separated by `\n`.
* `categories_size` - Store categories size.
* `supervisors` - Store web supervisors names separated by `\n`.
* `supervisors_size` - Store web supervisors size.
* `items` - Store items separated by `\n`.
* `items_size` - Store items size.

### Store item

**Format:** `store_item_<parameter>`

**Parameters:**

* `id` - The item name.
* `category` - Current item category.
* `categories` - Item categories names separated by `\n`.
* `categories_size` - Item categories size.
* `price` - Item price.

### Action

**Format:** `action_<parameter>`

**Parameters:**

* `count` - The current number of times the action has been executed.