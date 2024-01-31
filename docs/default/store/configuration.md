---
sidebar_position: 1
title: Configuration
description: Information about PixelBuy store configuration.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

PixelBuy store configuration defines how the plugin interacts with the actual server store.

## Display

Any display information.

### Name

Is the name to be displayed on main store GUI.

## Options

### Group

Is the current server group name, you can set the same group name in different hosts to handle all as one server mode.

### Default supervisor

Is the main web supervisor to use when is not specified on any order-related command.

## Categories

A list of store categories, can be only its name or any extra configuration as category discount.

## Checkout

### Delay

Is a delay between order items building and execution, the time is on ticks.

## Supervisor

A group of web supervisors to read information from real web stores.

```mdx-code-block
<DocCard item={{
	type: "link",
	href: "/pixelbuy/store/web-supervisor/",
	label: "Web Supervisor",
	description: "How to setup PixelBuy web supervisors"
	}}
/>
```

## Global values

Is the global configuration that will be applied into store items.

```mdx-code-block
<DocCard item={{
	type: "link",
	href: "/pixelbuy/store/items/",
	label: "Store items",
	description: "Information about PixelBuy store items"
	}}
/>
```