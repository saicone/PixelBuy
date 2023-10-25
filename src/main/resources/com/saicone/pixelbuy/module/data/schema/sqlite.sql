-- create:users_table

CREATE TABLE `{prefix}users` (
  `uuid`     VARCHAR(36) NOT NULL,
  `username` VARCHAR(20),
  `donated`  FLOAT       NOT NULL,
  PRIMARY KEY (`uuid`)
);
CREATE INDEX `{prefix}users_username` ON `{prefix}users` (`username`);

-- create:orders_table

CREATE TABLE `{prefix}orders` (
  `id`        INTEGER PRIMARY KEY NOT NULL,
  `provider`  VARCHAR(255)        NOT NULL,
  `order`     INTEGER             NOT NULL,
  `group`     VARCHAR(255)        NOT NULL,
  `buyer`     VARCHAR(36)         NOT NULL,
  `time`      VARCHAR(255)        NOT NULL,
  `execution` VARCHAR(255)        NOT NULL,
  `items`     TEXT                NOT NULL
);
CREATE INDEX `{prefix}orders_buyer` ON `{prefix}orders` (`buyer`);

-- select:user

SELECT * FROM `{prefix}users` WHERE `username` = ?;

-- select:users

SELECT ALL * FROM `{prefix}users` WHERE `username` IS NOT NULL;

-- select:orders

SELECT ALL * FROM `{prefix}orders` WHERE `buyer` = ?;

-- insert:user

INSERT OR REPLACE INTO `{prefix}users` (
  `uuid`,
  `username`,
  `donated`
) VALUES (?, ?, ?);

-- insert:order

INSERT INTO `{prefix}orders` (
  `provider`,
  `order`,
  `group`,
  `buyer`,
  `time`,
  `execution`,
  `items`
) VALUES (?, ?, ?, ?, ?, ?, ?);

-- update:order

UPDATE `{prefix}orders` SET
  `buyer` = ?,
  `time` = ?,
  `execution` = ?,
  `items` = ?
WHERE `id` = ?;
