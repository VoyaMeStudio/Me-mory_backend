package zim.tave.memory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Diary {

    @Id @GeneratedValue
    @Column(name = "diaryId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripId")
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "countryId")
    private Country country;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "dateTime", nullable = false)
    private OffsetDateTime dateTime;

    @Lob
    @Column(name = "content", nullable = false, length = 88)
    private String content;

    @Column(name = "detailedLocation")
    private String detailedLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotionId")
    private Emotion emotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weatherId")
    private Weather weather;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20) // lazy loading 시 20개씩 조회
    private List<DiaryImage> diaryImages = new ArrayList<>();

    @Column(name = "createdAt", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "isStored", nullable = false)
    private Boolean isStored;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public void addDiaryImage(DiaryImage image) {
        diaryImages.add(image);
        image.setDiary(this);
    }

    public static Diary createDiary(User user, Trip trip, Country country, String city,
                                   OffsetDateTime dateTime, String content) {
        Diary diary = new Diary();
        diary.setUser(user);
        diary.setTrip(trip);
        diary.setCountry(country);
        diary.setCity(city);
        diary.setDateTime(dateTime);
        diary.setContent(content != null ? content : ""); // content는 선택 필드이므로 null인 경우 빈 문자열로 처리
        diary.setCreatedAt(dateTime != null ? dateTime : OffsetDateTime.now(ZoneOffset.UTC));
        diary.setIsStored(false);
        return diary;
    }

    public void setOptionalFields(String detailedLocation,
                                 Emotion emotion, Weather weather) {
        this.detailedLocation = detailedLocation;
        this.emotion = emotion;
        this.weather = weather;
    }
}
