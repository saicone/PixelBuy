---
sidebar_position: 1
title: Configuración
description: Configuración principal de PixelBuy.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

## Plugin

### Nivel de logs

Por defecto PixelBuy utiliza el nivel `3` de logs, puede ser cambiado para mostrar:

* `1` = Errores
* `2` = Advertencias
* `3` = Información
* `4` = Información de depuración

Por ejemplo, si pones `3` aparecerán errores, advertencias e información, o bien puedes poner `0` para desactivar cualquier mensaje de logs.

### Proveedor de jugadores

La mayoría de servidores Bukkit (principalmente los que utilizan el modo offline) no tiene una forma correcta de obtener la información de un `OfflinePlayer` (nombre e id), así que PixelBuy ofrece diferentes formas de reemplazar eso con un sistema más robusto:

* `AUTO` = Escoge automáticamente la mejor opción disponible.
* `PIXELBUY` = Utiliza los datos cargados por PixelBuy.
* `LUCKPERMS` = Utiliza el plugin LuckPerms.
* `BUKKIT` = Utiliza los métodos que trae Bukkit por defecto para obtener jugadores offline.

Cada proveedor de jugadores utiliza el tipo `BUKKIT` cuando no encuentra ninguna información sobre un `OfflinePlayer`.

## Idioma

PixelBuy maneja distintos tipos de idiomas de manera automática, seleccionando el idioma disponible más cercado al idioma de cada jugador.

Puedes agregar tu propio idioma al crear un archivo `.yml` en la carpeta `plugins/PixelBuy/lang`.

### Idioma del plugin

Es el idioma que se muestra de PixelBuy en la consola, se puede cambiar en la configuración del plugin.

### Idioma por defecto

Es el idioma que será puesto para los jugadores que tienen un idioma que no está disponible en el plugin.

### Alias

Es una lista de los alias de los idiomas principales.

## Base de datos

```mdx-code-block
<DocCard item={{
	type: "link",
	href: "/es/pixelbuy/setup/database/",
	label: "Configuración de la base de datos",
	description: "Como configurar la conexión con la base de datos en PixelBuy"
	}}
/>
```

## Mensajería

```mdx-code-block
<DocCard item={{
	type: "link",
	href: "/es/pixelbuy/setup/messenger/",
	label: "Configuración de la mensajería",
	description: "Como configurar la conexión con la mensajería en PixelBuy"
	}}
/>
```

## Placeholder

La compatibilidad con PlaceholderAPI puede ser registrada con diferentes nombres para utilizarlos en cualquier sistema compatible con placeholders.

## Comandos

Es la configuración de los comandos del plugin, puedes editar estos comandos como quieras siguiendo este formato:

* `register` - Registrar o no el comando para ser utilizado como un comando principal, como `/command`.
* `name` - El nombre principal del comando.
* `aliases` - La lista de los alias para registrar junto con el comando.
* `permission` - El permiso para utilizar el comando.
* `delay` - Retraso en segundos entre las ejecuciones del comando, esta opción solo funciona so el comando es registrado como un comando principal.