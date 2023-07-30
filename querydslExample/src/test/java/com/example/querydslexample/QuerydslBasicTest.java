package com.example.querydslexample;

import com.example.querydslexample.entity.Member;
import com.example.querydslexample.entity.QMember;
import com.example.querydslexample.entity.Team;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import java.util.List;

import static com.example.querydslexample.entity.QMember.*;
import static com.example.querydslexample.entity.QTeam.team;
import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL(){
        // member1을 찾아라
        String qlString = "select m from Member m " +
                "where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // compile시점에 오류를 잡을 수 있다
    // parameter binding을 자동으로 해결해준다
    @Test
    public void startQuerydsl(){

//        QMember m = new QMember("m");
//        QMember m = QMember.member;
        //import static com.example.querydslexample.entity.QMember.*;
        // import static(alt+enter)  : QMember.member -> member
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // 검색 조건 1
    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // 검색 조건 2 : and 대신 comma 사용가능
    @Test
    public void searchAndParam(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"),
                        member.age.eq(10))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // 결과 조회
    @Test
    public void resultFetch(){
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(member)
//                .limit(1).fetchOne()
                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        results.getTotal();
        List<Member> content = results.getResults();
        // paging :
//        results.getLimit();

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    // 정렬
    // 회원 나이 내림차순, 최원 이름 올림차순, 이름 없으면 마지막에 null 출력
    @Test
    public void sort(){
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        // null first도 있음

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    // 페이징 : 두번 째 데이터부터 시작하여 최대 두개의 데이터를 가져옴.
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 얼마나 skip할지
                .limit(2)
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }
    // 전체 조회
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 얼마나 skip할지
                .limit(2)
                .fetchResults();
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    // 집합
    @Test
    public void aggregation(){
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();
        // tuple : data type이 여러가지일 때
        // 실제로는 많이 사용되지않고 dto를 사용함
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);

    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception{

        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    // 조인
    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    // 세타 조인
    // 연관관계가 없는 조인(외부조인은 불가능한 형태)
    /**
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

    }

    // 조인-on절 ; 외부조인 (inner join인 경우 ; where로 보통 처리)
    // 엔티티가 하나만 들어간다 : join(member.team, team) -> from (member) leftjoin (team)
    // 1. 조인 대상 필터링
    /**
     * 회원과 팀을 조인, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * jpql : select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() throws Exception{
        List<Tuple> result= queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple = "+tuple);
        }
    }
    // 2. 연관관계가 없는 엔티티 외부 조인
    /**
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    public void join_on_no_relation(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple = "+tuple);
        }
    }

    // 조인 - 패치 조인
    // 연관된 엔티티를 한번에 조회 -> 성능 최적화
    // 1. fetch join이 없을 때
    @PersistenceUnit
    EntityManagerFactory emf;
    @Test
    public void fetchJoinNo() throws Exception{
        // 영속성 컨텍스트에 남아있는 것을 데이터베이스로 넘겨 비운다
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        // 초기화가 된 엔티티인지 아닌지 검증
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }
    // 2. fetch join 적용
    @Test
    public void fetchJoinUser() throws Exception{
        // 영속성 컨텍스트에 남아있는 것을 데이터베이스로 넘겨 비운다
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        // 초기화가 된 엔티티인지 아닌지 검증
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        System.out.println(loaded);
        assertThat(loaded).as("패치 조인 적용").isTrue();
    }

    // 서브 쿼리1
    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery(){
        // alias 중복 방지
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(select(memberSub.age.max())
                        .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }
    // 서브 쿼리2
    /**
     * 나이가 평균 이상인 회원
     */
    @Test
    public void subQueryGoe(){
        // alias 중복 방지
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(select(memberSub.age.avg())
                        .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }
    // 서브 쿼리3

    /**
     * 10살 이상인 회원
     */
    @Test
    public void subQueryIn(){
        // alias 중복 방지
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(select(memberSub.age)
                        .from(memberSub)
                        .where(memberSub.age.gt(10)) // 열살 초과
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }
    // 서브 쿼리4
    @Test
    public void selectSubQuery(){
        // alias 중복 방지
        QMember memberSub = new QMember("memberSub");
        List<Tuple> result = queryFactory
                .select(member.username,
                        // JPAExpressions(select...) : import static(alt+enter)
                        select((memberSub.age.avg()))
                                .from(memberSub))
                .from(member)
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple = "+tuple);
        }
    }

    /**
     * from 절의 서브 쿼리는 되지않음(JPA의 한계)
     * -> 서브 쿼리를 조인으로 변경, 쿼리를 2번 분리해서 실행, nativeSQL 사용
     */

}
