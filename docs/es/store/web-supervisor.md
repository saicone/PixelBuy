---
sidebar_position: 2
title: Supervisor web.
description: Como configurar los supervisores web en PixelBuy.
---

Los supervisores de web son un tipo de sistema que obtiene datos desde la web de la tienda para aplicar cualquier entrega necesaria dentro del servidor de Minecraft.

## Tipos

Pixelbuy actualmente tiene soporte a diferentes conceptos de entregas:

* `WOOMINECRAFT` - Hosteado propiamente en WordPress utilizando el plugin **WooMinecraft**.
* `TEBEX` - Una entrega de items como BuyCraft utilizando la tienda de **Tebex**.

### Valores globales

Cualquier supervisor web tiene configuraciones en común.

```yaml
Group: 'servername'
```

* `group` - Es el grupo de servidor al que el supervisor está añadiendo las órdenes de compra encontradas.

## WooMinecraft

El supervisor de WooMinecraft hace revisiones cada cierto tiempo para cuales comandos de órden de compra deben ser entregados (como el plugin WooMinecraft), pero este supervisor procesa todos los comandos separados por coma, como si fueran nombre de items de la tienda.

La instalación del plugin WooMinecraft en WordPress es la misma que en la [wiki de WooMinecraft](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-2:-Setting-up-the-wordpress-side) y también la [especificación de los comandos](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-3:-Creating-A-Package).

```yaml
Check-Interval: 7
URL: 'http://shop.mysite.com'
Key: 'asdUniqueKeyForServer'
```

* `Check-Interval` - Es el intervalo en segundos para verificar la api utilizando el url de la tienda.
* `URL` - Es el url de la tienda.
* `Key` - Is la key del servidor utilizado en la [configuración de WooMinecraft](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-2:-Setting-up-the-wordpress-side).

### Integración con WooCommerce

Envés del plugin WooMinecraft, este supervisor de web tiene una integración directamente con el plugin WooCommerce instalado en la página de WordPress para obetener más información sobre los items de la tienda (y realizar un mejor cálculo sobre los precios de los items de la tienda).

Para generar una nueva key y así utilizar la API de WooCommerce.

1. Ir al panel administrativo de tu sitio WordPress.
2. Mover el mouse sobre la sección de WooCommerce.
3. Ir a ajustes/configuración.
4. Darle click a "Avanzado" para ir a la configuración avanzada.
5. Ir a la sección de API Keys.
6. Generar una nueva key con permisos de lectura.

:::warning importante

Es importante generar únicamente una key con **permisos de lectura**, si le das **permisos de escritura** estarás dejando una brecha de seguridad de tu sitio WordPress en caso de que no protejas la instancia de servidor donde PixelBuy está instalado.

:::

Ahora solo debes poner la key generada y el secret en la configuración del supervisor de web.

```yaml
WooCommerce:
  ConsumerKey: 'ck_theGeneratedConsumerKey'
  ConsumerSecret: 'cs_theGeneratedConsumerSecret'
```

Tomar en cuenta que toda key de consumidor empieza con `ck_` y todo secret de consumidor empieza con `cs_`.