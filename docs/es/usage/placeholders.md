---
sidebar_position: 3
title: Placeholders
description: Información sobre los placeholders en PixelBuy.
---

PixelBuy tiene mucha información que puede ser obtenida utilizando placeholders ofrecidos en las interacciones del plugin o utilizando PlaceholderAPI.

## Placeholders internos

**Formato:** `{<objeto>_<parámetro>}`

Los placeholders internos están disponibles en las interacciones de entrega (como las acciones de las órdenes de compra) y los parámetros de objetos disponibles son:

* Usuario
* Órden de compra
* Item de órden de compra
* Tienda
* Item de la tienda
* Acción

## Compatibilidad con PlaceholderAPI

**Formato:** `%<nombre>_[objeto]_<parámetro>%`

El nombre del placeholder se puede cambiar en la configuración de PixelBuy.

Los parámetros de objetos que se pueden utilizar en los placeholders de PlaceholderAPI son:

* Usuario (se utiliza por defecto, no requiere especificar el "user" en el parámetro `[objeto]`).
* Top

Para obtener la posición actual de jugador en el top: `%<name>_top%`

## Parámetros de objeto

Los parámetros que pueden ser utilizados para obtener información de los objetos.

### Usuario

**Formato:** `user_<parámetro>`

**Parámetros:**

* `uuid`, `uniqueid` o `unique_id` - Id único del usuario.
* `id` - Id único del usuario sin los `-`.
* `name` - Nombre del usuario (en minúsculas), puede devolver `null`.
* `donated` - Cantidad donada por el jugador.
* `orders` - La cantidad de órdenes de compra.

### Top

**Formato:** `top_<posición>_<parámetro>`

La `<posición>` es un número del caché del top calculados y el `<parámetro>` es un parámetro del objeto usuario.

### Órden de compra

**Formato:** `order_<parámetro>`

**Parámetros:**

* `data_id` - El id asociado con la órden de compra guardada en la base de datos, puede devolver `null`.
* `provider` - El supervisor web/proveedor de la órden.
* `id` - Id numérico de la órden.
* `group` - Grupo de la órden.
* `buyer` - Id único del comprador de la órden.
* `buyer_id` - Id único del comprados de la órden sin los `-`.
* `date` - Fecha de la ejecución actual de la órden.
* `date_buy` - Fecha de cuando la órden fue comprada.
* `date_recover` - Fecha de cuando la órden fue recuperada.
* `date_refund` - Fecha de cuando la órden fue devuelta.
* `execution` - Ejecución actual de la órden.
* `items` - Cantidad de items en al órden.

### Item de órden de compra

**Formato:** `order_item_<parámetro>`

**Parámetros:**

* `id` - El nombre del item.
* `amount` - La cantidad de veces que este item está repetido.
* `price` - Precio del item.
* `state` - Estado actual del item.
* `error` - Mensaje de error actual si el estado del item es `ERROR`.

### Tienda

**Formato:** `store_<parámetro>`

**Parámetros:**

* `name` - Nombre de la tienda.
* `group` - Nombre del grupo de servidor.
* `supervisor` - Supervisor web utilizado por defecto.
* `categories` - Categorías de la tienda separadas por `\n`.
* `categories_size` - Cantidad de categorías de la tienda.
* `supervisors` - Supervisores web separados por `\n`.
* `supervisors_size` - Cantidad de supervisores web.
* `items` - Items de la tienda separados por `\n`.
* `items_size` - Cantidad de items de la tienda.

### Item de la tienda

**Formato:** `store_item_<parámetro>`

**Parámetros:**

* `id` - Nombre del item.
* `category` - Categoría actual del item.
* `categories` - Categorías del item separadas `\n`.
* `categories_size` - Cantidad de categorías del item.
* `price` - Precio del item.

### Acción

**Formato:** `action_<parámetro>`

**Parámetros:**

* `count` - El número actual de veces que la acción ha sido repetida.