-- create:users_table

CREATE TABLE `{prefix}users` (
  `uuid`     VARCHAR(36) NOT NULL,
  `username` VARCHAR(20),
  `donated`  REAL        NOT NULL,
  PRIMARY KEY (`uuid`)
);
CREATE INDEX `{prefix}users_username` ON `{prefix}users` (`username`);

-- create:orders_table

CREATE TABLE `{prefix}orders` (
  `id`        INTEGER AUTO_INCREMENT NOT NULL,
  `provider`  VARCHAR(255)           NOT NULL,
  `order`     INTEGER                NOT NULL,
  `group`     VARCHAR(255)           NOT NULL,
  `buyer`     VARCHAR(36)            NOT NULL,
  `time`      VARCHAR(255)           NOT NULL,
  `execution` VARCHAR(255)           NOT NULL,
  `items`     CLOB                   NOT NULL
);
CREATE INDEX `{prefix}orders_buyer` ON `{prefix}orders` (`buyer`);

-- select:user

SELECT * FROM `{prefix}users` WHERE `username` = ?;

-- select:order

SELECT * FROM `{prefix}orders` WHERE `provider` = ? AND `order` = ? AND `group` = ?;

-- select:users

SELECT ALL * FROM `{prefix}users` WHERE `username` IS NOT NULL;

-- select:orders

SELECT ALL * FROM `{prefix}orders` WHERE `buyer` = ?;

-- insert:user

MERGE INTO `{prefix}users` (
  `uuid`,
  `username`,
  `donated`
) KEY (`uuid`) VALUES (?, ?, ?);

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

-- delete:order

DELETE FROM `{prefix}orders` WHERE `provider` = ? AND `order` = ?;

-- update:order

UPDATE `{prefix}orders` SET
  `buyer` = ?,
  `time` = ?,
  `execution` = ?,
  `items` = ?
WHERE `id` = ?;
