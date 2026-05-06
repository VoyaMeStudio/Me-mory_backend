CREATE TABLE IF NOT EXISTS `TripCountry` (
    `tripCountryId` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `tripId` BIGINT NOT NULL,
    `countryCode` VARCHAR(2) NOT NULL,
    `createdAt` DATETIME(6) NOT NULL DEFAULT (UTC_TIMESTAMP()),
    UNIQUE KEY `uk_trip_country_trip_country` (`tripId`, `countryCode`),
    CONSTRAINT `fk_trip_country_trip` FOREIGN KEY (`tripId`) REFERENCES `Trip` (`tripId`) ON DELETE CASCADE,
    CONSTRAINT `fk_trip_country_country` FOREIGN KEY (`countryCode`) REFERENCES `Country` (`countryCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
