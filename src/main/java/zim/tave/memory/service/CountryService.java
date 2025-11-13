package zim.tave.memory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zim.tave.memory.domain.Country;
import zim.tave.memory.global.common.exception.CustomException;
import zim.tave.memory.global.common.exception.ErrorCode;
import zim.tave.memory.repository.CountryRepository;
import zim.tave.memory.util.EmojiValidator;


import java.util.List;

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
        return countryRepository.findByNameContaining(keyword.trim());
    }

    public Country findByCode(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_COUNTRY_CODE);
        }
        String normalizedCode = countryCode.trim().toUpperCase();
        Country country = countryRepository.findByCode(normalizedCode);
        if (country == null) {
            throw new CustomException(ErrorCode.COUNTRY_NOT_FOUND);
        }
        return country;
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

    @Transactional
    public void init() {
        //System.out.println(">> CountryService.init() 실행됨");
        if (!countryRepository.findAll().isEmpty()) {
            //System.out.println(">> 이미 데이터 있음, return");
            return;
        }


        List<Country> countries = List.of(
            new Country("GH", "가나", "🇬🇭"),
            new Country("GA", "가봉", "🇬🇦"),
            new Country("GY", "가이아나", "🇬🇾"),
            new Country("GM", "감비아", "🇬🇲"),
            new Country("GG", "건지", "🇬🇬"),
            new Country("GP", "과들루프", "🇬🇵"),
            new Country("GT", "과테말라", "🇬🇹"),
            new Country("GU", "괌", "🇬🇺"),
            new Country("GD", "그레나다", "🇬🇩"),
            new Country("GR", "그리스", "🇬🇷"),
            new Country("GL", "그린란드", "🇬🇱"),
            new Country("GN", "기니", "🇬🇳"),
            new Country("GW", "기니비사우", "🇬🇼"),
            new Country("NA", "나미비아", "🇳🇦"),
            new Country("NR", "나우루", "🇳🇷"),
            new Country("NG", "나이지리아", "🇳🇬"),
            new Country("AQ", "남극", "🇦🇶"),
            new Country("SS", "남수단", "🇸🇸"),
            new Country("ZA", "남아프리카공화국", "🇿🇦"),
            new Country("NL", "네덜란드", "🇳🇱"),
            new Country("NP", "네팔", "🇳🇵"),
            new Country("NO", "노르웨이", "🇳🇴"),
            new Country("NF", "노퍽섬", "🇳🇫"),
            new Country("NZ", "뉴질랜드", "🇳🇿"),
            new Country("NC", "뉴칼레도니아", "🇳🇨"),
            new Country("NU", "니우에", "🇳🇺"),
            new Country("NE", "니제르", "🇳🇪"),
            new Country("NI", "니카라과", "🇳🇮"),
            new Country("TW", "대만", "🇹🇼"),
            new Country("KR", "대한민국", "🇰🇷"),
            new Country("DK", "덴마크", "🇩🇰"),
            new Country("DM", "도미니카", "🇩🇲"),
            new Country("DO", "도미니카 공화국", "🇩🇴"),
            new Country("DE", "독일", "🇩🇪"),
            new Country("TL", "동티모르", "🇹🇱"),
            new Country("LA", "라오스", "🇱🇦"),
            new Country("LR", "라이베리아", "🇱🇷"),
            new Country("LV", "라트비아", "🇱🇻"),
            new Country("RU", "러시아", "🇷🇺"),
            new Country("LB", "레바논", "🇱🇧"),
            new Country("LS", "레소토", "🇱🇸"),
            new Country("RE", "레위니옹", "🇷🇪"),
            new Country("RO", "루마니아", "🇷🇴"),
            new Country("LU", "룩셈부르크", "🇱🇺"),
            new Country("RW", "르완다", "🇷🇼"),
            new Country("LY", "리비아", "🇱🇾"),
            new Country("LT", "리투아니아", "🇱🇹"),
            new Country("LI", "리히텐슈타인", "🇱🇮"),
            new Country("MG", "마다가스카르", "🇲🇬"),
            new Country("MQ", "마르티니크", "🇲🇶"),
            new Country("MH", "마셜제도", "🇲🇭"),
            new Country("YT", "마요트", "🇾🇹"),
            new Country("MO", "마카오", "🇲🇴"),
            new Country("MW", "말라위", "🇲🇼"),
            new Country("MY", "말레이시아", "🇲🇾"),
            new Country("ML", "말리", "🇲🇱"),
            new Country("IM", "맨섬", "🇮🇲"),
            new Country("MX", "멕시코", "🇲🇽"),
            new Country("MC", "모나코", "🇲🇨"),
            new Country("MA", "모로코", "🇲🇦"),
            new Country("MR", "모리타니", "🇲🇷"),
            new Country("MZ", "모잠비크", "🇲🇿"),
            new Country("ME", "몬테네그로", "🇲🇪"),
            new Country("MS", "몬트세라트", "🇲🇸"),
            new Country("MD", "몰도바", "🇲🇩"),
            new Country("MV", "몰디브", "🇲🇻"),
            new Country("MT", "몰타", "🇲🇹"),
            new Country("MN", "몽골", "🇲🇳"),
            new Country("US", "미국", "🇺🇸"),
            new Country("UM", "미국령 군소 제도", "🇺🇲"),
            new Country("VI", "미국령 버진아일랜드", "🇻🇮"),
            new Country("MM", "미얀마", "🇲🇲"),
            new Country("FM", "미크로네시아", "🇫🇲"),
            new Country("VU", "바누아투", "🇻🇺"),
            new Country("BH", "바레인", "🇧🇭"),
            new Country("BB", "바베이도스", "🇧🇧"),
            new Country("VA", "바티칸", "🇻🇦"),
            new Country("BS", "바하마", "🇧🇸"),
            new Country("BD", "방글라데시", "🇧🇩"),
            new Country("BM", "버뮤다", "🇧🇲"),
            new Country("BJ", "베냉", "🇧🇯"),
            new Country("VE", "베네수엘라", "🇻🇪"),
            new Country("VN", "베트남", "🇻🇳"),
            new Country("BY", "벨라루스", "🇧🇾"),
            new Country("BZ", "벨리즈", "🇧🇿"),
            new Country("BQ", "보네르섬, 신트유스타티우스섬, 사바섬", "🇧🇶"),
            new Country("BA", "보스니아 헤르체고비나", "🇧🇦"),
            new Country("BW", "보츠와나", "🇧🇼"),
            new Country("BO", "볼리비아", "🇧🇴"),
            new Country("BI", "부룬디", "🇧🇮"),
            new Country("BF", "부르키나파소", "🇧🇫"),
            new Country("BV", "부베섬", "🇧🇻"),
            new Country("BT", "부탄", "🇧🇹"),
            new Country("MP", "북마리아나제도", "🇲🇵"),
            new Country("MK", "북마케도니아", "🇲🇰"),
            new Country("BG", "불가리아", "🇧🇬"),
            new Country("BR", "브라질", "🇧🇷"),
            new Country("BN", "브루나이", "🇧🇳"),
            new Country("WS", "사모아", "🇼🇸"),
            new Country("SA", "사우디아라비아", "🇸🇦"),
            new Country("GS", "사우스조지아 사우스샌드위치 제도", "🇬🇸"),
            new Country("SM", "산마리노", "🇸🇲"),
            new Country("ST", "상투메 프린시페", "🇸🇹"),
            new Country("MF", "생마르탱(프랑스령)", "🇲🇫"),
            new Country("BL", "생바르텔레미", "🇧🇱"),
            new Country("PM", "생피에르 미클롱", "🇵🇲"),
            new Country("EH", "서사하라", "🇪🇭"),
            new Country("SN", "세네갈", "🇸🇳"),
            new Country("RS", "세르비아", "🇷🇸"),
            new Country("SC", "세이셸", "🇸🇨"),
            new Country("LC", "세인트루시아", "🇱🇨"),
            new Country("VC", "세인트빈센트 그레나딘", "🇻🇨"),
            new Country("KN", "세인트키츠 네비스", "🇰🇳"),
            new Country("SH", "세인트헬레나", "🇸🇭"),
            new Country("SO", "소말리아", "🇸🇴"),
            new Country("SB", "솔로몬 제도", "🇸🇧"),
            new Country("SD", "수단", "🇸🇩"),
            new Country("SR", "수리남", "🇸🇷"),
            new Country("LK", "스리랑카", "🇱🇰"),
            new Country("SJ", "스발바르 얀마옌 제도", "🇸🇯"),
            new Country("SE", "스웨덴", "🇸🇪"),
            new Country("CH", "스위스", "🇨🇭"),
            new Country("ES", "스페인", "🇪🇸"),
            new Country("SK", "슬로바키아", "🇸🇰"),
            new Country("SI", "슬로베니아", "🇸🇮"),
            new Country("SY", "시리아", "🇸🇾"),
            new Country("SL", "시에라리온", "🇸🇱"),
            new Country("SX", "신트마르턴", "🇸🇽"),
            new Country("SG", "싱가포르", "🇸🇬"),
            new Country("AE", "아랍에미리트", "🇦🇪"),
            new Country("AW", "아루바", "🇦🇼"),
            new Country("AM", "아르메니아", "🇦🇲"),
            new Country("AS", "아메리칸사모아", "🇦🇸"),
            new Country("IS", "아이슬란드", "🇮🇸"),
            new Country("HT", "아이티", "🇭🇹"),
            new Country("IE", "아일랜드", "🇮🇪"),
            new Country("AZ", "아제르바이잔", "🇦🇿"),
            new Country("AF", "아프가니스탄", "🇦🇫"),
            new Country("AD", "안도라", "🇦🇩"),
            new Country("AG", "안티구아 바부다", "🇦🇬"),
            new Country("AL", "알바니아", "🇦🇱"),
            new Country("DZ", "알제리", "🇩🇿"),
            new Country("AO", "앙골라", "🇦🇴"),
            new Country("AI", "앵귈라", "🇦🇮"),
            new Country("ER", "에리트레아", "🇪🇷"),
            new Country("SZ", "에스와티니", "🇸🇿"),
            new Country("EE", "에스토니아", "🇪🇪"),
            new Country("EC", "에콰도르", "🇪🇨"),
            new Country("ET", "에티오피아", "🇪🇹"),
            new Country("SV", "엘살바도르", "🇸🇻"),
            new Country("GB", "영국", "🇬🇧"),
            new Country("VG", "영국령 버진아일랜드", "🇻🇬"),
            new Country("IO", "영국령 인도양 지역", "🇮🇴"),
            new Country("YE", "예멘", "🇾🇪"),
            new Country("OM", "오만", "🇴🇲"),
            new Country("AT", "오스트리아", "🇦🇹"),
            new Country("HN", "온두라스", "🇭🇳"),
            new Country("AX", "올란드 제도", "🇦🇽"),
            new Country("JO", "요르단", "🇯🇴"),
            new Country("UG", "우간다", "🇺🇬"),
            new Country("UY", "우루과이", "🇺🇾"),
            new Country("UZ", "우즈베키스탄", "🇺🇿"),
            new Country("UA", "우크라이나", "🇺🇦"),
            new Country("WF", "월리스 푸투나", "🇼🇫"),
            new Country("IQ", "이라크", "🇮🇶"),
            new Country("IR", "이란", "🇮🇷"),
            new Country("IL", "이스라엘", "🇮🇱"),
            new Country("EG", "이집트", "🇪🇬"),
            new Country("IT", "이탈리아", "🇮🇹"),
            new Country("IN", "인도", "🇮🇳"),
            new Country("ID", "인도네시아", "🇮🇩"),
            new Country("JP", "일본", "🇯🇵"),
            new Country("JM", "자메이카", "🇯🇲"),
            new Country("ZM", "잠비아", "🇿🇲"),
            new Country("JE", "저지", "🇯🇪"),
            new Country("GQ", "적도 기니", "🇬🇶"),
            new Country("KP", "조선민주주의인민공화국", "🇰🇵"),
            new Country("GE", "조지아", "🇬🇪"),
            new Country("CN", "중국", "🇨🇳"),
            new Country("CF", "중앙아프리카공화국", "🇨🇫"),
            new Country("DJ", "지부티", "🇩🇯"),
            new Country("GI", "지브롤터", "🇬🇮"),
            new Country("ZW", "짐바브웨", "🇿🇼"),
            new Country("TD", "차드", "🇹🇩"),
            new Country("CZ", "체코", "🇨🇿"),
            new Country("CL", "칠레", "🇨🇱"),
            new Country("CM", "카메룬", "🇨🇲"),
            new Country("CV", "카보베르데", "🇨🇻"),
            new Country("KZ", "카자흐스탄", "🇰🇿"),
            new Country("QA", "카타르", "🇶🇦"),
            new Country("KH", "캄보디아", "🇰🇭"),
            new Country("CA", "캐나다", "🇨🇦"),
            new Country("KE", "케냐", "🇰🇪"),
            new Country("KY", "케이맨 제도", "🇰🇾"),
            new Country("KM", "코모로", "🇰🇲"),
            new Country("CR", "코스타리카", "🇨🇷"),
            new Country("CC", "코코스(킬링) 제도", "🇨🇨"),
            new Country("CI", "코트디부아르", "🇨🇮"),
            new Country("CO", "콜롬비아", "🇨🇴"),
            new Country("CG", "콩고", "🇨🇬"),
            new Country("CD", "콩고민주공화국", "🇨🇩"),
            new Country("CU", "쿠바", "🇨🇺"),
            new Country("KW", "쿠웨이트", "🇰🇼"),
            new Country("CK", "쿡 제도", "🇨🇰"),
            new Country("CW", "퀴라소", "🇨🇼"),
            new Country("HR", "크로아티아", "🇭🇷"),
            new Country("CX", "크리스마스섬", "🇨🇽"),
            new Country("KG", "키르기스스탄", "🇰🇬"),
            new Country("KI", "키리바시", "🇰🇮"),
            new Country("CY", "키프로스", "🇨🇾"),
            new Country("TJ", "타지키스탄", "🇹🇯"),
            new Country("TZ", "탄자니아", "🇹🇿"),
            new Country("TH", "태국", "🇹🇭"),
            new Country("TC", "터크스 케이커스 제도", "🇹🇨"),
            new Country("TR", "터키", "🇹🇷"),
            new Country("TG", "토고", "🇹🇬"),
            new Country("TK", "토켈라우", "🇹🇰"),
            new Country("TO", "통가", "🇹🇴"),
            new Country("TM", "투르크메니스탄", "🇹🇲"),
            new Country("TV", "투발루", "🇹🇻"),
            new Country("TN", "튀니지", "🇹🇳"),
            new Country("TT", "트리니다드 토바고", "🇹🇹"),
            new Country("PA", "파나마", "🇵🇦"),
            new Country("PY", "파라과이", "🇵🇾"),
            new Country("PK", "파키스탄", "🇵🇰"),
            new Country("PG", "파푸아뉴기니", "🇵🇬"),
            new Country("PW", "팔라우", "🇵🇼"),
            new Country("PS", "팔레스타인", "🇵🇸"),
            new Country("FO", "페로 제도", "🇫🇴"),
            new Country("PE", "페루", "🇵🇪"),
            new Country("PT", "포르투갈", "🇵🇹"),
            new Country("FK", "포클랜드 제도", "🇫🇰"),
            new Country("PL", "폴란드", "🇵🇱"),
            new Country("PR", "푸에르토리코", "🇵🇷"),
            new Country("FR", "프랑스", "🇫🇷"),
            new Country("GF", "프랑스령 기아나", "🇬🇫"),
            new Country("TF", "프랑스령 남부 지역", "🇹🇫"),
            new Country("PF", "프랑스령 폴리네시아", "🇵🇫"),
            new Country("FJ", "피지", "🇫🇯"),
            new Country("FI", "핀란드", "🇫🇮"),
            new Country("PH", "필리핀", "🇵🇭"),
            new Country("PN", "핏케언 제도", "🇵🇳"),
            new Country("HM", "허드 맥도널드 제도", "🇭🇲"),
            new Country("HU", "헝가리", "🇭🇺"),
            new Country("HK", "홍콩", "🇭🇰")
        );  
        countries.forEach(countryRepository::save);
        countryRepository.flush(); // 강제 DB 반영
        //List<Country> check = countryRepository.findAll();
        //System.out.println(">> 실제 저장된 수: " + check.size());
    }
}
