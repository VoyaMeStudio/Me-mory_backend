package zim.tave.memory.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boardThemeId")
    private Long boardThemeId;

    @Column(name = "themeName", nullable = false, length = 100)
    private String themeName;

    @Column(name = "thumbnailUrl", nullable = false, length = 255)
    private String thumbnailUrl;

    @Column(name = "cardUrl", nullable = false, length = 255)
    private String cardUrl;
}
