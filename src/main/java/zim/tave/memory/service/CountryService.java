package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Country;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.CountryRepository;
import zim.tave.memory.util.CountryValidator;
import zim.tave.memory.util.EmojiValidator;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;


    public List<Country> searchCountriesByName(String keyword) {
        // 검색 키워드가 null이거나 빈 문자열이면 전체 국가 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return countryRepository.findAll();
        }
        return countryRepository.findByCountryNameContainingIgnoreCase(keyword.trim());
    }

    public Country findByCode(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_COUNTRY_CODE);
        }
        String normalizedCode = CountryValidator.normalizeCountryCode(countryCode);
        return countryRepository.findById(normalizedCode)
                .orElseThrow(() -> new CustomException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    public void saveCountry(Country country) {
        if (country == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        if (country.getCountryCode() == null || country.getCountryCode().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_COUNTRY_CODE);
        }

        if (country.getEmoji() != null && !EmojiValidator.isUtf8mb4Compatible(country.getEmoji())) {
            throw new CustomException(ErrorCode.INVALID_COUNTRY_EMOJI);
        }
        countryRepository.save(country);
    }
}