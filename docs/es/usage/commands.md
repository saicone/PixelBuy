---
sidebar_position: 2
title: Comandos
description: Información sobre los comandos en PixelBuy.
---

PixelBuy tiene una variedad de comandos para manejar el plugin y sus datos.

**Argumentos del comando:** Cualquier argumento denomidado por `<>` es requerido, mientras que `[]` es opcional.

## Recargar

**Utilización:** `/pbuy reload <tipo>`

Recarga el plugin en base a los tipos:

* `files` - Recarga la configuración y archivos de idioma (no incluye la tienda).
* `store` - Recargar la tienda de PixelBuy.
* `database` - Recarga la conexión con la base de datos.
* `command` - Recarga la configuración de los comandos.
* `all` - Recarga todos los tipos mencionados anteriormente.

## Usuario

**Utilización:** `/pbuy user <jugador> <comando>`

Comandos relacionados con usuarios, el argumento `<jugador>` puede ser un nombre de jugador o id única.

### Información

**Utilización:** `/pbuy user <jugador> info [página] [grupo]`

Obtener la información sobre el jugador especificado, las órdenes de compra están separadas en grupos de 5, por lo que se pueden mostrar diferentes grupos al especificar una página distinta.

Además las órdenes de compra tienen items dependiendo el nombre del grupo de servidor, para mostrar items de un grupo en específico se puede añadir el grupo después del argumento de la página.

### Calcular

**Utilización:** `/pbuy user <jugador> calculate`

Calcula la cantidad de donada del usuario de la tienda, este comando también actualiza la data del usuario en caso de que la cantidad donada calculada no se igual a la que figura en la base de datos.

### Proveedor

**Utilización:** `/pbuy user <jugador> provided`

Obtener información del jugador según el proveedor de jugadores especificado en la configuración del plugin.

## Órden de compra

**Utilización:** `/pbuy order <orden> <comando>`

Comandos relacionados con órdenes de compra, el argumento `<orden>` utiliza en formato `proveedor:id:grupo`:

* `proveedor` (Opcional) - Es el proveedor de donde vino la orden de compra, por defecto es el supervisor web especificado en la configuración de la tienda de PixelBuy.
* `id` - Es el ID ennumerado de la orden de compra.
* `grupo` (Opcional) - Es el nombre de group de servidor asociado con la orden de compra, por defecto es el nombre de grupo de servidor especificado en la configuración de la tienda de PixelBuy.

**Formato de fecha:** Cualquier argumento de fecha utiliza el formato `YYYY-MM-DD`, por ejemplo `2024-03-21`.

**Tipos de ejecuciones de órden de compra:**

* `BUY` - La órden será marcada como si se hubiera comprado y los items deben ser entregados.
* `RECOVER` - La órden será marcada como si se hubiera comprado, pero ahora los items requieren ser entregados nuevamente debido a alguna pérdida o compensación.
* `REFUND` - Ejecuta un reembolso para la órden de compra especificada.

**Tipos de estados para los items pertenecientes a una órden de compra:**

* `DONE` - El item ya fue entregado.
* `PENDING` - El item no ha sido entregado.
* `ERROR` - El item no puede ser entregado.

### Información

**Utilización:** `/pbuy order <orden> info [grupo]`

Obtener información sobre la orden de compra especificada, por defecto solo muestra los items con el mismo grupo de la orden de compra, para mostrar items de un grupo en específico se puede especificar al nombre del grupo como un argumento en el comando.

### Arreglar

**Utilización:** `/pbuy order <orden> fix`

Cambia todos los items marcados como `ERROR`, al estado `PENDING` significando que cualquier error producido mientras la orden de compra era entregada ahora está solucionado.

### Ejecutar

**Utilización:** `/pbuy order <orden> execute <execution> [fecha]`

Ejecuta una acción en base a la orden de compra utilizando la fecha actual o una especificada en el comando.

### Entregar

**Utilización:** `/pbuy order <orden> give <jugador> <items...> [parámetros...]`

Crea y entrega la orden especificada al jugador con una lista de items de la tienda.

Todos los items utilizan el formato `nombre|cantidad` donde la cantidad es opcional (junto con el separador `|`).

Los parámetros afectan cualquier item especificado después del parámetro y utilizan el formato `--<tipo>=<value>` con los tipos:

* `group` - El grupo donde los items serán añadidos.
* `date` - Cambia la fecha de la orden de compra.
* `execution` - Establece la ejecución de la orden de compra.
* `state` - El estado de los items que serán añadidos.

### Eliminar

**Utilización:** `/pbuy order <orden> delete`

Elimina la orden de compra de la base de datos.

### Buscar

**Utilización:** `/pbuy order <orden> lookup <jugador>`

**Entregar:** `/pbuy order <orden> lookup run`

Busca cualquier orden del supervisor web al que pertenece en base al jugador especificado, para luego mostrar la información de la orden encontrada.

Si la orden es encontrada, el parámetro `run` envés del jugador procesará la orden encontrada como si fuera una orden del propio supervisor web.

## Item de orden de compra

**Utilización:** `/pbuy order <orden> item <item> <comando>`

Este es un sub comando del comando de la orden de compra para editar la información de los items de compra.

El parámetro `<item>` utiliza el formato `nombre:grupo` donde el grupo es opciona (junto con el separador `:`).


### Información

**Utilización:** `/pbuy order <orden> item <item> info`

**Ver el error guardado:** `/pbuy order <orden> item <item> info error`

Obtener información sobre el item especificado, se puede utilizar el parámetro `error` para mostrar el mensaje de error en caso de que item tenga el estado de `ERROR`.

### Estado

**Utilización:** `/pbuy order <orden> item <item> state <estado> [mensaje]`

Cambia el estado del item especificado, en caso de que el estado sea `ERROR` puedes agregar el mensaje de error como el último parámetro del comando.

### Precio

**Utilización:** `/pbuy order <orden> item <item> price <precio>`

Cambia el precio del item especificado.

### Cantidad

**Utilización:** `/pbuy order <orden> item <item> amount <cantidad>`

Cambia la cantidad del item especificado.

### Agregar

**Utilización:** `/pbuy order <orden> item <item> add [estado] [precio]`

Agrega el item especificado en la orden de compra con los parámetros del comando.

### Eliminar

**Utilización:** `/pbuy order <orden> item <item> delete`

Elimina el item especificado de la orden de compra.