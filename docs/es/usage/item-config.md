---
sidebar_position: 5
title: Configuración de item
description: Información sobre el formato para configurar items en PixelBuy.
---

Cualquier item configurado en PixelBuy utiliza el mismo formato, el cual es bastante simple.

## Uso básico

Utilizando tags comunes puedes hacer por ejemplo un diamante con un nombre personalizado:

```yaml
material: DIAMOND
amount: 3
name: '&eNombre personalizado'
```

## Formato

PixelBuy utiliza la librería XSeries para serializar items con el formato de XItemStack.

### Item

Los tags de item básicos para hacerlo correctamente, solo el `material` es requerido.

```yaml
material: DIAMOND
amount: 1
damage: 1
unbreakable: false
```

* `material` - El material del item.
* `amount` - La cantidad del item.
* `damage` - El valor del daño del item.
* `unbreakable` (Solo para MC +1.11) - Ponerlo en true para hacer el item irrompible

:::tip

El tag `unbreakable` fue añadido en Minecraft 1.7, pero la API de Bukkit solamente deja editarlo desde MC 1.11, igualmente puedes establecer el estado de irrompibilidad del item al editar su configuración de NBT más abajo en esta página.

:::

### Display

Cualquier configuración relacionada sobre como se mostrará el item.

```yaml
name: '&eNombre personalizado'
lore:
  - '&7Algunas líneas'
  - '&ade lore'
flags:
  - HIDE_ATTRIBUTES
glow: false
custom-model-data: 40
```

* `name` - Es el nombre mostrado del item.
* `lore` - Es el lore mostrado del item.
* `flags` - Son las flags del item, utiliza `ALL` para añadirle todas las flags.
* `glow` - Ponlo en true para hacer que el item brille (Esto añade `Durability I` al item con la flag `HIDE_ENCHANTS`).
* `custom-model-data` (Solo para MC +1.14) - Es el modelo personalizado del item en base al paquete de texturas.

### Atributos

:::info

Esta configuración es solamente para MC 1.13 o superior.

:::

Los modificadores de atributos utilizados en el item, una lista de los atributos disponibles se puede encontrar en los [Javadocs de Spigot](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html).

```yaml
attributes:
  GENERIC_ATTACK_DAMAGE:
    id: '7ca003dc-175f-4f1f-b490-5651045311ad'
    name: generic.attack_damage
    amount: 8.0
    operation: ADD_NUMBER
    slot: HAND
```

* `id` - Es el id único del modificador del atributo (no es requerido).
* `name` - Es el nombre del atributo.
* `amount` - Es la cantidad base utilizada en la operación.
* `operation` - La operación utilizada en el atributo, una lista de las operaciones disponibles se puede encontrar en los [Javadocs de Spigot](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/AttributeModifier.Operation.html).
* `slot` - Es el slot del equipamento donde el atributo hace efecto, puede ser `CHEST`, `FEET`, `HAND`, `HEAD`, `LEGS` o `OFF_HAND`.

### Encantamientos

Los encantamientos en el item.

```yaml
enchants:
  DURABILITY: 3
stored-enchants:
  LURE: 2
```

* `enchants` - Son los encantamientos para aplicar en el item.
* `stored-enchants` - Lo mismo que arriba, pero solo aplica para libros encantados.

### Cabeza con textura

Agrega una textura al item actual solo si este es del material `PLAYER_HEAD`.

```yaml
skull: Rubenicos
```

La textura puede ser el `id único de jugador`, `nombre de jugador`, `textura en base64`, `url de mojang` o `el valor hash de la textura`.

### Items especiales

Hay una gran cantidad de items especiales que pueden ser configurados utilizando el formato de XItemStack, pero por ahora no están documentados en esta wiki, dale un vistazo al [código de XItemStack](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XItemStack.java) para más información.

## Proveedores

Puedes hacer una configuración avanzada del material para obtener un item de otro plugin.

### Oraxen

Obtener items del plugin Oraxen.

```yaml
# Configuración simple
material: 'oraxen:storm_sword'

# Configuración avanzada
material:
  provider: oraxen
  id: storm_sword
```

### MMOItems

Obtener items del plugin MMOItems.

```yaml
# Configuración simple
material: 'mmoitems:SWORD:lava_sword'

# Configuración avanzada
material:
  provider: mmoitems
  type: SWORD
  id: lava_sword
```

### ItemsAdder

No hay soporte actualmente.

## Agregados

Agrega alguna información sobre como se mostrará el item luego de ensamblarlo, esto es útil si quieres hacer un item de otro plugin además de agregarle líneas al lore.

### Nombre

Agrega cosas al nombre del item.

```yaml
# Configuración simple
append:
  name.before: '&eText before'
  name.after: '&bText after'

# Configuración avanzada
append:
  name:
    before: '&eText before'
    after: '&bText after'
```

### Lore

Agrega cosas al lore del item.

```yaml
# Configuración simple
append:
  lore.before:
    - '&eLine before'
  lore.after:
    - '&bLine after'

# Configuración avanzada
append:
  lore:
    before:
      - '&eLine before'
    after:
      - '&bLine after'
```

## NBT

Es la característica más avanzada para editar los tags del item directamente de su data interna.

```yaml
nbt:
  # Hacer que el item sea irrompible en versiones inferiores a la 1.11
  Unbreakable: true
  # Establecer el costo de reparación del item en los yunques
  RepairCost: 10
  # También añadir cualquier cosa por aquí
  some:
    custom:
      tag: 'path'
```

**Tipos de datos:**

* `byte` - Escribirlo como `<número>b`, por ejemplo `tag: 30b`
* `short` - Escribirlo como `<número>s`, por ejemplo `tag: 30s`
* `int` - Escribirlo como `<número>`, por ejemplo `tag: 30`
* `long` - Escribirlo como `<número>L`, por ejemplo `tag: 30L`
* `float` - Escribirlo como `<número>f`, por ejemplo `tag: 30.0f`
* `double` - Escribirlo como `<número>d`, por ejemplo `tag: 30.0d`
* `byte[]` - Escribirlo como `[B; <bytes...>B]`, por ejemplo `tag: [B; 30B, 40B, 50B]`
* `int[]` - Escribirlo como `[I; <integrales...>]`, por ejemplo `tag: [I; 30, 40, 50]`
* `long[]` - Escribirlo como `[L; <longs...>l]`, por ejemplo `tag: [L; 30L, 40L, 50L]`
* `String` - Escribirlo como `<texto>`, por ejemplo `tag: 'Hola'`
