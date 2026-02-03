package zim.tave.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Country;
import zim.tave.memory.repository.CountryRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class CountryServiceTest {

    @Autowired
    private CountryService countryService;

    @Autowired
    private CountryRepository countryRepository;

    @BeforeEach
    void clearDB() {
        countryRepository.findAll().forEach(c -> countryRepository.delete(c));
    }

    // init() 메서드는 Flyway 마이그레이션으로 대체되었으므로 테스트 제거
    // Flyway가 V3__insert_countries.sql을 통해 국가 데이터를 관리합니다.

    @Test
    void searchCountryByKeyword() {
        // given
        countryRepository.save(new Country("KR", "대한민국", "🇰🇷"));
        countryRepository.save(new Country("TW", "대만", "🇹🇼"));
        countryRepository.save(new Country("US", "미국", "🇺🇸"));

        // when
        List<Country> result = countryService.searchCountriesByName("대");

        // then
        assertThat(result).extracting("countryName")
                .contains("대한민국", "대만")
                .doesNotContain("미국");
    }
}
