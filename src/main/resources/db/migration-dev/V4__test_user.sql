-- ============================================
-- V4__test_user.sql
-- 개발 환경 전용 테스트 사용자 데이터 삽입
-- 운영 환경에서는 실행되지 않음 (application-prod.yml에서 db/migration만 로드)
-- INSERT IGNORE를 사용하여 이미 데이터가 있으면 실행하지 않음
-- ============================================

-- 테스트 사용자 삽입 (이미 존재하는 경우 무시)
INSERT IGNORE INTO `User` (
    `kakaoId`,
    `profileImageUrl`,
    `surName`,
    `firstName`,
    `koreanName`,
    `birth`,
    `nationality`,
    `createdAt`,
    `status`,
    `isRegistered`,
    `diaryCount`,
    `visitedCountryCount`,
    `flags`
) VALUES (
    'test_강지혜',
    'https://me-mory.mooo.com/api/files?key=images/7c24aa61-2d36-48fd-80a5-646ac518256d_IMG_3831.jpg',
    'KANG',
    'JIHYE',
    '강지혜',
    '1995-03-15',
    'REPUBLIC OF KOREA',
    CURRENT_DATE,
    TRUE,
    FALSE,
    0,
    0,
    '🇰🇷'
);

-- 테스트 사용자의 Setting 생성 (이미 존재하는 경우 무시)
INSERT IGNORE INTO `Setting` (`userId`, `alarm`)
SELECT u.`userId`, TRUE
FROM `User` u
WHERE u.`kakaoId` = 'test_강지혜'
AND NOT EXISTS (
    SELECT 1 FROM `Setting` s WHERE s.`userId` = u.`userId`
);
