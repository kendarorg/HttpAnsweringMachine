-- MySQL dump 10.13  Distrib 8.0.28, for macos11.6 (x86_64)
--
-- Host: qa-db.qa.bravofly.intra    Database: obe
-- ------------------------------------------------------
-- Server version	5.7.35-38-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `obe`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `obe` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

USE `obe`;

--
-- Table structure for table `DYNAMIC_MESSAGES`
--

DROP TABLE IF EXISTS `DYNAMIC_MESSAGES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `DYNAMIC_MESSAGES` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'dynamic message identifier',
  `FORMULA` text COMMENT 'formula',
  `PRIORITY` int(11) DEFAULT NULL COMMENT 'formula priority',
  `TYPE` enum('WARNING','SUCCESS','DANGER','INFO','POSITIVE_INFO') NOT NULL COMMENT 'message type',
  `I18N_KEY` varchar(128) NOT NULL COMMENT 'i18n key to display',
  `CREATION_DATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'creation date (automatic)',
  `EXPANDABLE` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EVENTS`
--

DROP TABLE IF EXISTS `EVENTS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `EVENTS` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'cart event identifier',
  `CREATION_DATE` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation date (automatic)',
  `CART_ID` varchar(40) NOT NULL COMMENT 'cart identifier',
  `USER_ID` varchar(40) DEFAULT NULL COMMENT 'user UID',
  `ACCOUNT_HASH` varchar(40) DEFAULT NULL COMMENT 'Account hash',
  `EVENT_TYPE` varchar(32) NOT NULL COMMENT 'cart event type',
  `VERSION` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'content version encoding',
  `CONTENT` text COMMENT 'content of the cart event',
  PRIMARY KEY (`ID`,`CREATION_DATE`),
  KEY `CART_ID` (`CART_ID`),
  KEY `CREATION_DATE` (`CREATION_DATE`)
) ENGINE=InnoDB AUTO_INCREMENT=2082470912 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FULFILL_CHECKER`
--

DROP TABLE IF EXISTS `FULFILL_CHECKER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `FULFILL_CHECKER` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'rule identifier',
  `FORMULA` text COMMENT 'content of the cart event',
  `CREATION_DATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'creation date (automatic)',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MAGIC_METHOD_TRACKER`
--

DROP TABLE IF EXISTS `MAGIC_METHOD_TRACKER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `MAGIC_METHOD_TRACKER` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `TOKEN_ID` varchar(64) NOT NULL,
  `CREATION_DATE` datetime NOT NULL,
  `TYPE` varchar(40) NOT NULL,
  PRIMARY KEY (`ID`,`CREATION_DATE`),
  UNIQUE KEY `TOKEN_ID` (`TOKEN_ID`,`CREATION_DATE`)
) ENGINE=InnoDB AUTO_INCREMENT=4597 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RECENT_TRANSPORT_ORDERS`
--

DROP TABLE IF EXISTS `RECENT_TRANSPORT_ORDERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `RECENT_TRANSPORT_ORDERS` (
  `ID_ORDER` bigint(20) unsigned NOT NULL,
  `CREATION_DATETIME` datetime NOT NULL,
  `START_DATE` date NOT NULL,
  `END_DATE` date DEFAULT NULL,
  `DATA` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`ID_ORDER`),
  KEY `search_index` (`CREATION_DATETIME`,`START_DATE`,`END_DATE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `SETTINGS`
--

DROP TABLE IF EXISTS `SETTINGS`;
/*!50001 DROP VIEW IF EXISTS `SETTINGS`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `SETTINGS` AS SELECT 
 1 AS `SETTING_KEY`,
 1 AS `SETTING_SUB_KEY`,
 1 AS `VALUE_TYPE`,
 1 AS `DEFAULT_VALUE`,
 1 AS `MARKET`,
 1 AS `BRAND`,
 1 AS `ID_BUSINESS_PROFILE`,
 1 AS `SETTING_VALUE`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `SETTING_KEYS`
--

DROP TABLE IF EXISTS `SETTING_KEYS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `SETTING_KEYS` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'setting key identifier',
  `SETTING_KEY` varchar(32) NOT NULL COMMENT 'setting key',
  `SETTING_SUB_KEY` varchar(32) DEFAULT NULL COMMENT 'setting sub_key',
  `VALUE_TYPE` enum('STRING','INTEGER','BOOLEAN','STRING_LIST','INTEGER_LIST','BOOLEAN_LIST') NOT NULL DEFAULT 'STRING' COMMENT 'value content type',
  `DEFAULT_VALUE` text COMMENT 'default value in case of no override in SETTING_VALUES',
  `DESCRIPTION` varchar(1024) NOT NULL COMMENT 'formal definition of the setting and its purpose',
  `CREATION_DATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'creation date (automatic)',
  PRIMARY KEY (`ID`),
  KEY `KEYS` (`SETTING_KEY`,`SETTING_SUB_KEY`)
) ENGINE=InnoDB AUTO_INCREMENT=841 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SETTING_VALUES`
--

DROP TABLE IF EXISTS `SETTING_VALUES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `SETTING_VALUES` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'setting value identifier',
  `MARKET` varchar(5) DEFAULT NULL COMMENT 'market granularity (ex: it, en_gb, etc)',
  `BRAND` varchar(50) DEFAULT NULL COMMENT 'Brand name like in masterdata',
  `ID_BUSINESS_PROFILE` varchar(32) DEFAULT NULL COMMENT 'business profile granularity (ex: VACANZEVOLAGRATISIT, HOLIDAYSLASTMINUTECOUK)',
  `KEY_ID` bigint(20) unsigned NOT NULL COMMENT 'SETTING_KEYS id',
  `SETTING_VALUE` varchar(1024) DEFAULT NULL COMMENT 'value redefinition of SETTING_KEYS.DEFAULT_VALUE',
  `CREATION_DATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'creation date (automatic)',
  PRIMARY KEY (`ID`),
  KEY `IDX_FILTERS` (`MARKET`,`ID_BUSINESS_PROFILE`),
  KEY `IDX_BUSINESS_PROFILE` (`ID_BUSINESS_PROFILE`),
  KEY `FK_KEYS` (`KEY_ID`),
  CONSTRAINT `FK_KEYS` FOREIGN KEY (`KEY_ID`) REFERENCES `SETTING_KEYS` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3053 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SNAPSHOTS`
--

DROP TABLE IF EXISTS `SNAPSHOTS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `SNAPSHOTS` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'cart event identifier',
  `CREATION_DATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'creation date (automatic)',
  `CART_ID` varchar(40) NOT NULL COMMENT 'cart identifier',
  `USER_ID` varchar(40) DEFAULT NULL COMMENT 'user UID',
  `EVENT_ID` bigint(20) unsigned NOT NULL COMMENT 'the snapshot contains all cart events until this one',
  `CONTENT` text COMMENT 'content of the snapshot',
  PRIMARY KEY (`ID`),
  KEY `CART_ID` (`CART_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `STEPS`
--

DROP TABLE IF EXISTS `STEPS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `STEPS` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Step id',
  `NAME` varchar(100) NOT NULL COMMENT 'Step name',
  `CONFIGURATION` mediumtext NOT NULL COMMENT 'Configuration',
  `VERSION` bigint(20) DEFAULT NULL,
  `ACTIVE` tinyint(4) DEFAULT NULL,
  `WIZARD` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `NAME` (`NAME`)
) ENGINE=InnoDB AUTO_INCREMENT=683 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `THREE_DS_DATA`
--

DROP TABLE IF EXISTS `THREE_DS_DATA`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `THREE_DS_DATA` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `PAYMENT_DATA` text,
  `CART_ID` varchar(40) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=764 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TOUCH_POINTS`
--

DROP TABLE IF EXISTS `TOUCH_POINTS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `TOUCH_POINTS` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'table id',
  `TOUCH_POINT` varchar(40) NOT NULL COMMENT 'touch point identifier',
  `LAYOUT_NAME` varchar(100) NOT NULL COMMENT 'custom layout for touch point',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `TOUCH_POINT` (`TOUCH_POINT`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TOUCH_POINT_VALUE`
--

DROP TABLE IF EXISTS `TOUCH_POINT_VALUE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `TOUCH_POINT_VALUE` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'touch point value identifier',
  `TOUCH_POINT_ID` bigint(20) unsigned NOT NULL COMMENT 'TOUCH_POINT id',
  `SETTING_ID` bigint(20) unsigned NOT NULL COMMENT 'SETTING_KEYS id',
  `SETTING_VALUE` varchar(1024) NOT NULL,
  `CREATION_DATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'creation date (automatic)',
  PRIMARY KEY (`ID`),
  KEY `TOUCH_POINT_ID` (`TOUCH_POINT_ID`),
  KEY `SETTING_ID` (`SETTING_ID`),
  CONSTRAINT `TOUCH_POINT_VALUE_ibfk_1` FOREIGN KEY (`TOUCH_POINT_ID`) REFERENCES `TOUCH_POINTS` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `TOUCH_POINT_VALUE_ibfk_2` FOREIGN KEY (`SETTING_ID`) REFERENCES `SETTING_KEYS` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=160 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `WIDGETS`
--

DROP TABLE IF EXISTS `WIDGETS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `WIDGETS` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'widget identifier',
  `NAME` varchar(100) NOT NULL COMMENT 'widget name',
  `DESCRIPTION` varchar(1024) NOT NULL COMMENT 'widget description',
  `PRELOAD_REQUIRED` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`ID`),
  KEY `KEYS` (`NAME`)
) ENGINE=InnoDB AUTO_INCREMENT=174 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Current Database: `obe`
--

USE `obe`;

--
-- Final view structure for view `SETTINGS`
--

/*!50001 DROP VIEW IF EXISTS `SETTINGS`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`c_developer`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `SETTINGS` AS select `k`.`SETTING_KEY` AS `SETTING_KEY`,`k`.`SETTING_SUB_KEY` AS `SETTING_SUB_KEY`,`k`.`VALUE_TYPE` AS `VALUE_TYPE`,`k`.`DEFAULT_VALUE` AS `DEFAULT_VALUE`,`v`.`MARKET` AS `MARKET`,`v`.`BRAND` AS `BRAND`,`v`.`ID_BUSINESS_PROFILE` AS `ID_BUSINESS_PROFILE`,`v`.`SETTING_VALUE` AS `SETTING_VALUE` from (`SETTING_KEYS` `k` left join `SETTING_VALUES` `v` on((`k`.`ID` = `v`.`KEY_ID`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-05-24 17:12:40
