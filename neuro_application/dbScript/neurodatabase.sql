-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema neurodatabase
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema neurodatabase
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `neurodatabase` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `neurodatabase` ;

-- -----------------------------------------------------
-- Table `neurodatabase`.`device`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `neurodatabase`.`device` ;

CREATE TABLE IF NOT EXISTS `neurodatabase`.`device` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `devicename` VARCHAR(255) NOT NULL,
  `devicetype` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `neurodatabase`.`inimagestore`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `neurodatabase`.`inimagestore` ;

CREATE TABLE IF NOT EXISTS `neurodatabase`.`inimagestore` (
  `subjectid` VARCHAR(45) NOT NULL,
  `template` LONGBLOB NOT NULL,
  `img_id` INT(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`img_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `neurodatabase`.`insideoutinfo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `neurodatabase`.`insideoutinfo` ;

CREATE TABLE IF NOT EXISTS `neurodatabase`.`insideoutinfo` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `score` INT(11) NOT NULL,
  `age` INT(11) NOT NULL,
  `gender` VARCHAR(255) NOT NULL,
  `islive` INT(11) NOT NULL,
  `timestamp` DATE NULL DEFAULT NULL,
  `type` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `neurodatabase`.`outimagestore`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `neurodatabase`.`outimagestore` ;

CREATE TABLE IF NOT EXISTS `neurodatabase`.`outimagestore` (
  `subjectid` VARCHAR(45) NOT NULL,
  `template` LONGBLOB NOT NULL,
  `img_id` INT(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`img_id`))
ENGINE = InnoDB
AUTO_INCREMENT = 17
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `neurodatabase`.`subjectinfo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `neurodatabase`.`subjectinfo` ;

CREATE TABLE IF NOT EXISTS `neurodatabase`.`subjectinfo` (
  `subject_id` INT(10) NOT NULL AUTO_INCREMENT,
  `subject_title` VARCHAR(45) NOT NULL,
  `subject_img` LONGBLOB NOT NULL,
  PRIMARY KEY (`subject_id`))
ENGINE = InnoDB
AUTO_INCREMENT = 17
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
