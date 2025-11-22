package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.UpdateUserRequestDto;
import zim.tave.memory.dto.response.UserResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.DiaryRepository;
import zim.tave.memory.repository.UserRepository;
import zim.tave.memory.repository.VisitedCountryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final UserRepository userRepository;
    private final VisitedCountryRepository visitedCountryRepository;
    private final DiaryRepository diaryRepository;

    @Transactional
    public void deleteAccount(Long userId) {
        //사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        try {

            // 연관 데이터 삭제
            visitedCountryRepository.deleteAllByUserId(userId);
            diaryRepository.deleteAllImagesByUserId(userId);
            diaryRepository.deleteAllByUserId(userId);

            userRepository.delete(user);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public UserResponseDto updateUserInfo(Long userId, UpdateUserRequestDto requestDto) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 필수 필드 검증 (하나라도 비어 있으면 예외)
        validateFields(requestDto);

        // null이 아닌 필드만 업데이트
        if (requestDto.getSurName() != null)
            user.setSurName(requestDto.getSurName());
        if (requestDto.getFirstName() != null)
            user.setFirstName(requestDto.getFirstName());
        if (requestDto.getKoreanName() != null)
            user.setKoreanName(requestDto.getKoreanName());
        if (requestDto.getBirth() != null)
            user.setBirth(requestDto.getBirth());
        if (requestDto.getNationality() != null)
            user.setNationality(requestDto.getNationality());

        userRepository.save(user);

        return UserResponseDto.from(user);
    }

    //필수 필드 null 여부 검사
    private void validateFields(UpdateUserRequestDto dto) {
        if (isNullOrEmpty(dto.getSurName()) ||
                isNullOrEmpty(dto.getFirstName()) ||
                isNullOrEmpty(dto.getKoreanName()) ||
                dto.getBirth() == null ||
                isNullOrEmpty(dto.getNationality())) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELDS);
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public UserResponseDto updateUserInfo(Long userId, UpdateUserRequestDto requestDto) {

        // 1️⃣ 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2️⃣ 필수 필드 검증 (하나라도 비어 있으면 예외)
        validateFields(requestDto);

        // 3️⃣ null이 아닌 필드만 업데이트
        if (requestDto.getSurName() != null)
            user.setSurName(requestDto.getSurName());
        if (requestDto.getFirstName() != null)
            user.setFirstName(requestDto.getFirstName());
        if (requestDto.getKoreanName() != null)
            user.setKoreanName(requestDto.getKoreanName());
        if (requestDto.getBirth() != null)
            user.setBirth(requestDto.getBirth());
        if (requestDto.getNationality() != null)
            user.setNationality(requestDto.getNationality());

        // 4️⃣ 저장 (변경 없더라도 안전)
        userRepository.save(user);

        // 5️⃣ 응답 DTO 생성
        return new UserResponseDto(
                user.getId(),
                user.getKakaoId(),
                user.getProfileImageUrl(),
                user.getSurName(),
                user.getFirstName(),
                user.getKoreanName(),
                user.getBirth(),
                user.getNationality(),
                user.getDiaryCount(),
                user.getVisitedCountryCount(),
                user.getFlags()
        );
    }

    // ✅ 필수 필드 유효성 검사
    private void validateFields(UpdateUserRequestDto dto) {
        if (isNullOrEmpty(dto.getSurName()) ||
                isNullOrEmpty(dto.getFirstName()) ||
                isNullOrEmpty(dto.getKoreanName()) ||
                dto.getBirth() == null ||
                isNullOrEmpty(dto.getNationality())) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELDS);
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
