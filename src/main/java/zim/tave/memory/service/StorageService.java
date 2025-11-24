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

import java.util.ArrayList;
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
    public void unstoreTrips(Long userId, List<Long> tripIds) {

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Trip> tripsToUnstore = new ArrayList<>();

        for (Long tripId : tripIds) {

            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

            if (!trip.getUser().getId().equals(userId)) {
                throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
            }

            if (!Boolean.TRUE.equals(trip.getIsStored())) {
                throw new CustomException(ErrorCode.TRIP_NOT_STORED);
            }

            tripsToUnstore.add(trip);
        }

        for (Trip trip : tripsToUnstore) {
            trip.setIsStored(false);
        }
    }

    @Transactional
    public void deleteStoredTrips(Long userId, List<Long> tripIds) {

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Trip> tripsToDelete = new ArrayList<>();

        for (Long tripId : tripIds) {

            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TRIP_NOT_FOUND));

            // 본인 여행인지 확인
            if (!trip.getUser().getId().equals(userId)) {
                throw new CustomException(ErrorCode.TRIP_UPDATE_FORBIDDEN);
            }

            // 보관 상태인지 확인
            if (!Boolean.TRUE.equals(trip.getIsStored())) {
                throw new CustomException(ErrorCode.TRIP_NOT_STORED);
            }

            tripsToDelete.add(trip);
        }

        for (Trip trip : tripsToDelete) {
            tripRepository.delete(trip);
        }
    }
}
