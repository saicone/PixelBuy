---
sidebar_position: 2
title: Web supervisor
description: How to setup PixelBuy web supervisors.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

The web supervisor interface is a type of system that retrieves data from a web store to apply any necessary delivery inside Minecraft server.

## Types

PixelBuy currently aims to support some delivery concepts:

* `PIXELBUY` - Website compatible with PixelBuy web API format.
* `WOOMINECRAFT` - Self-hosted WordPress using **WooMinecraft** plugin.
* `TEBEX` - BuyCraft-like delivery from **Tebex** store.

### Global values

Any web supervisor has common configuration paths.

```yaml
Type: PIXELBUY
Group: 'servername'
URL: 'https://shop.mysite.com'
```

* `type` - The type of web supervisor.
* `group` - Is the server associated with the name that the supervisor is looking for orders.
* `url` - Is the store url.

### Secret declaration

In any part of the configuration where a "secret", "key" or any value that is supposed to be private, can be declared in the following ways:

* `<value>` - The simple one, where `<value>` is just the value.
* `file:<path>` - To get the value from a file content, where `<path>` is the file path.
* `property:<key>` - To get the value from a system property, where `<key>` is the system property key.

## PixelBuy

The PixelBuy web supervisor retrieves information from a site compatible with PixelBuy web API schema:

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/pixelbuy/developers/schema/",
  label: "Schema",
  description: "PixelBuy web API schema"
  }}
/>
```

This supervisor also came with its own configuration.

```yaml
    Version: 1
    Format:
      Server: '{url}/api/server/{key}'
      Order: '{url}/api/order/{key}'
    Rest:
      Check-Interval: 30
      Auth: PARAMS
      Property: 'secret'
      Secret: ''
```

* `version` - The version of PixelBuy schema.
* `format.server` - The URL format to get server information (`{key}` will be replaced with server group).
* `format.order` - The URL format to get order information (`{key}` will be replaced with order ID).
* `rest.check-interval` - The interval in seconds to check new orders, set to `DETECT` to use a provided one by REST API.
* `rest.auth` - The authentication type to connect with REST API.
* `rest.property` - The property name, this both apply to `PARAMS` and `HEADER` authentication.
* `rest.secret` - The secret key that is used to get information.

### Format version

* **Version 1:** Initial PixelBuy web API version, no changes.

### Auth types

* `PARAMS` - Use query parameters to provide the secret.
* `HEADER` - Use header property to provide the secret.
* `BASIC` - Use HTTP Basic authorization.

## WooMinecraft

The WooMinecraft supervisor type makes delayed checks to get what order commands must be delivered (like WooMinecaft plugin), but it processes every command separated by comma as store item names.

The store plugin setup is the same as [WooMinecraft wiki](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-2:-Setting-up-the-wordpress-side) and also [the commands](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-3:-Creating-A-Package).

```yaml
Check-Interval: 600
Key: 'asdUniqueKeyForServer'
```

* `check-interval` - Is the interval in seconds to check WooMinecraft rest api from store url.
* `key` - Is the server key from [WooMinecraft configuration](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-2:-Setting-up-the-wordpress-side).

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
  Version: 3
  Auth: PARAMS
  ConsumerKey: 'ck_theGeneratedConsumerKey'
  ConsumerSecret: 'cs_theGeneratedConsumerSecret'
```

* `version` - WooCommerce API version, currently only version 3 is supported.
* `auth` - The [authentication type](https://woocommerce.github.io/woocommerce-rest-api-docs/#authentication-over-https) to connect with WooCommerce API (`PARAMS` or `BASIC`).
* `consumerkey` - The generated key.
* `consumersecret` - The generated secret.

Take in count that every consumer key starts with `ck_` and every consumer secret starts with `cs_`.