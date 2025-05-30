-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: users
-- ------------------------------------------------------
-- Server version	8.0.39

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
-- Table structure for table `newusers`
--

DROP TABLE IF EXISTS `newusers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `newusers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `newusers`
--

LOCK TABLES `newusers` WRITE;
/*!40000 ALTER TABLE `newusers` DISABLE KEYS */;
INSERT INTO `newusers` VALUES (11,'admin','$2a$10$neUn1kQ4rxpbk168Cvr45ucXmSdv4CpqTi58d5oRhgDU32u4MQP5.',NULL,'admin',0),(18,'newUser','$2a$10$Oyn3l0XzPsvTFhF52CZtuu48vBJ0RkMNioGpaGQH8yde5dsFnLPHa','NewUser@mail.ru','user',0),(20,'LittlePunsh5','$2a$10$Rfx3jHcZ9EayH7/lWzLYnOKJtzd9JciLFq5JYwYIlUe0JyUaM7l4G','LittlePunsh@mail.ru','user',0),(21,'apiuser','$2a$10$zvaOttU3c8qSIJUIbRIV8eFltP19DxW/Mh8N72ur59KiZHgDoXS/a','apiuser@example.com','user',0),(22,'User2','$2a$10$CYMP1psnE6Gm2NYeFlqnuOA2/0MXw20UP8PSncYHk9ADlBWwD9ohy','User2@example.com','user',0),(23,'User3','$2a$10$Yf1FUDgBytuDHzxxT.IAguic8URAcROcuAOktfMB8wdn5n6h98TkO','User3@example.com','user',0),(24,'proverka','$2a$10$qSKNqsMnrPKufwwdDFHIluMptas.waaUAXs7uZvDzyGXFHzVDzr7a','proverka@example.com','user',0),(25,'proverka3','$2a$10$JWtcB2o3BjK7Vg3rRrBRS.tNaas0YaHMHsHE.eXBV16/qPrklE.0C','proverka3@example.com','user',0),(26,'proverka4','$2a$10$sHdxDniMNOrq6ZbvHmxYVuzEviKcXoAyeq.f7QAjj/4zDDccQXiL6','proverka4@example.com','user',1),(27,'proverka5','$2a$10$CcKKZScLG/QaiJbaomibuOeApwihGD.e9W3g6b/R5CY3B.4SH28zS','proverka5@example.com','user',1),(28,'proverka55','$2a$10$t.sH5YzbF0LH7EMRiRlTN.Ha0ZTq3.rli.O4tNHVjLyA62cnrKfrm','proverka655@example.com','user',0);
/*!40000 ALTER TABLE `newusers` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-17 16:51:02
