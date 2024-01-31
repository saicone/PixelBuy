---
sidebar_position: 1
title: Formato
description: Formato de configuración de las acciones ejecutables de PixelBuy.
---

La acciones en PixelBuy son simples objetos de configuración que pueden ser especificados en diferentes maneras.

## Simple

```yaml
actions: 'action: value'
```

## Avanzado

```yaml
actions:
  action1: 'value'

  action2:
    - 'value1'
    - 'value2'
    - 'value3'

  action3:
    parameter1: 'value1'
    parameter2: 'value2'
    parameter3:
      - 'value1'
      - 'value2'
```

## Lista

```yaml
actions:
  - 'action: value'
  - action: 'value'
  - action:
      - 'value1'
      - 'value2'
      - 'value3'
  - action:
      parameter1: 'value1'
      parameter2:
        - 'value1'
        - 'value2'
```