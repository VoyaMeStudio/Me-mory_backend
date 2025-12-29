package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zim.tave.memory.domain.Trip;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

	@Query("select t from Trip t where t.user.id = :userId and (t.isStored = false or t.isStored is null)")
	List<Trip> findByUserId(@Param("userId") Long userId);

	@Query("select distinct t from Trip t left join fetch t.diaries where t.user.id = :userId and (t.isStored = false or t.isStored is null)")
	List<Trip> findByUserIdWithDiaries(@Param("userId") Long userId);

	@Query("select t from Trip t where size(t.diaries) > 0")
	List<Trip> findAllWithDiaries();

	/**
	 * 타임라인 조회를 위한 활성 여행 목록 조회(N+1 문제 방지를 위해 모든 관련 엔티티를 한 번에 fetch)
	 * - 여행의 일기 목록, 일기의 국가 정보, 일기의 감정 정보
	 */
	@Query("""
			select distinct t from Trip t
			left join fetch t.diaries d
			left join fetch d.country c
			left join fetch d.emotion e
			where t.user.id = :userId and (t.isStored = false or t.isStored is null)
			""")
	List<Trip> findActiveTripsWithDetails(@Param("userId") Long userId);

	@Modifying
	@Query("update Trip t set t.endDate = coalesce((select cast(max(d.createdAt) as date) from Diary d where d.trip.id = :tripId), t.startDate) where t.id = :tripId")
	void updateTripEndDate(@Param("tripId") Long tripId);

	@Query("select cast(max(d.createdAt) as date) from Diary d where d.trip.id = :tripId")
	LocalDate findLastDiaryDateByTripId(@Param("tripId") Long tripId);

    @Query("SELECT t FROM Trip t " +
            "WHERE t.user.id = :userId " +
            "AND t.isStored = true ")
    List<Trip> findStoredTripsByUserId(@Param("userId") Long userId);

    // 현재 진행중인 여행 조회 (알림 전송 계산용)
    @Query("""
        SELECT t
        FROM Trip t
        WHERE t.user.id = :userId
          AND t.startDate <= :today
          AND t.endDate >= :today
          AND (t.isPast = false OR t.isPast IS NULL)
    """)
    Optional<Trip> findOngoingTripByUserId(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );
}
