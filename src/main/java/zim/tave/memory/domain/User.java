package zim.tave.memory.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long id;

    //카카오 로그인
    @Column(name = "kakaoId", unique=true)
    private String kakaoId;
    @Column(name = "profileImageUrl")
    private String profileImageUrl;

    @Column(name = "surName")
    private String surName;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "koreanName")
    private String koreanName;

    @Column(name = "createdAt")
    private LocalDate createdAt;
    @Column(name = "status")
    private boolean status;
    @Column(name = "birth")
    private LocalDate birth;
    @Column(name = "nationality")
    private String nationality;
    @Column(name = "isRegistered", nullable = false)
    private boolean isRegistered; // 회원가입 완료 여 (true = 가입 완료)

    //마이페이지 Statistics 정보
    @Column(name = "diaryCount")
    private Long diaryCount; //일기 수
    @Column(name = "visitedCountryCount")
    private Long visitedCountryCount; //방문한 나라 수

    @Column(name = "flags", length = 255)
    private String flags; //국기

    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Setting setting;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trip> trips = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Diary> diaries = new ArrayList<>();
}

