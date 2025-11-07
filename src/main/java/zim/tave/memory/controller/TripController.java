package zim.tave.memory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.dto.CreateTripRequest;
import zim.tave.memory.dto.ListResponse;
import zim.tave.memory.dto.TripRepresentativeImageDto;
import zim.tave.memory.dto.TripResponseDto;
import zim.tave.memory.dto.UpdateRepresentativeImageRequest;
import zim.tave.memory.dto.UpdateTripRequest;
import zim.tave.memory.security.CustomUserDetails;
import zim.tave.memory.service.DiaryService;
import zim.tave.memory.service.TripService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Tag(name = "Trip", description = "JWT 인증이 필요한 여행 관리 API")
public class TripController {

    private final TripService tripService;
    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "여행 생성", description = "JWT 토큰으로 인증된 사용자의 새로운 여행을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "여행 생성 성공",
                    content = @Content(schema = @Schema(implementation = TripResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    })
    public ResponseEntity<TripResponseDto> createTrip(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateTripRequest request) {
        Long userId = userDetails.getUserId();
        // DTO 검증
        if (request.getTripName() == null || request.getTripName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        if (request.getTripName().trim().length() > 14) {
            return ResponseEntity.badRequest().build();
        }
        
        if (request.getDescription() != null && request.getDescription().trim().length() > 56) {
            return ResponseEntity.badRequest().build();
        }
        
        Trip trip = tripService.createTrip(userId, request);
        return ResponseEntity.ok(TripResponseDto.from(trip));
    }

    @PutMapping("/{tripId}")
    @Operation(summary = "여행 수정", description = "JWT 토큰으로 인증된 사용자의 여행 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "여행 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Void> updateTrip(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId,
            @RequestBody UpdateTripRequest request) {
        Long userId = userDetails.getUserId();
        // DTO 검증
        if (request.getTripName() != null && request.getTripName().trim().length() > 14) {
            return ResponseEntity.badRequest().build();
        }
        
        if (request.getDescription() != null && request.getDescription().trim().length() > 56) {
            return ResponseEntity.badRequest().build();
        }
        
        // 날짜 순서 검증: startDate가 endDate보다 늦으면 안됨
        if (request.getStartDate() != null && request.getEndDate() != null && 
            request.getStartDate().isAfter(request.getEndDate())) {
            return ResponseEntity.badRequest().build();
        }
        
        tripService.updateTrip(userId, tripId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "내 여행 목록 조회", description = "JWT 토큰으로 인증된 사용자의 모든 여행을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "여행 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = TripResponseDto.class)))
    public ResponseEntity<ListResponse<TripResponseDto>> getAllTrips(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        List<Trip> trips = tripService.findByUserId(userId);
        List<TripResponseDto> tripDtos = trips.stream()
                .map(TripResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ListResponse<>(tripDtos));
    }

    @GetMapping("/{tripId}")
    @Operation(summary = "여행 상세 조회", description = "JWT 토큰으로 인증된 사용자의 특정 여행 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "여행 조회 성공",
                    content = @Content(schema = @Schema(implementation = TripResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<TripResponseDto> getTrip(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId) {
        Long userId = userDetails.getUserId();
        Trip trip = tripService.findOne(userId, tripId);
        return ResponseEntity.ok(TripResponseDto.from(trip));
    }

    @GetMapping("/{tripId}/representative-images")
    @Operation(summary = "여행에 속한 다이어리들의 대표사진 목록 조회", description = "JWT 토큰으로 인증된 사용자의 특정 여행에 속한 다이어리 대표사진을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대표사진 조회 성공",
                    content = @Content(schema = @Schema(implementation = TripRepresentativeImageDto.class))),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<ListResponse<TripRepresentativeImageDto>> getRepresentativeImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId) {
        Long userId = userDetails.getUserId();
        tripService.findOne(userId, tripId);
        List<TripRepresentativeImageDto> representativeImages = diaryService.getRepresentativeImagesByTripId(tripId);
        return ResponseEntity.ok(new ListResponse<>(representativeImages));
    }

    @PutMapping("/{tripId}/representative-image")
    @Operation(summary = "여행 대표사진 설정", description = "JWT 토큰으로 인증된 사용자의 여행 대표사진을 설정합니다. 해당 여행에 속한 다이어리의 대표사진 중에서 선택할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "여행 대표사진 설정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행 또는 이미지를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Void> updateTripRepresentativeImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId,
            @RequestBody UpdateRepresentativeImageRequest request) {
        Long userId = userDetails.getUserId();
        
        if (request.getImageId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            tripService.updateTripRepresentativeImage(userId, tripId, request.getImageId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 