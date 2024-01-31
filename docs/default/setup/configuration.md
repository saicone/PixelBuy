---
sidebar_position: 1
title: Configuration
description: PixelBuy main configuration.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

## Plugin

### Logging level

By default PixelBuy uses a logging level of `3`, can be changed to listen:

* `1` = Errors
* `2` = Warnings
* `3` = Information
* `4` = Debug information

For example, if you set `3` it will listen errors, warnings and information or set `0` to disable any logging.

### Player provider

Most Bukkit servers (mainly the offline-mode ones) doesn't have a proper way to get `OfflinePlayer` information (name and unique id), so PixelBuy offers different ways to replace that with a more robust system like:

* `AUTO` = Choose automatically the best available option.
* `PIXELBUY` = Use PixelBuy loaded data.
* `LUCKPERMS` = Use LuckPerms plugin.
* `BUKKIT` = Use the default Bukkit offline player methods.

Every player provider system fallback into `BUKKIT` if it doesn't find any information for `OfflinePlayer`.

## Language

PixelBuy handles language types automatically, choosing the nearest available language to players with some default values.

You can add your own language by creating a `.yml` file into `plugins/PixelBuy/lang` folder.

### Plugin language

Is the language how you see PixelBuy in console, can be changed on plugin configuration.

### Default language

Is the language to set for players that have a language not supported by the plugin.

### Aliases

Is a list of any language alias for a mainly configured one.

## Database

```mdx-code-block
<DocCard item={{
	type: "link",
	href: "/pixelbuy/setup/database/",
	label: "Database configuration",
	description: "How to setup PixelBuy database connection"
	}}
/>
```

## Messenger

```mdx-code-block
<DocCard item={{
	type: "link",
	href: "/pixelbuy/setup/messenger/",
	label: "Messenger configuration",
	description: "How to setup PixelBuy messenger connection"
	}}
/>
```

## Placeholder

The PlaceholderAPI compatibility can be registered with different names to be used on any placeholder compatible system.

## Commands

Is the plugin command configuration, you can edit those commands as you want following the current format:

* `register` - Register or not the command to be used as the main command like `/command`.
* `name` - The main command name.
* `aliases` - The list of aliases to be registered along the command.
* `permission` - The permission to use the command.
* `delay` - Delay in seconds between execution, this only takes effect if the command is registered as the main command.