---
sidebar_position: 2
title: Types
description: PixelBuy exectuable actions types.
---

```mdx-code-block
import DocCard from '@theme/DocCard';
```

PixelBuy contains different action types.

* Every action has a RegEx name pattern, this means the action name can be written differently.
* Every RegEx pattern is case-insensitive.

## Command

Run a command to server console or as the player itself.

**Name:** `command`

**Multiple:** `multi-command`

**RegEx:** `((run|execute)-?)?(multi(ples)?-?)?(command|cmd)s?`

**Single command:** Every command will be executed only one time (ignore order item amount).

```yaml
# Simple
'command: cmd'

command: 'cmd'

# Run multiple commands
command:
  - 'cmd1'
  - 'cmd2'

command:
  value:
    - 'cmd1'
    - 'cmd2'

# Advanced configuration
command:
  value: 'cmd'
  console: true
  multiple: false # <-- Set true to NOT ignore order item amount
```

**Multiple command:** Every command will be executed the same times or order item amount.

```yaml
# Simple
'multi-command: cmd'

multi-command: 'cmd'

# Run multiple commands
multi-command:
  - 'cmd1'
  - 'cmd2'

multi-command:
  value:
    - 'cmd1'
    - 'cmd2'

# Advanced configuration
multi-command:
  value: 'cmd'
  console: true
```

* `value` (`(value|command|cmd)s?`) - Is the command value itself, can be a single or multiple commands.
* `console` (`console(-?sender)?`) - Set to false to execute the command as the player.
* `multiple` (`multiples?`) - Set true to NOT ignore order item amount (only affect single command configuration).

## Item

Give item to player.

**Name:** `item`

**RegEx:** `(give-?)?items?`

```yaml
# Simple
'item: DIAMOND'

item: 'DIAMOND'

# Give items from provider
'item: oraxen:storm_sword'

item: 'oraxen:storm_sword'

# Advanced configuration
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

For more information about item format:

```mdx-code-block
<DocCard item={{
  type: "link",
  href: "/pixelbuy/usage/item-config/",
  label: "Item Configuration",
  description: "Information about PixelBuy item configuration format"
  }}
/>
```

## Message

Send a colored text message only to player.

**Name:** `message`

**RegEx:** `(send-?)?messages?`

```yaml
# Simple
'message: &eHello'

message: '&eHello'

# Send multiple line message
message:
  - '&eHello'
  - '&aWorld'

# Advanced configuration
message:
  value: '&eHello'
  centered: false
  color: false
```

* `value` (`(value|msg|message)s?`) - The message value itself, can be a single or multiple messages.
* `centered` (`center(ed)?(-?text)?|chat-?width`) - Set to true to make a chat-centered message, or set a number to specify the chat pixel width.
* `color` (`color(ed)?(-?after)?`) - Set to true to re-color message before sending it to the player (this is useful if a placeholder returns a value that is not colored yet).

## Broadcast

Send a colored text message to all online players.

**Name:** `broadcast`

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

* `value` (`(value|msg|message)s?`) - The message value itself, can be a single or multiple messages.
* `centered` (`center(ed)?(-?text)?|chat-?width`) - Set to true to make a chat-centered message, or set a number to specify the chat pixel width.
* `color` (`color(ed)?(-?after)?`) - Set to true to re-color message before broadcast to players (this is useful if a placeholder returns a value that is not colored yet).
* `parse` (`(viewer-?)?parse(able)?`) - Parse bracket placeholders inside a message using the current player that receives the broadcast.