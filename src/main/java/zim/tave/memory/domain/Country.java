package zim.tave.memory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Country {

    @Id
    @Column(name = "countryCode")
    private String countryCode; // 예: "KR", "US"

    @Column(name = "countryName")
    private String countryName;

    @Column(columnDefinition = "VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String emoji;

    public Country(String countryCode, String countryName, String emoji) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.emoji = emoji;
    }

}
