package zim.tave.memory.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.*;
import zim.tave.memory.repository.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TripThemeRepository tripThemeRepository;
    private final EmotionRepository emotionRepository;
    private final WeatherRepository weatherRepository;
    private final UserRepository userRepository;
    private final BoardThemeRepository boardThemeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("기본 데이터 초기화 시작...");

        // 테마 데이터 초기화
        initTripThemes();

        // 감정 데이터 초기화
        initEmotions();

        // 날씨 데이터 초기화
        initWeathers();

        // 테스트 사용자 데이터 초기화
        initTestUser();

        log.info("기본 데이터 초기화 완료!");
    }

    private void initTripThemes() {
        if (tripThemeRepository.count() == 0) {
            log.info("여행 테마 데이터 생성 중...");

            // 기본 테마 (ID 1)
            TripTheme basicTheme = new TripTheme("기본",
                    "https://me-mory01.mooo.com/api/files?key=images/d69be93f-fef6-4a73-815f-ea6b93fd2d59_trip_thumb_default.png",
                    "https://me-mory01.mooo.com/api/files?key=images/6e56681d-9fc3-43d2-a803-0ef165c63c3c_trip_card_default.png");
            tripThemeRepository.save(basicTheme);

            // Grey 테마 (ID 2)
            TripTheme greyTheme = new TripTheme("Grey",
                    "https://me-mory01.mooo.com/api/files?key=images/f0d5e1f7-2718-4dac-a780-d6f6e5f2d42a_trip_thumb_grey.png",
                    "https://me-mory01.mooo.com/api/files?key=images/462d91ad-b618-4449-805f-35f07187247b_trip_card_grey.png");
            tripThemeRepository.save(greyTheme);

            // 탑승권 테마 (ID 3)
            TripTheme ticketTheme = new TripTheme("탑승권",
                    "https://me-mory01.mooo.com/api/files?key=images/cbcf48b8-8469-4e0f-b597-7ef27cc6190e_trip_thumb_boardingpass.png",
                    "https://me-mory01.mooo.com/api/files?key=images/3e637461-f9a2-477d-ae06-4245033174a9_trip_card_boardingpass.png");
            tripThemeRepository.save(ticketTheme);

            // 액자 테마 (ID 4)
            TripTheme frameTheme = new TripTheme("액자",
                    "https://me-mory01.mooo.com/api/files?key=images/c3efb8f8-f4a8-41a1-9d29-e93bc9d2dd83_trip_thumb_frame.png",
                    "https://me-mory01.mooo.com/api/files?key=images/eade6dc1-c412-4924-af7f-832292b258c0_trip_card_frame.png");
            tripThemeRepository.save(frameTheme);

            // Beach 테마 (ID 5)
            TripTheme beachTheme = new TripTheme("Beach",
                    "https://me-mory01.mooo.com/api/files?key=images/f692134b-0676-47e9-9782-778f7df9a23a_trip_thumb_beach.png",
                    "https://me-mory01.mooo.com/api/files?key=images/94abb8d5-5f56-4061-8e90-1df9c9107caf_trip_card_beach.png");
            tripThemeRepository.save(beachTheme);

            // Forest 테마 (ID 6)
            TripTheme forestTheme = new TripTheme("Forest",
                    "https://me-mory01.mooo.com/api/files?key=images/6f40c3c9-313f-44e5-9624-3264991b2f9b_trip_thumb_forest.png",
                    "https://me-mory01.mooo.com/api/files?key=images/99589e53-9539-46eb-b702-644fab7a87e3_trip_card_forest.png");
            tripThemeRepository.save(forestTheme);

            log.info("여행 테마 데이터 생성 완료: {}개", tripThemeRepository.count());
        }
    }

    private void initEmotions() {
        if (emotionRepository.count() == 0) {
            log.info("감정 데이터 생성 중...");

            emotionRepository.save(new Emotion("기본", "#EEEEEE"));
            emotionRepository.save(new Emotion("설렘", "#FDD7DE"));
            emotionRepository.save(new Emotion("신기함", "#FFCB6B"));
            emotionRepository.save(new Emotion("즐거움", "#FFE13E"));
            emotionRepository.save(new Emotion("힐링", "#C1E8A0"));
            emotionRepository.save(new Emotion("평온", "#D1E5D4"));
            emotionRepository.save(new Emotion("뿌듯함", "#5ACFD5"));
            emotionRepository.save(new Emotion("해방감", "#5EB6D9"));
            emotionRepository.save(new Emotion("낯섦", "#634E72"));
            emotionRepository.save(new Emotion("긴장됨", "#2C3E50"));
            emotionRepository.save(new Emotion("외로움", "#A9A9B0"));
            emotionRepository.save(new Emotion("아쉬움", "#866868"));
            emotionRepository.save(new Emotion("벅참", "#800020"));


            log.info("감정 데이터 생성 완료: {}개", emotionRepository.count());
        }
    }

    private void initWeathers() {
        if (weatherRepository.count() == 0) {
            log.info("날씨 데이터 생성 중...");

            Weather sunny = new Weather();
            sunny.setName("맑음");
            sunny.setIconUrl("https://me-mory01.mooo.com/api/files?key=images/7de47dfb-6f09-47d2-9268-2b8c49c2f9bd_weather_sunny.png");
            weatherRepository.save(sunny);

            Weather cloudy = new Weather();
            cloudy.setName("구름");
            cloudy.setIconUrl("https://me-mory01.mooo.com/api/files?key=images/6a8c4a38-0ccd-479c-ba70-7202d7535bf7_weather_cloudy.png");
            weatherRepository.save(cloudy);

            Weather rainy = new Weather();
            rainy.setName("비");
            rainy.setIconUrl("https://me-mory01.mooo.com/api/files?key=images/72a8e191-9b31-4646-ae48-cb31fb543de7_weather_rainy.png");
            weatherRepository.save(rainy);

            Weather windy = new Weather();
            windy.setName("바람");
            windy.setIconUrl("https://me-mory01.mooo.com/api/files?key=images/dae2b4e2-7ac4-45fe-bccc-be835218d8d4_weather_windy.png");
            weatherRepository.save(windy);

            Weather snowy = new Weather();
            snowy.setName("눈");
            snowy.setIconUrl("https://me-mory01.mooo.com/api/files?key=images/3b298957-360e-4ea4-86ea-bcd65fcd3e77_weather_snowy.png");
            weatherRepository.save(snowy);

            log.info("날씨 데이터 생성 완료: {}개", weatherRepository.count());
        }
    }

    private void initBoardThemes() {
        if (boardThemeRepository.count() == 0) {
            log.info("보드 테마 데이터 생성 중...");

            // 1. 칠판 테마 (chalkboard)
            BoardTheme chalkboard = new BoardTheme();
            chalkboard.setThemeName("칠판");
            chalkboard.setDescription("칠판 느낌의 따뜻한 보드 테마");
            chalkboard.setThumbnailUrl("https://me-mory01.mooo.com/api/files?key=images/board_theme/chalkboard_thumb.png");
            chalkboard.setCardUrl("https://me-mory01.mooo.com/api/files?key=images/73212582-a940-4929-9c9b-f99ce68f3238_board_card_chalkboard.png");
            boardThemeRepository.save(chalkboard);

            // 2. 식탁보 테마 (tablecloth)
            BoardTheme tablecloth = new BoardTheme();
            tablecloth.setThemeName("식탁보");
            tablecloth.setDescription("식탁보 패턴을 사용한 홈 스타일 보드 테마");
            tablecloth.setThumbnailUrl("https://me-mory01.mooo.com/api/files?key=images/board_theme/tablecloth_thumb.png");
            tablecloth.setCardUrl("https://me-mory01.mooo.com/api/files?key=images/ea8230ad-a70a-4f87-acb4-648add56b1b8_board_card_tablecloth.png");
            boardThemeRepository.save(tablecloth);

            // 3. 보드판 테마 (board)
            BoardTheme board = new BoardTheme();
            board.setThemeName("보드판");
            board.setDescription("깔끔한 화이트보드 스타일의 테마");
            board.setThumbnailUrl("https://me-mory01.mooo.com/api/files?key=images/board_theme/board_thumb.png");
            board.setCardUrl("https://me-mory01.mooo.com/api/files?key=images/ecee4553-72cc-4097-8d20-10a7ee889269_board_card_woodboard.png");
            boardThemeRepository.save(board);

            // 4. 체스판 테마 (chess)
            BoardTheme chess = new BoardTheme();
            chess.setThemeName("체스판");
            chess.setDescription("체스판 패턴의 독특한 보드 테마");
            chess.setThumbnailUrl("https://me-mory01.mooo.com/api/files?key=images/board_theme/chess_thumb.png");
            chess.setCardUrl("https://me-mory01.mooo.com/api/files?key=images/e026afe7-1619-4ee5-b3e7-c2564d88447c_board_card_chess.png");
            boardThemeRepository.save(chess);

            log.info("보드 테마 데이터 생성 완료: {}개", boardThemeRepository.count());
        }
    }

    private void initTestUser() {
        // 테스트용 사용자가 없으면 생성
        if (!userRepository.findByKakaoId("test_강지혜").isPresent()) {
            log.info("테스트 사용자 데이터 생성 중...");

            User testUser = new User();
            testUser.setKakaoId("test_강지혜");
            testUser.setSurName("KANG");
            testUser.setFirstName("JIHYE");
            testUser.setKoreanName("강지혜");
            testUser.setBirth(java.time.LocalDate.of(1995, 3, 15));
            testUser.setNationality("REPUBLIC OF KOREA");
            testUser.setCreatedAt(java.time.LocalDate.now());
            testUser.setStatus(true);
            testUser.setProfileImageUrl("https://me-mory.mooo.com/api/files?key=images/7c24aa61-2d36-48fd-80a5-646ac518256d_IMG_3831.jpg");
            testUser.setDiaryCount(0L);
            testUser.setVisitedCountryCount(0L);
            testUser.setFlags("🇰🇷");
            userRepository.save(testUser);

            log.info("테스트 사용자 데이터 생성 완료");
        } else {
            log.info("테스트 사용자가 이미 존재합니다.");
        }
    }
}
