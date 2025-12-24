package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Setting;
import zim.tave.memory.domain.User;
import zim.tave.memory.dto.request.JoinRequestDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.SettingRepository;
import zim.tave.memory.repository.UserRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final UserRepository userRepository;
    private final SettingRepository settingRepository;

    @Transactional
    public User join(Long userId, JoinRequestDto requestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // DB에서 카톡 아이디 조회하여 로그인 여부 검증
        if (user.getKakaoId() == null) {
            throw new CustomException(ErrorCode.KAKAO_LOGIN_REQUIRED);
        }

        if (user.isRegistered()) {
            throw new CustomException(ErrorCode.ALREADY_JOINED);
        }

        // 초기 정보 입력
        user.setSurName(requestDto.getSurName());
        user.setFirstName(requestDto.getFirstName());
        user.setKoreanName(requestDto.getKoreanName());
        user.setBirth(requestDto.getBirth());
        user.setNationality(requestDto.getNationality());
        user.setCreatedAt(LocalDate.now());
        user.setStatus(true);
        user.setRegistered(true);

        //마이페이지 Statistics 정보
        user.setDiaryCount(0L);
        user.setVisitedCountryCount(0L);
        user.setFlags("");

        userRepository.save(user);

        Setting setting = new Setting();
        setting.setId(user.getId());

        if (requestDto.getAlarm() != null) {
            setting.setAlarm(requestDto.getAlarm());
        } else {
            setting.setAlarm(true); // 안전하게 명시
        }

        settingRepository.save(setting);

        return user;
    }
}
