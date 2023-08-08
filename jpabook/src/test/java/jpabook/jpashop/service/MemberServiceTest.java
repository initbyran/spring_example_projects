package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.respository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

// test 이므로 기본적으로 rollback이 된다
@RunWith(SpringRunner.class) // JUnit 실행시 spring이랑 함께 실행
@SpringBootTest // spring container안에서 test 실행
@Transactional // test 실행 후 rollback
public class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    // rollback이지만 query문을 확인하고싶을때
    @Autowired
    EntityManager em;

    @Test
//    @Rollback(false) // 강제 commit -> insert문 나감
    public void 회원가입() throws Exception {
        //given : 입력
        Member member = new Member();
        member.setName("kim");

        //when : 실행
        Long saveId = memberService.join(member);

        //then : 결과
        em.flush();
        assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //Given
        Member member1 = new Member();
        member1.setName("kim");
        Member member2 = new Member();
        member2.setName("kim");

        //When
        memberService.join(member1);
        memberService.join(member2);
//        try {
//            memberService.join(member2); //예외가 발생
//        } catch(IllegalStateException e) {
//            return;
//        }

        //Then
        fail("예외가 발생해야 한다.");
    }

}