---
sidebar_position: 2
title: Base de datos
description: Configuración de la base de datos en PixelBuy.
---

PixelBuy actualmente tiene soporte para el tipo de base de datos `SQL`.

## Usuario

### Cargar todo

Cargar o no toda la información de los usuarios desde la base de datos en la memoria del servidor, esta información incluye:

* El nombre del jugador
* El id único del jugador
* La cantidad donada

Esta opción también hace disponible la opción de generar el top de donadores.

## Top

Es la configuración sobre el top de donadores.

### Límite

Limita el número de entradas para la cantidad calculada en el top donadores, se puede poner como `-1` para calcular la posición del top para todos los usuarios.

### Tiempo

Tiempo en ticks entre el cálculo de top donadores.

Un número base puede provocar lag en la mayoría de servidores, se puede poner como `-1` para desactivar el cálculo de top donadores.

## Sql

Es la configuración del tipo de base de datos `SQL`, edita únicamente esta parte si pusiste `SQL` como el tipo de base de datos utilizado.

El tipo de base de datos `SQL` tiene soporte para diferentes instancias de sql:

| Tipo       | Conexión | Descripción                                                                   |
|------------|----------|-------------------------------------------------------------------------------|
| H2         | Local    | La instancia de base de datos **recomendada** para guardar data local.        |
| SQLITE     | Local    | Es un tipo de instancia más lento para guardar data local.                    |
| MYSQL      | Externa  | El tipo de base de datos más utilizado en servidores de Minecraft             |
| MARIADB    | Externa  | Parecido a MySQL, pero más rápido                                             |
| POSTGRESQL | Externa  | La instancia de base de datos **recomendada** para guardar data externamente. |

Si seleccionaste una base de datos local, puedes ignorar el resto de la configuración.