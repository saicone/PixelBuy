---
sidebar_position: 2
title: Schema
description: Esquema de datos para hacer un sitio web compatible con PixelBuy.
---

Para permitir que la gente haga su propio sitio web conectado con PixelBuy, se ha creado un formato en común.

## REST API

Hacer que tu sitio web transfiera datos con el proceso tipico de `GET` y `POST`.

Se usará contenido en Json para explicar el formato.

### Autenticación

Todos los sitios web compatibles con PixelBuy deben proveer una "clave secreta", la cual será usada por el usuario para obtener información de la API del sitio web.

PixelBuy es compatible con multiples formatos de autenticación:

* Query parameters, al proveer la clave secrete como un query parameter en el mismo URL.
* Header property, al proveer la clave secrete como un header de http en cada conexión.
* Basic authentication, al pasar la clave secreta en un header de Basic authentication header en cada conexión.

### Versión 1

**error:** Es la respuesta para comunicar un error.

```json5
{
  "error": "error_message_code",
  "message": "An error has occur",
  "status": 404,
}
```

**(GET) order:** Una orden de compra, el jugador puede ser solo un nombre si no quieres dar el UUID.

```json5
{
  "id": 1234, // ID numérico de la orden / número de transacción
  "date": "1970-01-25", // En formato ISO-8601
  "player": "7ca003dc-175f-4f1f-b490-5651045311ad:Rubenicos", // [<uuid>:]<nombre>
  "execution": "BUY", // Opcional, el valor debe ser BUY, RECOVER o REFUND
  // Puede no haber items si el valor de la ejecución es RECOVER o REFUND
  "items": [
    {
      "product": 55, // Opcional, es el identificador del producto, puede ser cualquier tipo de objeto
      "id": "super-pickaxe", // El identificador del item que fue configurado en PixelBuy
      "amount": 1, // La cantidad de items de este tipo, debe ser un número entero
      "price": 2.49, // La cantidad real de dinero gastado en este items (considera la cantidad de items)
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

**(GET) server:** Información sobre el servidor.

```json5
{
  // Ordenes de compra que deberían ser procesadas por PixelBuy
  "pending_orders": [
    {
      // Objeto de una orden (arriba está especificado su formato)...
    },
    {
      // Otro objeto de una orden (arriba está especificado su formato)...
    }
  ],
  "next_check": 60, // Opcional, the amount of seconds to wait until make the next check
}
```

**(POST) update:** Le dice a la tienda que debe ejecutarse una actualización.

```json5
{
  "processed_orders": [ 1234, 6727, 1897 ], // Los IDs de las ordenes que compra que ya fueron procesadas (y por lo tanto ya no deberían aparecer en la información del servidor)
}
```
