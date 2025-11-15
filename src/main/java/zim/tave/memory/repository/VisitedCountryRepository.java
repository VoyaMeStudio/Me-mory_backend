package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zim.tave.memory.domain.VisitedCountry;

import java.util.List;
import java.util.Optional;

public interface VisitedCountryRepository extends JpaRepository<VisitedCountry, Long> {
    
    //사용자의 모든 방문 국가 목록 조회 (감정 정보 포함)
    @Query("SELECT v FROM VisitedCountry v JOIN FETCH v.country JOIN FETCH v.emotion WHERE v.user.id = :userId")
    List<VisitedCountry> findByUserIdWithDetails(@Param("userId") Long userId);
    
    //사용자가 방문한 나라 국가코드와 userId로 찾기
    @Query("SELECT v FROM VisitedCountry v JOIN FETCH v.country JOIN FETCH v.emotion WHERE v.user.id = :userId AND v.country.countryCode = :countryCode")
    Optional<VisitedCountry> findByUserIdAndCountryCodeWithDetails(@Param("userId") Long userId, @Param("countryCode") String countryCode);
    
    //지도에 색을 칠했는지 확인
    @Query("SELECT COUNT(v) > 0 FROM VisitedCountry v WHERE v.user.id = :userId AND v.country.countryCode = :countryCode")
    boolean existsByUserIdAndCountryCode(@Param("userId") Long userId, @Param("countryCode") String countryCode);
    
    //사용자의 방문 국가 수 조회
    Long countByUserId(Long userId);
    
    // 회원 탈퇴용 정보 삭제
    @Modifying
    @Query("DELETE FROM VisitedCountry v WHERE v.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
