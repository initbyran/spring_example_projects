package com.example.querydslexample.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;

    // 양방향 연관 관계
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
    public Team(String name) {
        this.name = name;
    }
}
