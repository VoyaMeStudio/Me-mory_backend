package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Country;
import zim.tave.memory.domain.Trip;
import zim.tave.memory.domain.TripCountry;
import zim.tave.memory.repository.TripCountryRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class TripCountryService {

    private final TripCountryRepository tripCountryRepository;
    private final CountryService countryService;

    public void registerTripCountries(Trip trip, List<String> countryCodes) {
        if (countryCodes == null || countryCodes.isEmpty()) {
            return;
        }

        Set<String> registeredCodes = new LinkedHashSet<>();
        for (String countryCode : countryCodes) {
            Country country = countryService.findByCode(countryCode);
            if (registeredCodes.add(country.getCountryCode())) {
                registerTripCountry(trip, country);
            }
        }
    }

    public void registerTripCountry(Trip trip, Country country) {
        if (trip == null || trip.getId() == null || country == null || country.getCountryCode() == null) {
            return;
        }

        boolean exists = tripCountryRepository.existsByTrip_IdAndCountry_CountryCode(
                trip.getId(),
                country.getCountryCode()
        );
        if (exists) {
            return;
        }

        tripCountryRepository.save(TripCountry.create(trip, country));
    }

    public void deleteTripCountry(Trip trip, Country country) {
        if (trip == null || trip.getId() == null || country == null || country.getCountryCode() == null) {
            return;
        }

        tripCountryRepository.findByTrip_IdAndCountry_CountryCode(trip.getId(), country.getCountryCode())
                .ifPresent(tripCountryRepository::delete);
    }
}
