package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import zim.tave.memory.domain.Country;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, String> {
    
    //나라 검색용
    List<Country> findByCountryNameContainingIgnoreCase(String keyword);
    
    @Query("SELECT c FROM Country c ORDER BY c.countryName")
    List<Country> findAll();
}
