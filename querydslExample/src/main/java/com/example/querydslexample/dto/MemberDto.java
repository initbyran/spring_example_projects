package com.example.querydslexample.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;

    // querydsl에 대한 의존성 발생
    // -> 여러 layer에서 dto는 활용되므로 아키텍쳐상 순수성 저해
    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
