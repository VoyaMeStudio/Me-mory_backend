package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zim.tave.memory.domain.User;
import zim.tave.memory.domain.VisitedCountry;
import zim.tave.memory.dto.MyPageResponseDto;
import zim.tave.memory.repository.DiaryRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.VisitedCountryRepository;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MyPageService {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final VisitedCountryRepository visitedCountryRepository;

    public MyPageResponseDto getMyPage(Long userId) {

        // Get user by userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 필수 필드 입력여부 체크
        if (isIncompleteUserInfo(user)) {
            throw new CustomException(ErrorCode.INCOMPLETE_USER_INFO);
        }

        String formattedBirth = birthFormatter(user.getBirth());

        // UserInfo
        MyPageResponseDto.UserInfo userInfo = new MyPageResponseDto.UserInfo(
                user.getId(),
                user.getProfileImageUrl(),
                user.getSurName().toUpperCase(),
                user.getFirstName().toUpperCase(),
                user.getKoreanName(),
                formattedBirth,
                user.getNationality()
        );

        // Statistics
        Long diaryCount = diaryRepository.countByUserId(userId);
        Long countryCount = visitedCountryRepository.countByUserId(userId);
        MyPageResponseDto.Statistics statistics = new MyPageResponseDto.Statistics(countryCount, diaryCount);

        // Flags
        List<VisitedCountry> visitedCountries = visitedCountryRepository.findByUserIdWithDetails(userId);
        String flags = visitedCountries.stream()
                .map(vc -> vc.getCountry().getEmoji())
                .collect(Collectors.joining());

        return new MyPageResponseDto(userInfo, statistics, flags);
    }

    private String birthFormatter(LocalDate birth) {

        if (birth == null) {
            return "생일 정보를 입력해주세요.";
        }

        int day = birth.getDayOfMonth();
        int year = birth.getYear();

        String[] englishMonthForm = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] koreanMonthForm = {"1월", "2월", "3월", "4월", "5월", "6월",
                "7월", "8월", "9월", "10월", "11월", "12월"};

        int monthIndex = birth.getMonthValue() - 1;
        String koreanMonth = koreanMonthForm[monthIndex];
        String englishMonth = englishMonthForm[monthIndex];

        return String.format("%d %s/%s %d", day, koreanMonth, englishMonth, year);
    }

    // 정보누락여부 체크용 메서드
    private boolean isIncompleteUserInfo(User user) {
        return isNullOrEmpty(user.getSurName()) ||
                isNullOrEmpty(user.getFirstName()) ||
                isNullOrEmpty(user.getKoreanName()) ||
                user.getBirth() == null ||
                isNullOrEmpty(user.getNationality());
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /*
    // 에러 테스트 코드
    public MyPageResponseDto getErrorExample(Long userId) {
        // 일부러 예외 발생
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
     */
}
