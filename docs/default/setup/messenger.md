---
sidebar_position: 3
title: Messenger
description: PixelBuy messenger setup.
---

A messenger hook allows the plugin to synchronize data between hosts that are using the same external database.

Not setting a messenger instance may lead to data loss.

This concept also uses a main channel to transfer messages, every server with a messenger instance must use the same channel name.

PixelBuy currently supports `SQL`, `REDIS` and `RABBITMQ` messenger types, set `AUTO` to let the plugin choose the best available option.

## Sql

The `SQL` messenger type uses the current sql database to transfer messages by adding rows into a separated table, then checks for any unread row (probably added by another plugin instance on a different host).

Every 30 seconds, all rows more than 60 seconds old are automatically deleted by every plugin instance connected at the same `SQL` messenger instance.

## Redis

The `REDIS` messenger type connects to a Redis instance to transfer messages, there's not much to say since it's a highly known system.

## RabbitMQ

The `RABBITMQ` messenger type connects to a RabbitMQ instance to transfer temporary messages, instead of Redis this type of messaging is more standardized (and probably better), additionally it requires to set an exchange or "pre-channel" to access messenger channels.