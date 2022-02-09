CREATE TABLE `twitch`.`testentity` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `name_unique` UNIQUE (`name`)
  );
