package zim.tave.memory.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardStickerMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardStickerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardId", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stickerId", nullable = false)
    private Sticker sticker;

    @Column(nullable = false)
    private BigDecimal posX;

    @Column(nullable = false)
    private BigDecimal posY;

    @Column(nullable = false)
    private BigDecimal rotation;
}
