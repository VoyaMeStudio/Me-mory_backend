package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zim.tave.memory.domain.Diary;

import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

	// 파생 쿼리 메소드
	List<Diary> findByTrip_Id(Long tripId);
	List<Diary> findByUser_Id(Long userId);
	long countByUser_Id(Long userId);

	// 회원 탈퇴 시 DiaryImage -> Diary 순서로 일괄 삭제 (FK 제약 회피)
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM DiaryImage di WHERE di.diary.id IN (SELECT d.id FROM Diary d WHERE d.user.id = :userId)")
	void deleteAllImagesByUserId(@Param("userId") Long userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM Diary d WHERE d.user.id = :userId")
	void deleteAllByUserId(@Param("userId") Long userId);
}
