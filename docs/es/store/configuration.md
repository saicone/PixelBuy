---
sidebar_position: 1
title: Configuración
description: Información sobre la configuración de al tienda de PixelBuy.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

La configuración de la tienda en PixelBuy define como el plugin inetractúa con el servidor de la tienda actual.

## Display

Cualquier información mostrada.

### Nombre

Es el nombre que se mostrará en el menú principal de la tienda.

## Opciones

### Grupo

Es el nombre del grupo de servidor actual, puedes poner el mismo nombre de grupo en diferentes hosts para manejarlos a todos como si fueran un solo modo de juego.

### Supervisor por defecto

Es el supervisor web que se utiliza principalmente si no se especifica en ningún comando relacionado con órdenes de compra.

## Categorías

Es una lista de las categorías de la tienda, puede ser únicamente el nombre de la categoría o cualquier información como un descuento por categoría.

## Caja

### Retraso

Es el retraso que hay entre la construcción de los items y su ejecución, el tiempo es en ticks.

## Supervisor

Un grupo de supervisores web para leer información sobre las tiendas web reales.

```mdx-code-block
<DocCard item={{
	type: "link",
	href: "/es/pixelbuy/store/web-supervisor/",
	label: "Supervisor web",
	description: "Como configurar los supervisores de web"
	}}
/>
```

## Valores globales

Es la configuración global que será aplicada en todos los items de la tienda.

```mdx-code-block
<DocCard item={{
	type: "link",
	href: "/es/pixelbuy/store/items/",
	label: "Items de la tienda",
	description: "Información sobre los items de la tienda en PixelBuy"
	}}
/>
```