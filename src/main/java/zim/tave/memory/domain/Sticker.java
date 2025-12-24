package zim.tave.memory.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stickerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String imageUrl;
}
