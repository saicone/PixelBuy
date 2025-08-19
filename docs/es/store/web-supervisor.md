---
sidebar_position: 2
title: Supervisor web.
description: Como configurar los supervisores web en PixelBuy.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

Los supervisores de web son un tipo de sistema que obtiene datos desde la web de la tienda para aplicar cualquier entrega necesaria dentro del servidor de Minecraft.

## Tipos

Pixelbuy actualmente tiene soporte a diferentes conceptos de entregas:

* `PIXELBUY` - Sitio web compatible con el formato de la API de PixelBuy.
* `WOOMINECRAFT` - Hosteado propiamente en WordPress utilizando el plugin **WooMinecraft**.
* `TEBEX` - Una entrega de items como BuyCraft utilizando la tienda de **Tebex**.

### Valores globales

Cualquier supervisor web tiene configuraciones en común.

```yaml
Type: PIXELBUY
Group: 'servername'
URL: 'https://shop.mysite.com'
```

* `type` - El tipo de supervisor de web.
* `group` - Es el grupo de servidor al que el supervisor está añadiendo las órdenes de compra encontradas.
* `url` - Es el url de la tienda.

## PixelBuy

El supervisor de PixelBuy obtiene información de un sitio web compatible con el esquema de la API de PixelBuy:

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/es/pixelbuy/developers/schema/",
  label: "Schema",
  description: "Esquema de datos para hacer un sitio web compatible con PixelBuy"
  }}
/>
```

Este supervisor de web trae su propia configuración.

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

* `version` - Es la versión del esquema de datos de PixelBuy.
* `format.server` - Es el formato de la URL para obtener información del servidor (La parte de `{key}` será reemplazada con el grupo del servidor).
* `format.order` - Es el formato de la URL para obtener información de una orden de compra (La parte de `{key}` será reemplazada con el ID de la orden).
* `rest.check-interval` - El intervalo en segundos para revisar nuevas ordenes de compra, ponlo como `DETECT` si quieres usar el intervalo ofrecido por la REST API del sitio web.
* `rest.auth` - El tipo de autenticación para conectar con la REST API.
* `rest.property` - El nombre de la propiedad, esto aplica para los tipos de autenticación `PARAMS` y `HEADER`.
* `rest.secret` - La clave secreta que será usada para obtener información.

### Versiones

* **Versión 1:** Es la versión inicial de una API de sitio web compatible con PixelBuy, sin cambios.

### Tipos de autenticación

* `PARAMS` - Usar los parametros del query para ofrecer la clave secreta.
* `HEADER` - Usar una propiedad del header para ofrecer la clave secreta.
* `BASIC` - Usar una autorización del tipo HTTP Basic.

## WooMinecraft

El supervisor de WooMinecraft hace revisiones cada cierto tiempo para cuales comandos de órden de compra deben ser entregados (como el plugin WooMinecraft), pero este supervisor procesa todos los comandos separados por coma, como si fueran nombre de items de la tienda.

La instalación del plugin WooMinecraft en WordPress es la misma que en la [wiki de WooMinecraft](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-2:-Setting-up-the-wordpress-side) y también la [especificación de los comandos](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-3:-Creating-A-Package).

```yaml
Check-Interval: 600
Key: 'asdUniqueKeyForServer'
```

* `check-interval` - Es el intervalo en segundos para verificar la api utilizando el url de la tienda.
* `key` - Is la key del servidor utilizado en la [configuración de WooMinecraft](https://github.com/WooMinecraft/WooMinecraft/wiki/Step-2:-Setting-up-the-wordpress-side).

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
  Version: 3
  Auth: PARAMS
  ConsumerKey: 'ck_theGeneratedConsumerKey'
  ConsumerSecret: 'cs_theGeneratedConsumerSecret'
```

* `version` - La versión de la API de WooCommerce, actualmente solo la versión 3 es compatible.
* `auth` - El [tipo de autenticación](https://woocommerce.github.io/woocommerce-rest-api-docs/#authentication-over-https) para conectar con la API de WooCommerce (`PARAMS` o `BASIC`).
* `consumerkey` - La key que generaste.
* `consumersecret` - El secret que generaste.

Tomar en cuenta que toda key de consumidor empieza con `ck_` y todo secret de consumidor empieza con `cs_`.