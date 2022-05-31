-- MySQL dump 10.13  Distrib 8.0.28, for macos11.6 (x86_64)
--
-- Host: qa-db.qa.bravofly.intra    Database: obe_stats
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
-- Current Database: `obe_stats`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `obe_stats` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `obe_stats`;

--
-- Table structure for table `TRACKINGS`
--

DROP TABLE IF EXISTS `TRACKINGS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `TRACKINGS` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'tracking id',
  `DETAIL_ID` bigint(20) unsigned NOT NULL COMMENT 'TRACKING_DETAILS id',
  `TRACKING_DATE` datetime NOT NULL COMMENT 'tracking event date',
  `CART_ID` varchar(40) NOT NULL COMMENT 'cart identifier',
  `TRACE_ID` char(36) DEFAULT NULL COMMENT 'cart identifier',
  `TRACING` varchar(512) DEFAULT NULL COMMENT 'complete tracing information',
  `PAYLOAD` text COMMENT 'event custom payload',
  `UNSUPPORTED_EVENT` varchar(64) DEFAULT NULL COMMENT 'unsupported event name',
  `CREATION_DATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'creation date (automatic)',
  PRIMARY KEY (`ID`),
  KEY `IDX_CART` (`CART_ID`),
  KEY `IDX_TRACE` (`TRACE_ID`),
  KEY `FK_DETAILS` (`DETAIL_ID`),
  KEY `IDX_DETAIL_AND_DATE` (`DETAIL_ID`,`TRACKING_DATE`),
  CONSTRAINT `FK_DETAILS` FOREIGN KEY (`DETAIL_ID`) REFERENCES `TRACKING_DETAILS` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=46847085 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TRACKING_DETAILS`
--

DROP TABLE IF EXISTS `TRACKING_DETAILS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `TRACKING_DETAILS` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'tracking detail id',
  `CHANNEL` enum('BACKEND','FRONTEND','INTERNAL') NOT NULL DEFAULT 'INTERNAL' COMMENT 'tracking channel (source)',
  `EVENT_NAME` varchar(64) NOT NULL COMMENT 'event name',
  `DESCRIPTION` varchar(255) NOT NULL COMMENT 'formal definition of the tracking and its purpose',
  `FUNNEL_EVENT` varchar(64) DEFAULT NULL COMMENT 'BI funnel event name',
  `STARTRACK_EVENT_NAME` varchar(64) DEFAULT NULL COMMENT 'startrack event name',
  `STARTRACK_EVENT_VERSION` varchar(4) DEFAULT NULL COMMENT 'startrack event version',
  `ENABLE_METRICS` tinyint(1) DEFAULT '0' COMMENT 'enable metrics on grafana for the tracking',
  `ENABLE_STARTRACK` tinyint(1) NOT NULL DEFAULT '1',
  `ENABLE_PERSISTENCE` tinyint(1) NOT NULL DEFAULT '1',
  `STORE_TRACING_INFO` tinyint(1) DEFAULT '0' COMMENT 'add tracing info to tracking event',
  `STORE_USER_INFO` tinyint(1) DEFAULT '1' COMMENT 'add user info to tracking event',
  `SEVERITY` enum('INFO','WARN','ERROR') DEFAULT 'INFO' COMMENT 'tracking severity',
  `CREATION_DATE` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'creation date (automatic)',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNIQUE_EVENT` (`CHANNEL`,`EVENT_NAME`),
  KEY `EVENT` (`CHANNEL`,`EVENT_NAME`)
) ENGINE=InnoDB AUTO_INCREMENT=6263 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `V_TRACKINGS`
--

DROP TABLE IF EXISTS `V_TRACKINGS`;
/*!50001 DROP VIEW IF EXISTS `V_TRACKINGS`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `V_TRACKINGS` AS SELECT 
 1 AS `CHANNEL`,
 1 AS `EVENT_NAME`,
 1 AS `UNSUPPORTED_EVENT`,
 1 AS `FUNNEL_EVENT`,
 1 AS `TRACKING_DATE`,
 1 AS `CART_ID`,
 1 AS `TRACE_ID`,
 1 AS `TRACING`,
 1 AS `PAYLOAD`*/;
SET character_set_client = @saved_cs_client;

--
-- Current Database: `obe_stats`
--

USE `obe_stats`;

--
-- Final view structure for view `V_TRACKINGS`
--

/*!50001 DROP VIEW IF EXISTS `V_TRACKINGS`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `V_TRACKINGS` AS select `D`.`CHANNEL` AS `CHANNEL`,`D`.`EVENT_NAME` AS `EVENT_NAME`,`V`.`UNSUPPORTED_EVENT` AS `UNSUPPORTED_EVENT`,`D`.`FUNNEL_EVENT` AS `FUNNEL_EVENT`,`V`.`TRACKING_DATE` AS `TRACKING_DATE`,`V`.`CART_ID` AS `CART_ID`,`V`.`TRACE_ID` AS `TRACE_ID`,`V`.`TRACING` AS `TRACING`,`V`.`PAYLOAD` AS `PAYLOAD` from (`TRACKING_DETAILS` `D` join `TRACKINGS` `V` on((`D`.`ID` = `V`.`DETAIL_ID`))) order by `V`.`ID` desc */;
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

-- Dump completed on 2022-05-24 17:29:35
