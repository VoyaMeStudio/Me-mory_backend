package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zim.tave.memory.domain.Trip;

import java.time.LocalDate;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

	@Query("select t from Trip t where t.user.id = :userId and (t.isStored = false or t.isStored is null)")
	List<Trip> findByUserId(@Param("userId") Long userId);

	@Query("select distinct t from Trip t left join fetch t.diaries where t.user.id = :userId and (t.isStored = false or t.isStored is null)")
	List<Trip> findByUserIdWithDiaries(@Param("userId") Long userId);

	@Query("select t from Trip t where size(t.diaries) > 0")
	List<Trip> findAllWithDiaries();

	@Modifying
	@Query("update Trip t set t.endDate = coalesce((select cast(max(d.createdAt) as date) from Diary d where d.trip.id = :tripId), t.startDate) where t.id = :tripId")
	void updateTripEndDate(@Param("tripId") Long tripId);

	@Query("select cast(max(d.createdAt) as date) from Diary d where d.trip.id = :tripId")
	LocalDate findLastDiaryDateByTripId(@Param("tripId") Long tripId);

    @Query("SELECT t FROM Trip t " +
            "WHERE t.user.id = :userId " +
            "AND t.isStored = true ")
    List<Trip> findStoredTripsByUserId(@Param("userId") Long userId);
}
