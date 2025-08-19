---
sidebar_position: 2
title: Schema
description: PixelBuy web API schema.
---

To allow people make its own website and connect it with PixelBuy, a format to get website data is provided.

## REST API

Make your site serve data with the typical `GET` and `POST` process.

Json content is used to explain the format.

### Authentication

Every website must provide a "secret", an API key to allow the user to get information from the website API.

PixelBuy is compatible with a variety of authentication formats:

* Query parameters, by providing the secret as a query parameter in the same URL.
* Header property, by providing the secret as a http header in every connection.
* Basic authentication, by passing the secret in a Basic authentication header in every connection.

### Version 1

**error:** The response to communicate an error.

```json5
{
  "error": "error_message_code",
  "message": "An error has occur",
  "status": 404,
}
```

**(GET) order:** A store order, the player can be provided by only its name if you don't want to provide the UUID.

```json5
{
  "id": 1234, // Order numeric ID / transaction number
  "date": "1970-01-25", // ISO-8601 formatted
  "player": "7ca003dc-175f-4f1f-b490-5651045311ad:Rubenicos", // [<uuid>:]<name>
  "execution": "BUY", // Optional, the value must be BUY, RECOVER or REFUND
  // The items can be empty if execution is RECOVER or REFUND
  "items": [
    {
      "product": 55, // Optional, it's the product identifier, can be any type of object
      "id": "super-pickaxe", // The item identifier configured on PixelBuy items
      "amount": 1, // The amount of items of this type, must be an integer number
      "price": 2.49, // The real amount of money spend on this item(s)
    },
    {
      "product": 94,
      "id": "eco-bundle",
      "amount": 6,
      "price": 5.04,
    }
  ]
}
```

**(GET) server:** A server information.

```json5
{
  // Orders that should be processed by PixelBuy
  "pending_orders": [
    {
      // Order object...
    },
    {
      // Order object...
    }
  ],
  "next_check": 60, // Optional, the amount of seconds to wait until make the next check
}
```

**(POST) update:** Communicate to store a server update.

```json5
{
  "processed_orders": [ 1234, 6727, 1897 ], // Order IDs that must be removed from server pending orders
}
```
