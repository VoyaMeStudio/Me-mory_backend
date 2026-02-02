-- ============================================
-- V1__init.sql
-- 초기 데이터베이스 스키마 생성
-- ============================================

-- Country 테이블 (FK 없음, 가장 먼저 생성)
CREATE TABLE IF NOT EXISTS `country` (
    `countryCode` VARCHAR(2) NOT NULL PRIMARY KEY,
    `countryName` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    `emoji` VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Emotion 테이블 (FK 없음)
CREATE TABLE IF NOT EXISTS `emotion` (
    `emotion_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    `colorCode` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Weather 테이블 (FK 없음)
CREATE TABLE IF NOT EXISTS `weather` (
    `weather_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    `iconUrl` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TripTheme 테이블 (FK 없음)
CREATE TABLE IF NOT EXISTS `trip_theme` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `themeName` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    `sampleImageUrl` VARCHAR(255),
    `cardImageUrl` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- BoardTheme 테이블 (FK 없음)
CREATE TABLE IF NOT EXISTS `board_theme` (
    `boardThemeId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `themeName` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `thumbnailUrl` VARCHAR(255) NOT NULL,
    `cardUrl` VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sticker 테이블 (FK 없음)
CREATE TABLE IF NOT EXISTS `sticker` (
    `stickerId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `imageUrl` VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User 테이블 (FK 없음)
CREATE TABLE IF NOT EXISTS `user` (
    `userId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `kakaoId` VARCHAR(255) UNIQUE,
    `profileImageUrl` VARCHAR(255),
    `surName` VARCHAR(255),
    `firstName` VARCHAR(255),
    `koreanName` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    `createdAt` DATE DEFAULT (CURRENT_DATE),
    `status` BOOLEAN DEFAULT FALSE,
    `birth` DATE,
    `nationality` VARCHAR(255),
    `isRegistered` BOOLEAN NOT NULL DEFAULT FALSE,
    `diaryCount` BIGINT,
    `visitedCountryCount` BIGINT,
    `flags` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Setting 테이블 (FK: User)
CREATE TABLE IF NOT EXISTS `setting` (
    `userId` BIGINT NOT NULL PRIMARY KEY,
    `alarm` BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT `fk_setting_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Trip 테이블 (FK: User, TripTheme)
CREATE TABLE IF NOT EXISTS `trip` (
    `tripId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `tripName` VARCHAR(14) NOT NULL,
    `description` VARCHAR(56),
    `startDate` DATE NOT NULL,
    `endDate` DATE NOT NULL,
    `isStored` BOOLEAN DEFAULT FALSE,
    `isPast` BOOLEAN DEFAULT FALSE,
    `userId` BIGINT,
    `tripThemeId` BIGINT,
    `content` LONGTEXT,
    `representativeImageUrl` VARCHAR(255),
    CONSTRAINT `fk_trip_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE,
    CONSTRAINT `fk_trip_trip_theme` FOREIGN KEY (`tripThemeId`) REFERENCES `trip_theme` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Diary 테이블 (FK: User, Trip, Country, Emotion, Weather)
CREATE TABLE IF NOT EXISTS `diary` (
    `diaryId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `userId` BIGINT,
    `tripId` BIGINT,
    `countryId` VARCHAR(2),
    `city` VARCHAR(255) NOT NULL,
    `dateTime` DATETIME(6) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `detailedLocation` VARCHAR(255),
    `emotionId` BIGINT,
    `weatherId` BIGINT,
    `createdAt` DATETIME(6) DEFAULT (UTC_TIMESTAMP()),
    `isStored` BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT `fk_diary_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE,
    CONSTRAINT `fk_diary_trip` FOREIGN KEY (`tripId`) REFERENCES `trip` (`tripId`) ON DELETE CASCADE,
    CONSTRAINT `fk_diary_country` FOREIGN KEY (`countryId`) REFERENCES `country` (`countryCode`),
    CONSTRAINT `fk_diary_emotion` FOREIGN KEY (`emotionId`) REFERENCES `emotion` (`emotion_id`),
    CONSTRAINT `fk_diary_weather` FOREIGN KEY (`weatherId`) REFERENCES `weather` (`weather_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- DiaryImage 테이블 (FK: Diary)
CREATE TABLE IF NOT EXISTS `diary_image` (
    `DiaryImageId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `diaryId` BIGINT,
    `imageUrl` VARCHAR(255) NOT NULL,
    `cameraType` VARCHAR(255) NOT NULL,
    `isRepresentative` BOOLEAN DEFAULT FALSE,
    `imageOrder` INT,
    CONSTRAINT `fk_diary_image_diary` FOREIGN KEY (`diaryId`) REFERENCES `diary` (`diaryId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- VisitedCountry 테이블 (FK: User, Country, Emotion)
CREATE TABLE IF NOT EXISTS `visited_country` (
    `visitedCountryId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `color` VARCHAR(255),
    `createdAt` DATETIME(6) DEFAULT (UTC_TIMESTAMP()),
    `userId` BIGINT,
    `countryCode` VARCHAR(2),
    `emotionId` BIGINT,
    CONSTRAINT `fk_visited_country_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE,
    CONSTRAINT `fk_visited_country_country` FOREIGN KEY (`countryCode`) REFERENCES `country` (`countryCode`),
    CONSTRAINT `fk_visited_country_emotion` FOREIGN KEY (`emotionId`) REFERENCES `emotion` (`emotion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Board 테이블 (FK: User, BoardTheme)
CREATE TABLE IF NOT EXISTS `board` (
    `boardId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(100) NOT NULL,
    `createdAt` DATETIME(6) NOT NULL DEFAULT (UTC_TIMESTAMP()),
    `updatedAt` DATETIME(6),
    `userId` BIGINT NOT NULL,
    `boardThemeId` BIGINT NOT NULL,
    CONSTRAINT `fk_board_user` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE CASCADE,
    CONSTRAINT `fk_board_board_theme` FOREIGN KEY (`boardThemeId`) REFERENCES `board_theme` (`boardThemeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- BoardStickerMap 테이블 (FK: Board, Sticker)
CREATE TABLE IF NOT EXISTS `board_sticker_map` (
    `boardStickerId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `boardId` BIGINT NOT NULL,
    `stickerId` BIGINT NOT NULL,
    `posX` DECIMAL(19,2) NOT NULL,
    `posY` DECIMAL(19,2) NOT NULL,
    `rotation` DECIMAL(19,2) NOT NULL,
    CONSTRAINT `fk_board_sticker_map_board` FOREIGN KEY (`boardId`) REFERENCES `board` (`boardId`) ON DELETE CASCADE,
    CONSTRAINT `fk_board_sticker_map_sticker` FOREIGN KEY (`stickerId`) REFERENCES `sticker` (`stickerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AlarmHistory 테이블 (FK 없음, 하지만 User, Trip 참조)
CREATE TABLE IF NOT EXISTS `alarm_history` (
    `alarmHistoryId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `userId` BIGINT NOT NULL,
    `tripId` BIGINT NOT NULL,
    `alarmType` VARCHAR(50) NOT NULL,
    `sentAt` DATETIME NOT NULL,
    `sentDate` DATE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
