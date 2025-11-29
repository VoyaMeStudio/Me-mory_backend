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
    private Long boardThemeId;

    @Column(nullable = false, length = 100)
    private String themeName;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 255)
    private String thumbnailUrl;

    @Column(nullable = false, length = 255)
    private String cardUrl;
}
