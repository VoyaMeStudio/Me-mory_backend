package zim.tave.memory.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import zim.tave.memory.domain.DiaryImage;
import zim.tave.memory.dto.request.CreateDiaryRequest;
import zim.tave.memory.dto.request.CreateTripRequest;

import java.time.LocalDate;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 기록하기 플로우 API 통합 테스트
 * - HTTP 엔드포인트 레벨에서 테스트
 * - JWT 인증 포함
 */
public class RecordingFlowApiTest extends ApiTestBase {

    @BeforeEach
    void setUp() {
        initializeTestData();
    }

    @Test
    void 새_여행_생성_후_일기_추가_API_테스트() throws Exception {
        // given - 새 여행 생성
        CreateTripRequest tripRequest = new CreateTripRequest();
        tripRequest.setTripName("제주도 여행");
        tripRequest.setDescription("제주도 3박 4일 여행");
        tripRequest.setThemeId(defaultTheme.getId());
        tripRequest.setStartDate(LocalDate.of(2024, 1, 1));
        tripRequest.setEndDate(LocalDate.of(2024, 1, 4));

        String token = getAuthorizationHeader(testUser.getId());

        ResultActions tripResult = mockMvc.perform(post("/api/users/me/trips")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripRequest)));

        tripResult.andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.tripName").value("제주도 여행"));

        String tripResponseJson = tripResult.andReturn().getResponse().getContentAsString();
        Long tripId = objectMapper.readTree(tripResponseJson)
                .get("data").get("id").asLong();

        // when - 일기 추가
        CreateDiaryRequest diaryRequest = new CreateDiaryRequest();
        diaryRequest.setUserId(testUser.getId());
        diaryRequest.setTripId(tripId);
        diaryRequest.setCountryCode("KR");
        diaryRequest.setCity("제주시");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        diaryRequest.setContent("제주도 첫째 날");
        diaryRequest.setEmotionId(defaultEmotion.getId());
        diaryRequest.setWeatherId(sunnyWeather.getId());

        CreateDiaryRequest.DiaryImageInfo frontImage = new CreateDiaryRequest.DiaryImageInfo();
        frontImage.setImageUrl("https://example.com/front1.jpg");
        frontImage.setCameraType(DiaryImage.CameraType.FRONT);
        frontImage.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo backImage = new CreateDiaryRequest.DiaryImageInfo();
        backImage.setImageUrl("https://example.com/back1.jpg");
        backImage.setCameraType(DiaryImage.CameraType.BACK);
        backImage.setRepresentative(false);

        diaryRequest.setImages(Arrays.asList(frontImage, backImage));

        // then
        mockMvc.perform(post("/api/users/me/diaries")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(diaryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.city").value("제주시"))
                .andExpect(jsonPath("$.data.content").value("제주도 첫째 날"));
    }

    @Test
    void 기존_여행에_일기_추가_API_테스트() throws Exception {
        // given - 기존 여행 생성
        CreateTripRequest tripRequest = new CreateTripRequest();
        tripRequest.setTripName("제주도 여행");
        tripRequest.setDescription("제주도 여행");
        tripRequest.setThemeId(defaultTheme.getId());
        tripRequest.setStartDate(LocalDate.of(2024, 1, 1));
        tripRequest.setEndDate(LocalDate.of(2024, 1, 4));

        String token = getAuthorizationHeader(testUser.getId());

        ResultActions tripResult = mockMvc.perform(post("/api/users/me/trips")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tripRequest)));

        String tripResponseJson = tripResult.andReturn().getResponse().getContentAsString();
        Long tripId = objectMapper.readTree(tripResponseJson)
                .get("data").get("id").asLong();

        // 첫 번째 일기 추가
        CreateDiaryRequest firstDiary = new CreateDiaryRequest();
        firstDiary.setUserId(testUser.getId());
        firstDiary.setTripId(tripId);
        firstDiary.setCountryCode("KR");
        firstDiary.setCity("제주시");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        firstDiary.setContent("첫째 날");

        CreateDiaryRequest.DiaryImageInfo front1 = new CreateDiaryRequest.DiaryImageInfo();
        front1.setImageUrl("https://example.com/front1.jpg");
        front1.setCameraType(DiaryImage.CameraType.FRONT);
        front1.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo back1 = new CreateDiaryRequest.DiaryImageInfo();
        back1.setImageUrl("https://example.com/back1.jpg");
        back1.setCameraType(DiaryImage.CameraType.BACK);
        back1.setRepresentative(false);

        firstDiary.setImages(Arrays.asList(front1, back1));
        mockMvc.perform(post("/api/users/me/diaries")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstDiary)))
                .andExpect(status().isCreated());

        // when - 두 번째 일기 추가
        CreateDiaryRequest secondDiary = new CreateDiaryRequest();
        secondDiary.setUserId(testUser.getId());
        secondDiary.setTripId(tripId);
        secondDiary.setCountryCode("KR");
        secondDiary.setCity("서귀포시");
        // dateTime 필드 제거 - 서버에서 UTC 기준으로 자동 생성됨
        secondDiary.setContent("둘째 날");

        CreateDiaryRequest.DiaryImageInfo front2 = new CreateDiaryRequest.DiaryImageInfo();
        front2.setImageUrl("https://example.com/front2.jpg");
        front2.setCameraType(DiaryImage.CameraType.FRONT);
        front2.setRepresentative(true);

        CreateDiaryRequest.DiaryImageInfo back2 = new CreateDiaryRequest.DiaryImageInfo();
        back2.setImageUrl("https://example.com/back2.jpg");
        back2.setCameraType(DiaryImage.CameraType.BACK);
        back2.setRepresentative(false);

        secondDiary.setImages(Arrays.asList(front2, back2));

        // then
        mockMvc.perform(post("/api/users/me/diaries")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondDiary)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.city").value("서귀포시"));
    }
}

