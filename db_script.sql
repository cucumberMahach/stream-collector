-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema twitch-collector
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema twitch-collector
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `twitch-collector` DEFAULT CHARACTER SET utf8 ;
USE `twitch-collector` ;

-- -----------------------------------------------------
-- Table `twitch-collector`.`circles`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`circles` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `number` BIGINT UNSIGNED NOT NULL,
  `startTime` DATETIME(3) NULL,
  `endTime` DATETIME(3) NULL,
  `totalChannels` INT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `twitch-collector`.`channels`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`channels` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `lastCircle_id` BIGINT UNSIGNED NULL,
  `name` VARCHAR(45) NOT NULL,
  `lastCheckedTime` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC),
  INDEX `fk_channels_circles1_idx` (`lastCircle_id` ASC),
  CONSTRAINT `fk_channels_circles1`
    FOREIGN KEY (`lastCircle_id`)
    REFERENCES `twitch-collector`.`circles` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `twitch-collector`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `twitch-collector`.`user_types`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`user_types` (
  `id` TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `twitch-collector`.`users_channels`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`users_channels` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `channel_id` BIGINT UNSIGNED NOT NULL,
  `firstCircle_id` BIGINT UNSIGNED NULL,
  `lastCircle_id` BIGINT UNSIGNED NULL,
  `type_id` TINYINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_users_has_channels_channels1_idx` (`channel_id` ASC),
  INDEX `fk_users_has_channels_users_idx` (`user_id` ASC),
  INDEX `fk_users_channels_circles1_idx` (`firstCircle_id` ASC),
  INDEX `fk_users_channels_circles2_idx` (`lastCircle_id` ASC),
  INDEX `fk_users_channels_user_types1_idx` (`type_id` ASC),
  CONSTRAINT `fk_users_has_channels_users`
    FOREIGN KEY (`user_id`)
    REFERENCES `twitch-collector`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_users_has_channels_channels1`
    FOREIGN KEY (`channel_id`)
    REFERENCES `twitch-collector`.`channels` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_users_channels_circles1`
    FOREIGN KEY (`firstCircle_id`)
    REFERENCES `twitch-collector`.`circles` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_users_channels_circles2`
    FOREIGN KEY (`lastCircle_id`)
    REFERENCES `twitch-collector`.`circles` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_users_channels_user_types1`
    FOREIGN KEY (`type_id`)
    REFERENCES `twitch-collector`.`user_types` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `twitch-collector`.`channelstocheck`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`channelstocheck` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `priority` INT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `twitch-collector`.`channels_circles`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`channels_circles` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `circle_id` BIGINT UNSIGNED NOT NULL,
  `channel_id` BIGINT UNSIGNED NOT NULL,
  `collectTime` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_channels_circles_circles1_idx` (`circle_id` ASC),
  INDEX `fk_channels_circles_channels1_idx` (`channel_id` ASC),
  CONSTRAINT `fk_channels_circles_circles1`
    FOREIGN KEY (`circle_id`)
    REFERENCES `twitch-collector`.`circles` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_channels_circles_channels1`
    FOREIGN KEY (`channel_id`)
    REFERENCES `twitch-collector`.`channels` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `twitch-collector`.`tgusers`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`tgusers` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tg_id` VARCHAR(25) NOT NULL,
  `firstName` VARCHAR(64) NOT NULL,
  `lastName` VARCHAR(64) NULL,
  `username` VARCHAR(32) NULL,
  `language` VARCHAR(8) NOT NULL,
  `messagesTotal` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `state` VARCHAR(100) NOT NULL,
  `firstOnlineTime` DATETIME(3) NOT NULL,
  `lastOnlineTime` DATETIME(3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `tg_id_UNIQUE` (`tg_id` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `twitch-collector`.`tghistory`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`tghistory` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tgUser_id` BIGINT UNSIGNED NOT NULL,
  `message` VARCHAR(300) NOT NULL,
  `result` VARCHAR(100) NULL,
  `messageTime` DATETIME(3) NOT NULL,
  `requestTime` DATETIME(3) NOT NULL,
  `answerTime` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_tgHistory_tgUsers1_idx` (`tgUser_id` ASC),
  CONSTRAINT `fk_tgHistory_tgUsers1`
    FOREIGN KEY (`tgUser_id`)
    REFERENCES `twitch-collector`.`tgusers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `twitch-collector`.`tgbans`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitch-collector`.`tgbans` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tgUser_id` BIGINT UNSIGNED NOT NULL,
  `reason` VARCHAR(100) NOT NULL,
  `fromTime` DATETIME(3) NOT NULL,
  `untilTime` DATETIME(3) NOT NULL,
  INDEX `fk_tgBans_tgUsers1_idx` (`tgUser_id` ASC),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tgBans_tgUsers1`
    FOREIGN KEY (`tgUser_id`)
    REFERENCES `twitch-collector`.`tgusers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `twitch-collector`.`user_types`
-- -----------------------------------------------------
START TRANSACTION;
USE `twitch-collector`;
INSERT INTO `twitch-collector`.`user_types` (`id`, `type`) VALUES (1, 'admin');
INSERT INTO `twitch-collector`.`user_types` (`id`, `type`) VALUES (2, 'staff');
INSERT INTO `twitch-collector`.`user_types` (`id`, `type`) VALUES (3, 'broadcaster');
INSERT INTO `twitch-collector`.`user_types` (`id`, `type`) VALUES (4, 'moderator');
INSERT INTO `twitch-collector`.`user_types` (`id`, `type`) VALUES (5, 'vip');
INSERT INTO `twitch-collector`.`user_types` (`id`, `type`) VALUES (6, 'viewer');

COMMIT;

