---
sidebar_position: 2
title: Database
description: PixelBuy database setup.
---

PixelBuy currently supports `SQL` database type.

## User

### Load all

Load or not all user information from database into memory, the information will include:

* Player name
* Player unique id
* Donated amount

This option also makes available the top donors option.

## Top

Is the top donors configuration.

### Limit

Limit by number the amount of available entries for top donors, or set to `-1` to calculate a top position for every user.

### Time

Time in ticks between top calculations.

A lower number may cause lag in most servers, set `-1` to disable.

## Sql

Is the `SQL` database type configuration, only edit this parameters if you set `SQL` as database type.

The `SQL` database type also have support for different sql instances:

| Type       | Connection | Description                                                       |
|------------|------------|-------------------------------------------------------------------|
| H2         | Local      | The **recommended** database instance for any local data save.    |
| SQLITE     | Local      | Is a more slow database type for local instances.                 |
| MYSQL      | External   | Most common database type on Minecraft server plugins.            |
| MARIADB    | External   | Similar to MySQL but faster.                                      |
| POSTGRESQL | External   | The **recommended** database instance for any external data save. |

If the selected database is a local type you can ignore the rest of the configuration.