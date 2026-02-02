-- ============================================
-- V2__insert_basic_data.sql
-- 기초 데이터 삽입 (TripTheme, Emotion, Weather, BoardTheme, Sticker)
-- ============================================

-- TripTheme 데이터 삽입
INSERT INTO `trip_theme` (`themeName`, `sampleImageUrl`, `cardImageUrl`) VALUES
('기본', 'https://me-mory01.mooo.com/api/files?key=images/d69be93f-fef6-4a73-815f-ea6b93fd2d59_trip_thumb_default.png', 'https://me-mory01.mooo.com/api/files?key=images/6e56681d-9fc3-43d2-a803-0ef165c63c3c_trip_card_default.png'),
('Grey', 'https://me-mory01.mooo.com/api/files?key=images/f0d5e1f7-2718-4dac-a780-d6f6e5f2d42a_trip_thumb_grey.png', 'https://me-mory01.mooo.com/api/files?key=images/462d91ad-b618-4449-805f-35f07187247b_trip_card_grey.png'),
('탑승권', 'https://me-mory01.mooo.com/api/files?key=images/cbcf48b8-8469-4e0f-b597-7ef27cc6190e_trip_thumb_boardingpass.png', 'https://me-mory01.mooo.com/api/files?key=images/3e637461-f9a2-477d-ae06-4245033174a9_trip_card_boardingpass.png'),
('액자', 'https://me-mory01.mooo.com/api/files?key=images/c3efb8f8-f4a8-41a1-9d29-e93bc9d2dd83_trip_thumb_frame.png', 'https://me-mory01.mooo.com/api/files?key=images/eade6dc1-c412-4924-af7f-832292b258c0_trip_card_frame.png'),
('Beach', 'https://me-mory01.mooo.com/api/files?key=images/f692134b-0676-47e9-9782-778f7df9a23a_trip_thumb_beach.png', 'https://me-mory01.mooo.com/api/files?key=images/94abb8d5-5f56-4061-8e90-1df9c9107caf_trip_card_beach.png'),
('Forest', 'https://me-mory01.mooo.com/api/files?key=images/6f40c3c9-313f-44e5-9624-3264991b2f9b_trip_thumb_forest.png', 'https://me-mory01.mooo.com/api/files?key=images/99589e53-9539-46eb-b702-644fab7a87e3_trip_card_forest.png');

-- Emotion 데이터 삽입
INSERT INTO `emotion` (`name`, `colorCode`) VALUES
('기본', '#EEEEEE'),
('설렘', '#FDD7DE'),
('신기함', '#FFCB6B'),
('즐거움', '#FFE13E'),
('힐링', '#C1E8A0'),
('평온', '#D1E5D4'),
('뿌듯함', '#5ACFD5'),
('해방감', '#5EB6D9'),
('낯섦', '#634E72'),
('긴장됨', '#2C3E50'),
('외로움', '#A9A9B0'),
('아쉬움', '#866868'),
('벅참', '#800020');

-- Weather 데이터 삽입
INSERT INTO `weather` (`name`, `iconUrl`) VALUES
('맑음', 'https://me-mory01.mooo.com/api/files?key=images/7de47dfb-6f09-47d2-9268-2b8c49c2f9bd_weather_sunny.png'),
('구름', 'https://me-mory01.mooo.com/api/files?key=images/6a8c4a38-0ccd-479c-ba70-7202d7535bf7_weather_cloudy.png'),
('비', 'https://me-mory01.mooo.com/api/files?key=images/72a8e191-9b31-4646-ae48-cb31fb543de7_weather_rainy.png'),
('바람', 'https://me-mory01.mooo.com/api/files?key=images/dae2b4e2-7ac4-45fe-bccc-be835218d8d4_weather_windy.png'),
('눈', 'https://me-mory01.mooo.com/api/files?key=images/3b298957-360e-4ea4-86ea-bcd65fcd3f77_weather_snowy.png');

-- BoardTheme 데이터 삽입
INSERT INTO `board_theme` (`themeName`, `thumbnailUrl`, `cardUrl`) VALUES
('칠판', 'https://me-mory01.mooo.com/api/files?key=images/board_theme/chalkboard_thumb.png', 'https://me-mory01.mooo.com/api/files?key=images/73212582-a940-4929-9c9b-f99ce68f3238_board_card_chalkboard.png'),
('식탁보', 'https://me-mory01.mooo.com/api/files?key=images/board_theme/tablecloth_thumb.png', 'https://me-mory01.mooo.com/api/files?key=images/ea8230ad-a70a-4f87-acb4-648add56b1b8_board_card_tablecloth.png'),
('나무보드', 'https://me-mory01.mooo.com/api/files?key=images/board_theme/board_thumb.png', 'https://me-mory01.mooo.com/api/files?key=images/ecee4553-72cc-4097-8d20-10a7ee889269_board_card_woodboard.png'),
('체스판', 'https://me-mory01.mooo.com/api/files?key=images/board_theme/chess_thumb.png', 'https://me-mory01.mooo.com/api/files?key=images/e026afe7-1619-4ee5-b3e7-c2564d88447c_board_card_chess.png');

-- Sticker 데이터 삽입
INSERT INTO `sticker` (`name`, `imageUrl`) VALUES
('Star', 'https://me-mory01.mooo.com/api/files?key=images/6d4c8b2f-00ec-41ae-bd57-9ce914e1c78d_sticker_star.png');
