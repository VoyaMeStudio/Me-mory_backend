package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.dto.response.StoredTripListResponseDto;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.TripRepository;
import zim.tave.memory.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public StoredTripListResponseDto getStoredTrips(Long userId) {

        // 1️⃣ 사용자 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        try {
            // 2️⃣ isStored = true 인 여행만 조회
            List<Trip> storedTrips = tripRepository.findStoredTripsByUserId(userId);

            // 3️⃣ DTO 변환 후 반환
            return StoredTripListResponseDto.from(storedTrips);

        } catch (CustomException e) {
            throw e; // 그대로 전달
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
