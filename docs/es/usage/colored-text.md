---
sidebar_position: 4
title: Texto de color
description: Información sobre el formato de texto de color en PixelBuy.
---

A diferencia de otros plugins modernos (popularmente utilizando el formato MiniMessage de la librería Adventure), PixelBuy utiliza un sistema más común para los textos con color con nuevas opciones de coloración.

**Argumentos de color:** Cada arumento dentro de `<>` es requerido, mientras que `[]` es opcional.

## Utilización básica

**Formato:** `&<código de color>`

Utilizando los códigos de color comúnes de minecraft, por ejemplo `&a` o `&c`.

## RGB

**Formato:** `&#<hex>`

Un formato de coloración moderno para los servidores +1.16 compatibles con colores personalizables, por ejemplo `&#B508E3`.

## Colores especiales

Tipos de colores especiales solamente compatibles con servidores +1.16.

### Arcoíris

**Formato:** `&$rainbow[:<saturación>[:<brillo>]][:looping][#<velocidad>]$`

* `rainbow` puede ser escrito como `r` o `lgbt`.
* `[:<saturación>]` es un valor con decimales, por defecto es `1.0` y debe escribirse con un `:` primero, por ejemplo `:1.3`.
* `[:<brillo>]` es un valor con decimales, por defecto es `1.0` y debe escribirse después de la sección de saturación con un `:` primero, por ejemplo `:0.7`.
* `[:looping]` puede ser escrito como `loop` o `l` y debe escribirse después de la sección de saturación y brillo con un `:` primero, por ejemplo `:looping`.
* `[#<velocidad>]` es un número para calcular la velocidad de actualización de texto de color actual y se debe escribir al final, por ejemplo `#30`.

**Ejemplos:**

* `&$rainbow$Este es un texto arcoíris!`
* `&$rainbow:looping#20$Este es un texto arcoíris que se actualiza!`
* `&$rainbow:1.2:1.5$Este es un texto arcoíris con una saturación y brillo diferente!`

### Gradiente

**Formato:** `&$gradient:<hex...>[:looping][#<velocidad>]$`

* `gradient` puede ser escrito como `g`.
* `<hex...>` es una lista de colores en formato HEX separados por `:` para generar una gradiente con múltiples colores, por ejemplo `12E308:E30808`.
* `[:looping]` puede ser escrito como `loop` o `l` y debe escribirse al final del último valor de los hex con un `:` primero, por ejemplo `:looping`.
* `[#<velocidad>]` es un número para calcular la velocidad de actualización de texto de color actual y se debe escribir al final, por ejemplo `#20`.

**Ejemplos:**

* `&$gradient:12E308:E30808$Este es un texto con una gradiente de color!`
* `&$gradient:12E308:E30808#20$Este es un texto con una gradiente de color que se actualiza!`

### Detener coloración

**Formato:** `&$stop$`

Este placeholder se utiliza para markar un alto a cualquier coloración de texto especial.

**Ejemplos:**

* `&$rainbow$Este es un texto arcoíris!&$stop$Y este es un texto normal`
* `&$gradient:12E308:E30808$Este es un texto con una gradiente de color!&$stop$Y este es un texto normal`

:::info

Todas las coloraciones de texto especiales pueden ser detenidas con cualquier formato de color.

:::