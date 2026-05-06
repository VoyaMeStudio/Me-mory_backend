package zim.tave.memory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zim.tave.memory.domain.TripCountry;

import java.util.List;
import java.util.Optional;

public interface TripCountryRepository extends JpaRepository<TripCountry, Long> {

    boolean existsByTrip_IdAndCountry_CountryCode(Long tripId, String countryCode);

    Optional<TripCountry> findByTrip_IdAndCountry_CountryCode(Long tripId, String countryCode);

    @Query("""
            select tc from TripCountry tc
            join fetch tc.trip t
            join fetch tc.country
            where t.id in :tripIds
            """)
    List<TripCountry> findByTripIdsWithCountry(@Param("tripIds") List<Long> tripIds);
}
