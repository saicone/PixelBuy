---
sidebar_position: 1
title: Format
description: PixelBuy exectuable actions format configuration.
---

The PixelBuy actions are simple configuration objects that can be set in different ways.

## Single

```yaml
actions: 'action: value'
```

## Advanced

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

## List

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