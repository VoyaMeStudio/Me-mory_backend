package zim.tave.memory.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Country;
import zim.tave.memory.repository.CountryRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Tag("integration")
@ExtendWith(MockitoExtension.class)
public class CountryServiceTest {

    @InjectMocks
    private CountryService countryService;

    @Mock
    private CountryRepository countryRepository;

    // init() 메서드는 Flyway 마이그레이션으로 대체되었으므로 테스트 제거
    // Flyway가 V3__insert_countries.sql을 통해 국가 데이터를 관리합니다.

    @Test
    void searchCountryByKeyword() {
        // given
        when(countryRepository.findByCountryNameContainingIgnoreCase("대"))
                .thenReturn(List.of(
                        new Country("KR", "대한민국", "🇰🇷"),
                        new Country("TW", "대만", "🇹🇼")
                ));

        // when
        List<Country> result = countryService.searchCountriesByName("대");

        // then
        assertThat(result).extracting("countryName")
                .contains("대한민국", "대만")
                .doesNotContain("미국");
    }
}
