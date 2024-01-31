---
sidebar_position: 4
title: Colored text
description: Information about PixelBuy colored text format.
---

Unlike other modern plugins (popularly using Adventure MiniMessage format), PixelBuy uses a more common system for colored texts with also new coloring features.

**Color arguments:** Every argument rounded by `<>` is required, and `[]` is optional.

## Basic usage

**Format:** `&<color code>`

Using the common color codes from minecraft, for example `&a` or `&c`.

## RGB

**Format:** `&#<hex>`

A modern color formatting for +1.16 servers that support hex colors, for example `&#B508E3`.

## Special colors

Special color types only available for +1.16 servers.

### Rainbow

**Format:** `&$rainbow[:<saturation>[:<brightness>]][:looping][#<speed>]$`

* `rainbow` can also be written as `r` or `lgbt`.
* `[:<saturation>]` is a float value, by default it's `1.0` and must be written with `:` first, for example `:1.3`.
* `[:<brightness>]` is a float value, by default it's `1.0` and must be written after saturation parameter with `:` first, for example `:0.7`.
* `[:looping]` can also be written as `loop` or `l` and must be written after saturation or brightness parameter with `:` first, for example `:looping`.
* `[#<speed>]` is a number to calculate the update speed of current colored text and is written at the end, for example `#30`.

**Examples:**

* `&$rainbow$This a rainbow colored text!`
* `&$rainbow:looping#20$This a rainbow colored text that support updates!`
* `&$rainbow:1.2:1.5$This a rainbow colored text with a different saturation and brightness!`

### Gradient

**Format:** `&$gradient:<hex...>[:looping][#<speed>]$`

* `gradient` can also be written as `g`.
* `<hex...>` is a list of HEX values separated by `:` to generate a multi-colored gradient, for example `12E308:E30808`.
* `[:looping]` can also be written as `loop` or `l` and must be written after latest hex value parameter with `:` first, for example `:looping`.
* `[#<speed>]` is a number to calculate the update speed of current colored text and is written at the end, for example `#20`.

**Examples:**

* `&$gradient:12E308:E30808$This a gradient colored text!`
* `&$gradient:12E308:E30808#20$This a gradient colored text that support updates!`

### Stop coloring

**Format:** `&$stop$`

This placeholder is used to mark a stop for any special text colorizing.

**Examples:**

* `&$rainbow$This a rainbow colored text!&$stop$And this text is also normal`
* `&$gradient:12E308:E30808$This a gradient colored text!&$stop$And this text is also normal`

:::info

All the special coloring types can be stopped with any color format.

:::