
SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

DROP  TABLE IF EXISTS `user-data`, `meta-data_data-node`, `data-node`, `meta-data`;

-- Table structure for table `data-node`
--

CREATE TABLE IF NOT EXISTS `data-node` (
  `name` varchar(256) NOT NULL,
  `address` varchar(256) NOT NULL,
  `port` int(11) NOT NULL,
  PRIMARY KEY (`name`),
  UNIQUE KEY `address` (`address`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `group-date`
--

CREATE TABLE IF NOT EXISTS `group-date` (
  `gid` int(11) NOT NULL,
  `name` varchar(256) NOT NULL,
  PRIMARY KEY (`gid`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `meta-data`
--

CREATE TABLE IF NOT EXISTS `meta-data` (
  `filePath` varchar(256) NOT NULL,
  `size` bigint(20) DEFAULT NULL,
  `fileType` varchar(128) DEFAULT NULL,
  `storageName` varchar(256) DEFAULT NULL,
  `permission` int(11) DEFAULT NULL,
  `uid` int(11) DEFAULT NULL,
  `gid` int(11) DEFAULT NULL,
  `created` int(11) DEFAULT NULL,
  `lastEdited` int(11) DEFAULT NULL,
  `lastTouched` int(11) DEFAULT NULL,
  PRIMARY KEY (`filePath`),
  KEY `owner` (`owner`),
  KEY `group` (`group`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `meta-data_data-node`
--

CREATE TABLE IF NOT EXISTS `meta-data_data-node` (
  `Meta_Data_filePath` varchar(256) NOT NULL,
  `Data_Node_Name` varchar(256) NOT NULL,
  PRIMARY KEY (`Meta_Data_filePath`,`Data_Node_Name`),
  KEY `Data_Node_Name` (`Data_Node_Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `user-data`
--

CREATE TABLE IF NOT EXISTS `user-data` (
  `uid` int(11) NOT NULL,
  `name` varchar(256) NOT NULL,
  `pwdHash` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



--
-- Constraints for table `meta-data`
--
ALTER TABLE `meta-data`
  ADD CONSTRAINT `meta@002ddata_ibfk_2` FOREIGN KEY (`group`) REFERENCES `group-date` (`gid`) ON DELETE NO ACTION ON UPDATE CASCADE,
  ADD CONSTRAINT `meta@002ddata_ibfk_1` FOREIGN KEY (`owner`) REFERENCES `user-data` (`uid`) ON DELETE NO ACTION ON UPDATE CASCADE;

--
-- Constraints for table `meta-data_data-node`
--
ALTER TABLE `meta-data_data-node`
  ADD CONSTRAINT `meta@002ddata_data@002dnode_ibfk_2` FOREIGN KEY (`Data_Node_Name`) REFERENCES `data-node` (`name`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `meta@002ddata_data@002dnode_ibfk_1` FOREIGN KEY (`Meta_Data_filePath`) REFERENCES `meta-data` (`filePath`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `user-data_group-data`
--
ALTER TABLE `user-data_group-data`
  ADD CONSTRAINT `user@002ddata_group@002ddata_ibfk_2` FOREIGN KEY (`gid`) REFERENCES `group-date` (`gid`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user@002ddata_group@002ddata_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `user-data` (`uid`) ON DELETE CASCADE ON UPDATE CASCADE;
