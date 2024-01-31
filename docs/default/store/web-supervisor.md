---
sidebar_position: 2
title: Web supervisor
description: How to setup PixelBuy web supervisors.
---

The web supervisor interface is a type of system that retrieves data from a web store to apply any necessary delivery inside Minecraft server.

## Types

PixelBuy currently aims to support some delivery concepts:

* `WOOMINECRAFT` - Self-hosted WordPress using **WooMinecraft** plugin.
* `TEBEX` - BuyCraft-like delivery from **Tebex** store.

### Global values

Any web supervisor has common configuration paths.

```yaml
Group: 'servername'
```

* `group` - Is the server associated with the name that the supervisor is looking for orders.

## WooMinecraft

The WooMinecraft supervisor type makes delayed checks to get what order commands must be delivered (like WooMinecaft plugin), but it processes every command separated by comma as store item names.

The store plugin setup is the same as [WooMinecraft wiki](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-2:-Setting-up-the-wordpress-side) and also [the commands](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-3:-Creating-A-Package).

```yaml
Check-Interval: 7
URL: 'http://shop.mysite.com'
Key: 'asdUniqueKeyForServer'
```

* `Check-Interval` - Is the interval in seconds to check WooMinecraft rest api from store url.
* `URL` - Is the store url.
* `Key` - Is the server key from [WooMinecraft configuration](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-2:-Setting-up-the-wordpress-side).

### WooCommerce integration

Instead of WooMinecraft plugin, this web supervisor also has an integration directly with the WooCommerce plugin installed on WordPress site to retrieve more information about store items (and also a better calculation about store order price).

To generate a new WooCommerce API key:

1. Go to the admin panel on your WordPress site.
2. Move the mouse over WooCommerce.
3. Go to Settings.
4. Click on "Advanced" to get into advanced settings.
5. Go to API Keys.
6. Generate a read access key.

:::warning

It's important to generate only a **read access** key, if you set **write access** you are leaving a security breach in your WordPress site if you don't protect the server instance where PixelBuy is installed.

:::

Next you only need to set the generated key and secret into web supervisor configuration.

```yaml
WooCommerce:
  ConsumerKey: 'ck_theGeneratedConsumerKey'
  ConsumerSecret: 'cs_theGeneratedConsumerSecret'
```

Take in count that every consumer key starts with `ck_` and every consumer secret starts with `cs_`.