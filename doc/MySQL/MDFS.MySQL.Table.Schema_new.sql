
SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

DROP TABLE IF EXISTS `user-data_group-data`, `meta-data_data-node`, `user-data`, `data-node`, `meta-data`, `group-data`;

-- Table structure for table `data-node`
--

CREATE TABLE `data-node` (
  `name` varchar(256) NOT NULL,
  `address` varchar(256) NOT NULL,
  `port` int(11) NOT NULL,
  PRIMARY KEY (`name`),
  UNIQUE KEY `address` (`address`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `group-data`
--

CREATE TABLE `group-data` (
  `gid` int(11) NOT NULL,
  `name` varchar(256) NOT NULL,
  PRIMARY KEY (`gid`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `meta-data`
--

CREATE TABLE `meta-data` (
  `filePath` varchar(256) NOT NULL,
  `size` bigint(20) DEFAULT NULL,
  `fileType` varchar(128) DEFAULT NULL,
  `storageName` varchar(256) DEFAULT NULL,
  `permission` int(11) DEFAULT NULL,
  `uid` int(11) DEFAULT NULL,
  `gid` int(11) DEFAULT NULL,
  `created` bigint(64) DEFAULT NULL,
  `lastEdited` bigint(64) DEFAULT NULL,
  `lastTouched` bigint(64) DEFAULT NULL,
  PRIMARY KEY (`filePath`)
 
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `meta-data_data-node`
--

CREATE TABLE `meta-data_data-node` (
  `Meta_Data_filePath` varchar(256) NOT NULL,
  `Data_Node_Name` varchar(256) NOT NULL,
  PRIMARY KEY (`Meta_Data_filePath`,`Data_Node_Name`),
  KEY `Data_Node_Name` (`Data_Node_Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `user-data`
--

CREATE TABLE `user-data` (
  `uid` int(11) NOT NULL,
  `name` varchar(256) NOT NULL,
  `pwdHash` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



CREATE TABLE `user-data_group-data` (
  `uid` int(11) NOT NULL,
  `gid` int(11) NOT NULL,
  PRIMARY KEY (`uid`,`gid`),
  KEY `gid` (`gid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Constraints for table `meta-data_data-node`
--
ALTER TABLE `meta-data_data-node` ADD FOREIGN KEY (`Data_Node_Name`) REFERENCES `data-node` (`name`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `meta-data_data-node` ADD FOREIGN KEY (`Meta_Data_filePath`) REFERENCES `meta-data` (`filePath`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `user-data_group-data`
--
ALTER TABLE  `user-data_group-data` ADD FOREIGN KEY (  `uid` ) REFERENCES  `user-data` (`uid`) ON DELETE CASCADE ON UPDATE CASCADE ;
ALTER TABLE  `user-data_group-data` ADD FOREIGN KEY (  `gid` ) REFERENCES  `group-data` (`gid`) ON DELETE CASCADE ON UPDATE CASCADE ;


