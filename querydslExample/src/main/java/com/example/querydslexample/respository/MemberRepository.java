package com.example.querydslexample.respository;

import com.example.querydslexample.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// <Member, Long> ; member, id
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsername(String username);
}
