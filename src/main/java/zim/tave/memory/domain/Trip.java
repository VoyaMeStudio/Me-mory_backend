package zim.tave.memory.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tripId")
    private Long id;

    @Column(name = "tripName", length = 14, nullable = false)
    private String tripName;

    @Column(name = "description", length = 56)
    private String description;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;

    @Column(name = "isStored")
    private Boolean isStored = false; //보관 여부 확인

    @Column(name = "isPast")
    private Boolean isPast = false; //과거 여행 여부 확인

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripThemeId")
    private TripTheme tripTheme;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "representativeImageUrl")
    private String representativeImageUrl;

    @JsonManagedReference
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Diary> diaries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.startDate == null) {
            this.startDate = LocalDate.now(ZoneOffset.UTC);
        }
        if (this.endDate == null) {
            this.endDate = this.startDate;
        }
    }
    // 만약 입력 없으면 오늘 날짜로 설정

    public void addDiary(Diary diary) {
        diaries.add(diary);
        diary.setTrip(this);
        this.endDate = diary.getCreatedAt().toLocalDate();
    }

    //==생성 메서드==//
    public static Trip createTrip(User user, String tripName, String description, TripTheme tripTheme) {
        Trip trip = new Trip();
        trip.setUser(user);
        trip.setTripName(tripName);
        trip.setDescription(description);
        trip.setTripTheme(tripTheme);
        trip.setStartDate(LocalDate.now(ZoneOffset.UTC));
        trip.setEndDate(LocalDate.now(ZoneOffset.UTC));
        return trip;
    }

    public void updateEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
