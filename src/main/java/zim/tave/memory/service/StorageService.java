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

        // 사용자 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        try {
            // isStored = true 인 여행만 조회
            List<Trip> storedTrips = tripRepository.findStoredTripsByUserId(userId);

            return StoredTripListResponseDto.from(storedTrips);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void unstoreTrip(Long userId, Long tripId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
        }

        trip.setIsStored(false);
    }
}
