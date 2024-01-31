---
sidebar_position: 2
title: Tipos
description: Tipos de acciones ejecutables de PixelBuy.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

PixelBuy contiene múltiples tipo de acciones.

* Todas las acciones tienen un patrón de nombre compilado en RegEx, esto significa que el nombre de la acción puede ser escrito de diferentes maneras.
* Todos los patrones en RegEx no toman en cuenta las mayúsculas o minúsculas.

## Comando

Ejecuta un comando en la consola de servidor o como si fuera el propio jugador.

**Nombre:** `command`

**Múltiple:** `multi-command`

**RegEx:** `((run|execute)-?)?(multi(ples)?-?)?(command|cmd)s?`

**Comando individual:** Todos los comandos serán ejecutados solo una vez (ignora la cantidad del item).

```yaml
# Simple
'command: cmd'

command: 'cmd'

# Ejecutar múltiples comandos
command:
  - 'cmd1'
  - 'cmd2'

command:
  value:
    - 'cmd1'
    - 'cmd2'

# Configuración avanzada
command:
  value: 'cmd'
  console: true
  multiple: false # <-- Set true to NOT ignore order item amount
```

**Múltiples comandos:** Cada comando será ejecutado el mismo número de veces que la cantidad del item.

```yaml
# Simple
'multi-command: cmd'

multi-command: 'cmd'

# Ejecutar múltiples comandos
multi-command:
  - 'cmd1'
  - 'cmd2'

multi-command:
  value:
    - 'cmd1'
    - 'cmd2'

# Configuración avanzada
multi-command:
  value: 'cmd'
  console: true
```

* `value` (`(value|command|cmd)s?`) - Es el propio comando, puede ser uno o varios.
* `console` (`console(-?sender)?`) - Ponlo en false para ejecutar el comando como si fuera el propio jugador.
* `multiple` (`multiples?`) - Ponlo en true para NO ignorar la cantidad del item (Solo afecta los comandos individuales).

## Item

Darle un item al jugador.

**Nombre:** `item`

**RegEx:** `(give-?)?items?`

```yaml
# Simple
'item: DIAMOND'

item: 'DIAMOND'

# Darle un item de un proveedor
'item: oraxen:storm_sword'

item: 'oraxen:storm_sword'

# Configuración avanzada
item:
  material: DIAMOND_PICKAXE
  amount: 1
  name: '&9&lSuper Pickaxe'
  lore:
    - '&7Custom pickaxe'
    - '&7from server store'
    - ''
    - '&6Buyer: &7%player_name%'
    - '&6Order ID: &7{order_id}'
  enchants:
    SHARPNESS: 10
    DIG_SPEED: 5
    LOOT_BONUS_BLOCKS: 20
```

Para más información sobre el formato de configuración de los items.

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/es/pixelbuy/usage/item-config/",
  label: "Configuración de item",
  description: "Información sobre el formato para configurar items en PixelBuy"
  }}
/>
```

## Mensaje

Enviar un mensaje con texto de color solamente al jugador.

**Nombre:** `message`

**RegEx:** `(send-?)?messages?`

```yaml
# Simple
'message: &eHello'

message: '&eHello'

# Enviar mensaje de varias líneas
message:
  - '&eHello'
  - '&aWorld'

#Configuración avanzada
message:
  value: '&eHello'
  centered: false
  color: false
```

* `value` (`(value|msg|message)s?`) - El valor del propio mensaje, puede ser uno o varios.
* `centered` (`center(ed)?(-?text)?|chat-?width`) - Ponlo en true para enviar el mensaje centrado en el chat, o pon un número para especificar la cantidad de pixeles de anchura del chat.
* `color` (`color(ed)?(-?after)?`) - Ponlo en true para re-colorizar el texto antes de enviarlo al jugador (Esto es útil si un placeholder devuelve un texto que todavía no tiene color).

## Anuncio

Enviar un mensaje con texto de color a todos los jugadores online.

**Nombre:** `broadcast`

**RegEx:** `broadcast(-?messages?)?`

```yaml
# Simple
'broadcast: &eHello'

broadcast: '&eHello'

# Send multiple line message
broadcast:
  - '&eHello'
  - '&aWorld'

# Advanced configuration
broadcast:
  value: '&eHello'
  centered: false
  color: false
  parse: false
```

* `value` (`(value|msg|message)s?`) - El valor del propio mensaje, puede ser uno o varios.
* `centered` (`center(ed)?(-?text)?|chat-?width`) - Ponlo en true para enviar el mensaje centrado en el chat, o pon un número para especificar la cantidad de pixeles de anchura del chat.
* `color` (`color(ed)?(-?after)?`) - Ponlo en true para re-colorizar el texto antes de enviarlo a los jugadores (Esto es útil si un placeholder devuelve un texto que todavía no tiene color).
* `parse` (`(viewer-?)?parse(able)?`) - Reemplazar o no los placeholders dentro de `{}` utilizando como base el jugador que está recibiendo el anuncio.