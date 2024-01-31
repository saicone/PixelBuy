---
sidebar_position: 3
title: Mensajería
description: Configuración de la mensajería en PixelBuy.
---

Una conexión con un servicio de mensajería permite al plugin crear una sincronización de data entre hosts que utilizan la misma base de datos externa.

No configurar una instancia de mensajería puede provocar una pérdida de datos.

Este concepto utiliza un canal principal para la transferencia de mensajes, todos los servidores con una instancia de mensajería deben de utilizar el mismo nombre de canal.

PixelBuy actualmente es compatible con los tipos de mensajería `SQL`, `REDIS` y `RABBITMQ`, tambien lo puedes configurar como `AUTO` para hacer que el plugin escoga la mejor opción disponible.

## Sql

El tipo de mensajería `SQL` utiliza la base de datos sql actual para transferir mensajes al añadir filas en una tabla por separado, luego revisa cualquier fila nueva (probablemente añadida por otra instancia del plugin en otro host).

Cada 30 segundos, todas las filas con más de 60 segundos de antiguedad son borradas automáticamente por cualquier instancia del plugin conectada al mismo tipo de instancia de mensajería `SQL`.

## Redis

El tipo de mensajería `Redis` se conecta a una instancia Redis para transferir mensajes, no hay mucho que decir al respecto debido a que este es un sistema bastante conocido.

## RabbitMQ

El tipo de mensajería `RABBITMQ` se conecta a una instancia RabbitMQ para transferir mensajes temporales, a diferencia de Redis, este tipo de mensajería es más estandarizada (y probablemente mejor), además que requiere configurar un exchange o "pre-canal" para acceder a los canales de mensajería.