-- V4__create_user_sticker.sql

-- UserSticker 테이블 (FK: User, Sticker)
CREATE TABLE IF NOT EXISTS UserSticker (
    userStickerId BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    userId BIGINT NOT NULL,
    stickerId BIGINT NOT NULL,
    createdAt DATETIME(6) NOT NULL DEFAULT (UTC_TIMESTAMP()),

    CONSTRAINT fk_user_sticker_user
    FOREIGN KEY (userId) REFERENCES User (userId) ON DELETE CASCADE,
    CONSTRAINT fk_user_sticker_sticker
    FOREIGN KEY (stickerId) REFERENCES Sticker (stickerId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
