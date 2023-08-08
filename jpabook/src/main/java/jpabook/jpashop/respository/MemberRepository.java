package jpabook.jpashop.respository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

@Repository // @Component 내장 : component scan 대상 (bean으로 자동 등록)
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
        // insert문이 생성되지 않음 : commit될때 flush되면서 insert문이 나감
    }
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    // JPQL : entity 대상으로 query를 작성
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
    // JPQL : parameter binding하는 법
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name",
                        Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
